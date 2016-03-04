import hashlib
import sys
from base64 import standard_b64decode, standard_b64encode
from random import SystemRandom
from time import sleep

from ThresholdCryptography.Crypto.Utils import bits, product, mod_inv, mod_sqrt, publish_list, concat_bits, least_significant, \
    most_significant, list_to_bytes, bytes_to_list

BB_URL_PROD = "http://46.101.148.106"  # the address of the production Bulletin Board
BB_URL = "http://10.0.0.12:4567"  # the address of the Bulletin Board for testing - change to the production value when deploying
SECRET_FILE = "secret.txt"  # the local file where each party's secret value is stored
PRIVATE_KEY_FILE = "private.txt"  # the local file where each party's private signing key is stored


class EllipticCurve:
    """ a curve of the form y^2 = x^3+ax+b (mod p)
    self.order is the order of the generator g
    self.int_length is the size in bits of each coordinate
    """

    def __init__(self, a, b, p, order, int_length):
        self.a = a
        self.b = b
        self.p = p
        self.order = order
        self.int_length = int_length
        self.generator = None  # assigned after initializing

    def get_zero_member(self):
        return ECGroupMember(self, 0, 0)

    def get_random_member(self):
        return self.generator ** self.get_random_exponent()

    def get_random_exponent(self):
        rng = SystemRandom()
        return rng.randint(1, self.order)

    def get_member(self, num):
        return self.generator ** num

    def __str__(self):
        return self.a.__str__() + ", " + self.b.__str__() + ", " + self.p.__str__()

    def __eq__(self, y):
        return self.a == y.a and self.b == y.b and self.p == y.p


class ECGroupMember:
    def __init__(self, curve, x, y):
        self.curve = curve
        self.x = x
        self.y = y

    def modinv(self):
        """modular inverse of member: g^-1"""
        return ECGroupMember(self.curve, self.x, -self.y)

    def __div__(self, g):
        return self * g.modinv()

    def __mul__(self, g):
        """returns point addition of two points"""
        if not (isinstance(self, ECGroupMember) and isinstance(g, ECGroupMember)):
            raise Exception('The objects are not ECGroupMember')
        if not self.curve == g.curve:
            raise Exception('The group members are not from the same curve')
        if self == self.curve.get_zero_member():
            return g
        if g == self.curve.get_zero_member():
            return self
        if self.x == g.x and self.y == -g.y:
            return self.curve.get_zero_member()

        a = self.curve.a
        p = self.curve.p

        if self == g:
            m = (3 * self.x ** 2 + a) * mod_inv((2 * self.y) % p, p)
            m %= p
        else:
            m = (self.y - g.y) * mod_inv((self.x - g.x) % p, p)
            m %= p

        xr = (pow(m, 2, p) - self.x - g.x) % p
        yr = -g.y % p
        yr = (yr + (m * (g.x - xr))) % p
        return ECGroupMember(self.curve, xr, yr)

    def __pow__(self, n, modulo=None):
        """returns multiplication scalar n"""
        # TODO: use faster algorithm if needed: https://en.wikipedia.org/wiki/Elliptic_curve_point_multiplication#Point_multiplication
        if n == -1:
            return self.modinv()
        res = self.curve.get_zero_member()
        addend = self

        for bit in bits(n):
            if bit == 1:
                res = res * addend
            addend = addend * addend

        return res

    def __bytes__(self):
        """convert x any y values to a compact object for transferring to disk or network
        object is a record with 2 field: (self.x, self.y). each field is a little endian integer the same size as p"""
        length = self.curve.p.bit_length() // 8
        xb = self.x.to_bytes(length, 'little')
        yb = self.y.to_bytes(length, 'little')
        return xb + yb

    def to_base64(self):
        """encodes object as base64 string"""
        return standard_b64encode(bytes(self)).decode('utf-8')

    @staticmethod
    def from_bytes(data, curve):
        """converts a bytes object to a ECGroupMember object"""
        length = curve.p.bit_length() // 8
        if len(data) != 2 * length:
            raise Exception('binary data does not match curve parameters')
        xb = data[0:length]
        yb = data[length:2 * length]
        x = int.from_bytes(xb, 'little')
        y = int.from_bytes(yb, 'little')
        if not ECGroupMember.verify_point(x, y, curve):
            raise Exception('binary data does not match curve parameters')
        return ECGroupMember(curve, x, y)

    @staticmethod
    def from_base64(data, curve):
        """converts base64 string to a ECGroupMember object"""
        data_bytes = standard_b64decode(data.encode('utf-8'))
        return ECGroupMember.from_bytes(data_bytes, curve)

    @staticmethod
    def verify_point(x, y, curve):
        """returns True iff the point (x,y) is on the curve"""
        p = curve.p
        a = curve.a
        b = curve.b
        return y ** 2 % p == (x ** 3 + a * x + b) % p

    def __str__(self):
        return self.x.__str__() + ", " + self.y.__str__() + ", " + self.curve.__str__()

    def __eq__(self, other):
        return self.curve == other.curve and self.x == other.x and self.y == other.y

    @staticmethod
    def from_int(num, num_len, curve):
        """encodes integer num to an ECCGroupMember object
        algorithm from https://eprint.iacr.org/2013/373.pdf part 2.4
        num must be of bit size at most half of the bit size of curve.order"""
        rng = SystemRandom()
        prefix_length = curve.order.bit_length() - num_len
        a = curve.a
        b = curve.b
        p = curve.p
        while True:
            prefix = rng.randint(1, 2 ** prefix_length - 1)
            candidate_x = concat_bits(prefix, num, num_len)
            candidate_y2 = (candidate_x ** 3 + a * candidate_x + b) % p
            candidate_y = mod_sqrt(candidate_y2, p)
            if ECGroupMember.verify_point(candidate_x, candidate_y, curve):
                return ECGroupMember(curve, candidate_x, candidate_y)

    @staticmethod
    def to_int(group_member, curve, bit_length):
        """complementary method to from_int - decodes int from group_member"""
        return least_significant(group_member.x, bit_length)


