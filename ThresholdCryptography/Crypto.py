from base64 import standard_b64decode, standard_b64encode
from random import SystemRandom
from time import sleep
import sys

from Utils import bits, product, mod_inv, mod_sqrt, publish_list, concat_bits, least_significant, \
    most_significant, list_to_base64, list_to_bytes, bytes_to_list

BB_URL = "http://46.101.148.106"  # the address of the Bulletin Board`
SECRET_FILE = "secret.txt"  # the local file where each party's secret value is stored


class EllipticCurve:
    """ a curve of the form y^2 = x^3+ax+b (mod p)
    self.order is the order of the generator g
    """

    def __init__(self, a, b, p, order):
        self.a = a
        self.b = b
        self.p = p
        self.order = order
        self.generator = None  # assigned after initializing

    def get_zero_member(self):
        return ECGroupMember(self, 0, 0)

    def get_random_member(self):
        return self.generator ** self.get_random_exponent()

    def get_random_exponent(self):
        rng = SystemRandom()
        return rng.randint(1, self.order)

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
            m = (3 * self.x ** 2 + a) * mod_inv((2 * self.y), p)
            m %= p
        else:
            m = (self.y - g.y) * mod_inv((self.x - g.x), p)
            m %= p

        xr = (pow(m, 2, p) - self.x - g.x) % p
        yr = -g.y % p
        yr = (yr + (m * (g.x - xr))) % p
        return ECGroupMember(self.curve, xr, yr)

    def __pow__(self, n, modulo=None):
        """returns multiplication scalar n"""
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
    sign_key is the private key unique to the party, used for creating certificates
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
        for coefficient in self.polynomial.coefficients:
            self.publish_value(g ** coefficient)

    def publish_value(self, value):
        pass  # TODO:publish the value to the BB

    def get_commitment(self, j):
        """returns A_j's commitment to coefficients"""
        # TODO:get commitment from BB
        pass

    def get_public_key(self, j):
        """returns A_j's public key"""
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
        publish_list(cipher_text, 0, self.party_id, signature=None, table_id=None, recipient_id=j,
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
                    "message from party %d does not agree with it's commitment!" % j)  # TODO: handle message not agreeing with commitment
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
        cc = self.hash_func(G, g, c, h, w, u, v)
        z = (r + c * x) % G.order
        self.publish_zkp(c, h, w, u, v, cc, z)

    def publish_zkp(self, c, h, w, u, v, cc, z):
        pass  # TODO:publish zkp to the BB

    def generate_all_zkps(self, votes):
        """generate a zkp for every vote.
        votes is a list of tuples m=(c,d)"""
        for m in votes:
            self.generate_zkp(m[0])

    @staticmethod
    def validate_zkp(hash_func, G, g, c, h, w, u, v, cc, z):
        """returns True iff the zkp is valid"""
        # TODO:this function should be computed on the BB
        return cc == hash_func(G, g, c, h, w, u, v) and u * h ** cc == g ** z and v * w ** cc == h ** z

    @staticmethod
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


class Polynomial:
    """represents a degree t polynomial in the group F_order as a list of t+1 coefficients"""
    def __init__(self, coefficients, order):
        self.coefficients = coefficients
        self.order = order

    def value_at(self, x):
        """returns the value of the polynomial at point x"""
        return sum(c[1] * x ** c[0] for c in enumerate(self.coefficients)) % self.order


def zkp_hash_func(G, g, c, h, w, u, v):
    """hash function used in Zero Knowledge Proof of DLOG Equality"""
    # TODO: write hash function
    pass


# recommended NIST elliptic curves: http://csrc.nist.gov/groups/ST/toolkit/documents/dss/NISTReCur.pdf
_p = 6277101735386680763835789423207666416083908700390324961279
_r = 6277101735386680763835789423176059013767194773182842284081
_b = 0x64210519e59c80e70fa7e9ab72243049feb8deecc146b9b1
_Gx = 0x188da80eb03090f67cbf20eb43a18800f4ff0afd82ff1012
_Gy = 0x07192b95ffc8da78631011ed6b24cdd573f977a11e794811
curve_192 = EllipticCurve(-3, _b, _p, _r)
g_192 = ECGroupMember(curve_192, _Gx, _Gy)
curve_192.generator = g_192

_p = 26959946667150639794667015087019630673557916260026308143510066298881
_r = 26959946667150639794667015087019625940457807714424391721682722368061
_b = 0xb4050a850c04b3abf54132565044b0b7d7bfd8ba270b39432355ffb4
_Gx = 0xb70e0cbd6bb4bf7f321390b94a03c1d356c21122343280d6115c1d21
_Gy = 0xbd376388b5f723fb4c22dfe6cd4375a05a07476444d5819985007e34
curve_224 = EllipticCurve(-3, _b, _p, _r)
g_224 = ECGroupMember(curve_224, _Gx, _Gy)
curve_224.generator = g_224

_p = 115792089210356248762697446949407573530086143415290314195533631308867097853951
_r = 115792089210356248762697446949407573529996955224135760342422259061068512044369
_b = 0x5ac635d8aa3a93e7b3ebbd55769886bc651d06b0cc53b0f63bce3c3e27d2604b
_Gx = 0x6b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296
_Gy = 0x4fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb6406837bf51f5
curve_256 = EllipticCurve(-3, _b, _p, _r)
g_256 = ECGroupMember(curve_256, _Gx, _Gy)
curve_256.generator = g_256

VOTING_CURVE = curve_256
ZKP_HASH_FUNCTION = zkp_hash_func
T = 5  # number of parties needed for decryption
N = 7  # total number of parties
SLEEP_TIME = 1


def get_sign_key():
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


def phase1():
    """steps 1-8 in threshold workflow - voting can only begin after this phase ends successfully
    https://github.com/electronic-voting-workshop-2015/electronic-voting-workshop-2015/wiki/Threshold-Cryptography"""
    print("initializing values of party")
    party_id = int(sys.argv[2])
    sign_key = get_sign_key()  # TODO: write functions that read from public configuration files
    sign_curve = get_sign_curve()
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
    """steps 10-11 in threshold workflow - run only after voting stopped
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


def test():
    g1 = curve_256.get_random_member()
    g2 = curve_256.get_random_member()
    g3 = g1 * g2
    g4 = g1 ** 2
    print(ECGroupMember.verify_point(g1.x, g1.y, curve_256))
    print(ECGroupMember.verify_point(g2.x, g2.y, curve_256))
    print(ECGroupMember.verify_point(g3.x, g3.y, curve_256))
    print(ECGroupMember.verify_point(g3.x, g3.y, curve_256))
    print(g1)
    print(g2)
    print(g3)
    print(g4)


def main():
    if len(sys.argv) != 2:
        print("Error: exactly one argument expected")
    elif sys.argv[1] == "test":
        test()
    elif sys.argv[1] == "phase1":
        phase1()
    elif sys.argv[1] == "phase2":
        phase2()
    else:
        print("Error: argument should be one of: phase1, phase2, test")


if __name__ == "__main__":
    # execute only if run as a script
    main()