class ThresholdParty:
    """represents a member participating in the decryption process
    n is the number of parties, t is the number of parties required to decrypt
    sign_key is the private key unique to the party, used for creating certificates (an integer)
    sign_curve is the curve used for signing
    is_phase1 is a boolean value, signifying that the party is in phase1 of the threshold process
    algorithms from: http://moodle.tau.ac.il/pluginfile.php/217242/mod_resource/content/1/Tomer.pdf
    """

    def __init__(self, voting_curve, t, n, party_id, hash_func, sign_key, sign_curve, is_phase1):
        self.voting_curve = voting_curve
        self.t = t
        self.n = n
        self.party_id = party_id  # a number between 0 and n-1

        if is_phase1:
            self.polynomial = Polynomial([voting_curve.get_random_exponent() for _ in range(t)], voting_curve.order)
            self.secret_value = None  # the value f(j), assigned after calling validate_all_messages
        else:
            self.load_secret()  # load secret value from file if in phase2

        self.hash_func = hash_func  # the cryptographic hash function used for ZKP
        self.sign_key = sign_key
        self.sign_curve = sign_curve

    def publish_commitment(self):
        """publish the values g^v_i,g^a_i1...,g^a_it to the BB"""
        g = self.voting_curve.generator
        commitments = [g ** coefficient for coefficient in self.polynomial.coefficients]
        cert = self.sign(list_to_bytes(commitments))
        publish_list(commitments, 0, self.party_id, certificate=cert, table_id=None, recipient_id=None,
                     url=BB_URL)  # TODO: supply proper arguments

    def publish_value(self, value):
        """publish commitment to secret value"""
        cert = self.sign(bytes(value))
        publish_list(value, 0, self.party_id, certificate=cert, table_id=None, recipient_id=None,
                     url=BB_URL)  # TODO: supply proper arguments

    def get_commitment(self, j):
        """returns A_j's commitment to coefficients"""
        # TODO:get commitment from BB
        pass

    def get_public_key(self, j):
        """returns A_j's public key: g^v_j"""
        # TODO:get public key from the BB
        pass

    def encrypt_message(self, public_key, value):
        """returns encrypted value using public_key
        converts the value to 2 group members, the first is
        an encoding of the first half of value (most significant bits),
        the second encoded from the second half
        the returned cipher text is a quartet of group members"""
        g = self.voting_curve.generator
        r1 = self.voting_curve.get_random_exponent()
        r2 = self.voting_curve.get_random_exponent()
        int_length = self.voting_curve.order.bit_length() // 2
        first_half = most_significant(value, int_length)
        second_half = least_significant(value, int_length)
        m1 = ECGroupMember.from_int(first_half, int_length, self.voting_curve)
        m2 = ECGroupMember.from_int(second_half, int_length, self.voting_curve)
        return g ** r1, m1 * (public_key ** r1), g ** r2, m2 * (public_key ** r2)

    def decrypt_message(self, private_key, cipher_text):
        """returns an int that was encrypted using the encrypt_message"""
        g = self.voting_curve.generator
        c1 = cipher_text[0]
        c2 = cipher_text[2]
        d1 = cipher_text[1]
        d2 = cipher_text[3]
        x = private_key
        s1 = c1 ** x
        s2 = c2 ** x
        message1 = d1 * s1 ** -1
        message2 = d2 * s2 ** -1
        int_length = self.voting_curve.order.bit_length() // 2
        return concat_bits(message1, message2, int_length)

    def send_message(self, j):
        """send f_i(j) to party A_j"""
        message = self.polynomial.value_at(j)
        public_key = self.get_public_key(j)
        cipher_text = self.encrypt_message(public_key, message)
        publish_list(cipher_text, 0, self.party_id, certificate=None, table_id=None, recipient_id=j,
                     url=BB_URL)  # TODO: supply proper arguments

    def send_values(self):
        """send values f_i(j) to all parties A_j"""
        for j in range(self.n):
            if j == self.party_id:
                continue  # don't send message to myself
            self.send_message(j)

    def get_message(self, j):
        """returns A_j's message: f_j(i)"""
        cipher_text = None  # TODO:get cipher text from BB
        private_key = self.polynomial.coefficients[0]
        return self.decrypt_message(private_key, cipher_text)

    def validate_message(self, j, message, commitment):
        """returns True iff the message from A_j agrees with A_j's commitment"""
        exponent = self.polynomial.value_at(j)
        g = self.voting_curve.generator
        return message == g ** exponent

    def validate_all_messages(self):
        """returns True iff all the messages from the other parties
        agree with their commitments. Also computes and stores the secret value,
        and publishes commitment to secret value"""
        messages = []
        all_valid = True  # flag signifies all messages agree with commitments
        for j in range(self.n):
            if j == self.party_id:
                continue
            message = self.get_message(j)
            commitment = self.get_commitment(j)
            if self.validate_message(j, message, commitment):
                messages.append(message)
            else:
                print(
                    "message from party %d does not agree with it's commitment!" % j)
                all_valid = False

        if not all_valid:
            return False

        # compute s_i = f(i) and publish h_i = g^s_i
        self.secret_value = sum(self.polynomial.value_at(x) for x in messages) % self.voting_curve.order
        self.save_secret()
        g = self.voting_curve.generator
        self.publish_value(g ** self.secret_value)
        return True

    def save_secret(self):
        """save secret value to file"""
        b_secret = list_to_bytes(self.secret_value)
        with open(SECRET_FILE, 'wb') as f:
            f.write(b_secret)

    def load_secret(self):
        """load secret value from file"""
        try:
            with open(SECRET_FILE, 'rb') as f:
                b_secret = f.readall()
                self.secret_value = bytes_to_list(b_secret)
        except IOError:
            print("could not open file %s\n" % SECRET_FILE)

    def generate_zkp(self, c):
        """variable names follow Tomer page 4 and page 5 bottom.
        c is part of the message: m=(c,d). cc is the 'random' challenge"""
        x = self.secret_value
        w = c ** x
        g = self.voting_curve.generator
        h = g ** x
        G = self.voting_curve
        r = G.get_random_exponent()
        u = g ** r
        v = h ** r
        cc = self.hash_func(g, c, h, w, u, v)
        z = (r + c * x) % G.order
        proof = ZKP(c, h, w, u, v, cc, z)
        self.publish_zkp(proof)

    def publish_zkp(self, proof):
        cert = self.sign(bytes(proof))
        publish_list(proof, 0, self.party_id, certificate=cert, table_id=None, recipient_id=None,
                     url=BB_URL)  # TODO: supply proper arguments

    def generate_all_zkps(self, votes):
        """generate a zkp for every vote.
        votes is a list of tuples m=(c,d)"""
        for m in votes:
            self.generate_zkp(m[0])

    def sign(self, message):
        """ Signs message using ECDSA.
        :param message: bytes to sign
        :return: bytes representing r, s.
        """
        m = hashlib.sha256()
        m.update(message)
        e = m.digest()
        ln = self.sign_curve.order.bit_length() // 8
        n = self.sign_curve.order
        z = e[0:ln]
        z = int.from_bytes(z, byteorder='big')  # Matching the BigInteger form in the java signing.
        certificate = 0
        while certificate == 0:
            rng = SystemRandom()
            k = rng.randint(1, n)
            kg = self.sign_curve.get_member(k)
            r = kg.x
            if r == 0:
                continue
            s = (mod_inv(k, n) * (z + (r * self.sign_key) % n) % n) % n
            if s == 0:
                continue
            l = [r, s]
            int_length = self.sign_curve.int_length
            certificate = list_to_bytes(l, int_length)
        return certificate


class Polynomial:
    """represents a degree t polynomial in the group F_order as a list of t+1 coefficients
    P(x) = coefficients[0]*x^0 + coefficients[1]*x^1 +..."""

    def __init__(self, coefficients, order):
        self.coefficients = coefficients
        self.order = order

    def value_at(self, x):
        """returns the value of the polynomial at point x"""
        return sum(coefficient * x ** exponent for exponent, coefficient in enumerate(self.coefficients)) % self.order


class ZKP:
    """represents a Zero Knowledge Proof of DLOG equality
    c, h, w, u, v are group members
    cc, z are large integers
    """

    def __init__(self, c, h, w, u, v, cc, z):
        self.c = c
        self.h = h
        self.w = w
        self.u = u
        self.v = v
        self.cc = cc
        self.z = z

    def __bytes__(self):
        first_part = list_to_bytes([self.c, self.h, self.w, self.u, self.v])
        second_part = list_to_bytes([self.cc, self.z], self.c.curve.int_length)
        return first_part + second_part

    @staticmethod
    def from_bytes(data, curve):
        int_length = curve.p.bit_length() // 8
        if len(data) != 12 * int_length:
            raise Exception('binary data does not match curve parameters')
        first_part = data[0:5 * 2 * int_length]  # 5 group members, each one is size 2*int_length
        second_part = data[5 * 2 * int_length:]
        c, h, w, u, v = bytes_to_list(first_part, curve=curve)
        cc, z = bytes_to_list(second_part, member_length=int_length)
        return ZKP(c, h, w, u, v, cc, z)


def verify_certificate(publicKey, encrypted_message, certificate, sign_curve):
    int_length = sign_curve.int_length
    l = bytes_to_list(certificate, int_length)
    r = l[0]
    s = l[1]
    print(r)
    print(s)
    if r < 1 or r > sign_curve.order:
        return False
    if s < 1 or s > sign_curve.order:
        return False
    m = hashlib.sha256()
    m.update(encrypted_message)
    e = m.digest()
    ln = sign_curve.order.bit_length()
    n = sign_curve.order
    z = e[0:ln]
    z = int.from_bytes(z, byteorder='big')  # Matching the BigInteger form in the java signing.
    w = mod_inv(s, n)
    u1 = (z * w) % n
    u2 = (w * r) % n
    gu1 = sign_curve.get_member(u1)
    pu2 = publicKey.__pow__(u2)
    x1y1 = gu1.__mul__(pu2)
    x1 = x1y1.x
    return r == x1


def zkp_hash_func(g, c, h, w, u, v):
    """hash function used in Zero Knowledge Proof of DLOG Equality
    returns an integer between 1 and G.order"""
    bytes_data = list_to_bytes([g, c, h, w, u, v])
    m = hashlib.sha256()
    m.update(bytes_data)
    bytes_hash = m.digest()
    return int.from_bytes(bytes_hash, 'little') % g.curve.order


def decrypt_vote(curve, party_ids, commitments, d):
    """decrypts a single vote using Lagrange interpolation on t point
        party_ids is a list of t integers, commitments is a list of t commitments
        d is part of the cipher text - (c,d)
        performed after validating the ZKPs"""
    # TODO:this function should be computed on the BB
    q = curve.order
    lambda_list = []
    for j in party_ids:
        l = ((i * mod_inv(i - j, q)) % q for i in party_ids if i != j)
        lambda_list.append(product(l, q))
    cs = product(commitments[j] ** lambda_list[j] for j in party_ids)
    return d * cs ** -1


def validate_zkp(hash_func, g, proof):
    """returns True iff the zkp is valid"""
    # TODO:this function should be computed on the BB
    c = proof.c
    h = proof.h
    w = proof.w
    u = proof.u
    v = proof.v
    cc = proof.cc
    z = proof.z
    return cc == hash_func(g, c, h, w, u, v) and u * h ** cc == g ** z and v * w ** cc == h ** z


# recommended NIST elliptic curves: http://csrc.nist.gov/groups/ST/toolkit/documents/dss/NISTReCur.pdf
_p = 6277101735386680763835789423207666416083908700390324961279
_r = 6277101735386680763835789423176059013767194773182842284081
_b = 0x64210519e59c80e70fa7e9ab72243049feb8deecc146b9b1
_Gx = 0x188da80eb03090f67cbf20eb43a18800f4ff0afd82ff1012
_Gy = 0x07192b95ffc8da78631011ed6b24cdd573f977a11e794811
curve_192 = EllipticCurve(-3, _b, _p, _r, 192)
g_192 = ECGroupMember(curve_192, _Gx, _Gy)
curve_192.generator = g_192

_p = 26959946667150639794667015087019630673557916260026308143510066298881
_r = 26959946667150639794667015087019625940457807714424391721682722368061
_b = 0xb4050a850c04b3abf54132565044b0b7d7bfd8ba270b39432355ffb4
_Gx = 0xb70e0cbd6bb4bf7f321390b94a03c1d356c21122343280d6115c1d21
_Gy = 0xbd376388b5f723fb4c22dfe6cd4375a05a07476444d5819985007e34
curve_224 = EllipticCurve(-3, _b, _p, _r, 224)
g_224 = ECGroupMember(curve_224, _Gx, _Gy)
curve_224.generator = g_224

_p = 115792089210356248762697446949407573530086143415290314195533631308867097853951
_r = 115792089210356248762697446949407573529996955224135760342422259061068512044369
_b = 0x5ac635d8aa3a93e7b3ebbd55769886bc651d06b0cc53b0f63bce3c3e27d2604b
_Gx = 0x6b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296
_Gy = 0x4fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb6406837bf51f5
curve_256 = EllipticCurve(-3, _b, _p, _r, 256)
g_256 = ECGroupMember(curve_256, _Gx, _Gy)
curve_256.generator = g_256

VOTING_CURVE = curve_256
ZKP_HASH_FUNCTION = zkp_hash_func
T = 5  # number of parties needed for decryption
N = 7  # total number of parties
SLEEP_TIME = 1


def get_sign_key():
    """reads the private key used for signing from local file"""
    pass


def get_sign_curve():
    pass


def get_public_key_confirmation():
    """returns True iff the BB finished computing the public key"""
    pass


def get_sent_messages_confirmation():
    """returns True iff the BB contains secret messages from every party"""
    pass


def get_votes():
    """returns all the votes from the BB.
    Each vote is a tuple (c,d) of group members"""
    pass


def get_votes_local():
    """same as get_votes, but gets data from the local DB"""
    pass


def get_zkps_local():
    """returns all the zkps from the BB.
    output is a list of lists , one sub-list for every vote cast.
    each sub-list contains N tuples, one for every party
    the tuple contains the party id as the first object and ZKP as the second"""
    # TODO: add to JSON API
    pass


def get_voting_curve_local():
    """returns the voting curve"""
    # TODO: add to JSON API
    pass


def phase1():
    """steps 1-8 in threshold workflow - voting can only begin after this phase ends successfully
    https://github.com/electronic-voting-workshop-2015/electronic-voting-workshop-2015/wiki/Threshold-Cryptography"""
    print("initializing values of party")
    party_id = int(sys.argv[2])
    sign_key = get_sign_key()  # TODO: write function that reads from private configuration file
    sign_curve = get_sign_curve()  # TODO: write functions that read from public configuration files
    party = ThresholdParty(VOTING_CURVE, T, N, party_id, ZKP_HASH_FUNCTION, sign_key, sign_curve, is_phase1=True)

    print("publishing commitment")
    party.publish_commitment()
    while True:
        if get_public_key_confirmation():
            break
        sleep(SLEEP_TIME)

    print("sending secret values to other parties")
    party.send_values()
    while True:
        if get_sent_messages_confirmation():
            break
        sleep(SLEEP_TIME)

    print("validating messages from other parties")
    if not party.validate_all_messages():
        print("Fatal Error: one or more parties message's does not agree with commitment!!!")
        print("Aborting")
        sys.exit()

    print("phase 1 completed successfully - voting can now start!")
    sys.exit()


def phase2():
    """step 10 in threshold workflow - run only after voting stopped
    https://github.com/electronic-voting-workshop-2015/electronic-voting-workshop-2015/wiki/Threshold-Cryptography"""
    print("initializing values of party")
    party_id = int(sys.argv[2])
    sign_key = get_sign_key()  # TODO: write functions that read from public configuration files
    sign_curve = get_sign_curve()
    party = ThresholdParty(VOTING_CURVE, T, N, party_id, ZKP_HASH_FUNCTION, sign_key, sign_curve, is_phase1=False)

    print("retrieving voting data from the Bulletin Board")
    votes = get_votes()

    print("generating and publishing zero knowledge proofs")
    party.generate_all_zkps(votes)

    print("phase 2 completed successfully - decryption can now start on the Bulletin Board!")
    sys.exit()


def phase3():
    """step 11 in threshold workflow - run on the BB after phase 2 ended
    https://github.com/electronic-voting-workshop-2015/electronic-voting-workshop-2015/wiki/Threshold-Cryptography"""
    print("retrieving voting data from the Database")
    votes = get_votes_local()

    print("retrieving zero knowledge proofs from the database")
    zkps = get_zkps_local()

    print("verifying validity of zero knowledge proofs and decrypting")
    curve = get_voting_curve_local()
    g = curve.generator
    decrypted_votes = []  # a list of tuples: (vote_id, vote)

    for vote_list in zkps:
        A = []  # the list of valid zkps

        for party_id, proof in vote_list:
            if len(A) == T:  # we got t valid parties, we can now decrypt the message
                break
            if validate_zkp(ZKP_HASH_FUNCTION, g, proof):
                A.append(proof)
        if len(A) < T:
            print("Fatal Error: could not decrypt a vote: not enough parties provided the required data")
            sys.exit()

        party_ids = [zkp[0] for zkp in A]
        commitments = [zkp[1].w for zkp in A]
        vote_id = vote_list[0]  # TODO: how to get the vote_id?
        d = votes[vote_id]  # the encrypted vote
        decrypted_vote = decrypt_vote(curve, party_ids, commitments, d)
        decrypted_votes.append((vote_id, decrypted_vote))

        #  TODO: process decrypted_votes and print the results of the election


def test():
    sign_curve = VOTING_CURVE
    sign_keys = [sign_curve.get_random_exponent() for _ in range(N)]
    parties = [ThresholdParty(VOTING_CURVE, T, N, i, ZKP_HASH_FUNCTION, sign_keys[i], sign_curve, is_phase1=True) for i
               in range(N)]

    for party in parties:
        party.publish_commitment()
    for party in parties:
        party.send_values()


