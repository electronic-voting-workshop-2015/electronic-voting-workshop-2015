import hashlib
import os
import sys
from base64 import standard_b64decode, standard_b64encode
from random import SystemRandom
from time import sleep
from collections import defaultdict
from json import dumps
import math

gmpy2_is_installed = False  # TODO: profile optimization
try:
    from gmpy2 import mpz
except ImportError:
    gmpy2_is_installed = False

from .Utils import bits, product, mod_inv, mod_sqrt, concat_bits, least_significant, \
    most_significant, list_to_bytes, bytes_to_list, publish_dict, bytes_to_base64, \
    list_to_base64, base64_to_list, base64_to_bytes, get_bb_data

# TODO: organize constants (Ilay)
BB_URL_PROD = "http://46.101.148.106"  # the address of the production Bulletin Board
BB_URL = "http://10.0.0.2:4567"  # the address of the Bulletin Board for testing - change to the production value when deploying
LOCAL_BB_URL = "http://localhost:4567"  # the address of the Bulletin Board when running on the Bulletin Board
#LOCAL_BB_URL = BB_URL  # the address of the Bulletin Board when running on the Bulletin Board
SECRET_FILE = "secret.txt"  # the local file where each party's secret value is stored
RESULT_FILE = "result.json"  # the file where the final results are stored
MY_PRIVATE_KEY_PATH = ""  # the path for local file where the party's private signing key is stored (relative to working dir)s
PRIVATE_KEYS_PATH = ""  # The paths were the private keys will be saved on the server when generated.
PUBLISH_COMMITMENT_TABLE = "/publishCommitment"
PUBLISH_SECRET_COMMITMENT_TABLE = "/publishSecretCommitment"
GET_SECRET_COMMITMENT_TABLE = "/retrieveSecretCommitment"
PUBLISH_MESSAGE_TABLE = "/publishMessage"
PUBLISH_VOTING_PUBLIC_KEY_TABLE = "/publishVotingPublicKey"
PUBLISH_PUBLIC_KEY_TABLE_FOR_PARTIES = "/publishPublicKey"
SEND_VOTE_TABLE = "/sendVote"
GET_VOTES_TABLE = "/getBBVotes"
PUBLISH_ZKP_TABLE = "/publishZKP"
GET_ZKP_TABLE = "/getZKP"
GET_VOTING_PUBLIC_KEY_TABLE = "/retrieveVotingPublicKey"
GET_PUBLIC_KEY_TABLE = "/getPublicKey"
GET_COMMITMENT_TABLE = "/retrieveCommitment"
GET_MESSAGES_TABLE = "/retrieveMessage"


class EllipticCurve:
    """ a curve of the form y^2 = x^3+ax+b (mod p)
    self.order is the order of the generator g
    self.int_length is the size in bits of each coordinate
    """

    def __init__(self, a, b, p, order, int_length):
        if gmpy2_is_installed:
            self.a = mpz(a)
            self.b = mpz(b)
            self.p = mpz(p)
            self.order = mpz(order)
        else:
            self.a = a
            self.b = b
            self.p = p
            self.order = order

        self.int_length = int_length
        self.generator = None  # assigned after initializing
        self.zero_member = ECGroupMember(self, 0, 0)

    def get_zero_member(self):
        return self.zero_member

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
        if gmpy2_is_installed:
            self.x = mpz(x)
            self.y = mpz(y)
        else:
            self.x = x
            self.y = y

    def modinv(self):
        """modular inverse of member: g^-1"""
        return ECGroupMember(self.curve, self.x, -self.y)

    def __div__(self, g):
        return self * g.modinv()

    def double(self):
        a = self.curve.a
        p = self.curve.p

        m = ((3 * self.x * self.x + a) * mod_inv(2 * self.y, p)) % p
        xr = (m * m - 2 * self.x) % p
        yr = (m * (self.x - xr) - self.y) % p
        return ECGroupMember(self.curve, xr, yr)

    def __mul__(self, other):
        """returns point addition of two points"""
        if not self.curve == other.curve:
            raise Exception('The group members are not from the same curve')
        if self == self.curve.get_zero_member():
            return other
        if other == self.curve.get_zero_member():
            return self
        if self.x == other.x and self.y == -other.y:
            return self.curve.get_zero_member()
        if self == other:
            return self.double()

        p = self.curve.p

        m = (self.y - other.y) * mod_inv(self.x - other.x, p) % p
        xr = (m * m - self.x - other.x) % p
        yr = (m * (self.x - xr) - self.y) % p
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
            addend = addend.double()

        return res

    def __bytes__(self):
        """convert x any y values to a compact object for transferring to disk or network
        object is a record with 2 field: (self.x, self.y). each field is a little endian integer the same size as p"""
        length = self.curve.p.bit_length() // 8
        xb = int(self.x).to_bytes(length, 'little')
        yb = int(self.y).to_bytes(length, 'little')
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
        return "[" + self.x.__str__() + ", " + self.y.__str__() + "]"

    __repr__ = __str__

    def __eq__(self, other):
        return self.curve == other.curve and self.x == other.x and self.y == other.y

    def __hash__(self):
        return hash((self.x, self.y))

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
            self.polynomial = Polynomial([voting_curve.get_random_exponent() for _ in range(t + 1)], voting_curve.order)
            self.secret_value = None  # the value f(j), assigned after calling validate_all_messages
        else:
            self.load_secret()  # load secret value from file if in phase2

        self.hash_func = hash_func  # the cryptographic hash function used for ZKP
        self.sign_key = sign_key
        self.sign_curve = sign_curve
        self.other_commitments = None  # caches the other parties commitments used in phase 1

    def publish_commitment(self):
        """publish the values g^v_i,g^a_i1...,g^a_it to the BB"""
        g = self.voting_curve.generator
        commitments = [g ** coefficient for coefficient in self.polynomial.coefficients]
        cert = self.sign(list_to_bytes(commitments))
        base64_cert = bytes_to_base64(cert)
        base64_data = list_to_base64(commitments, int_length=0)
        dictionary = {"party_id": self.party_id, "commitment": base64_data}
        dictionary2 = {"content": dictionary, "party_id": self.party_id, "data": base64_data, "signature": base64_cert}
        publish_dict(dictionary2, BB_URL + PUBLISH_COMMITMENT_TABLE)

    def publish_secret_commitment(self, value):
        """publish commitment to secret value"""
        cert = self.sign(bytes(value))
        base64_cert = bytes_to_base64(cert)
        base64_value = list_to_base64([value], int_length=0)
        dictionary = {'party_id': self.party_id, 'secret_commitment': base64_value}
        dictionary2 = {"content": dictionary, "party_id": self.party_id, "data": base64_value, "signature": base64_cert}
        publish_dict(dictionary2, BB_URL + PUBLISH_SECRET_COMMITMENT_TABLE)

    def retrieve_commitments(self):
        """retrieves all commitment to coefficients as a dictionary mapping j to j's commitment"""
        self.other_commitments = get_commitments()
        assert len(self.other_commitments) == self.n

    def get_public_key(self, j):
        """returns A_j's public key: g^v_j"""
        if not self.other_commitments:
            self.retrieve_commitments()
        commitments = self.other_commitments
        return commitments[j][0]

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
        second_half = least_significant(value, int_length)
        first_half = most_significant(value, return_size=int_length, total_size=int_length * 2)
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
        int1 = ECGroupMember.to_int(message1, self.voting_curve, int_length)
        int2 = ECGroupMember.to_int(message2, self.voting_curve, int_length)
        return concat_bits(int1, int2, int_length)

    def send_message(self, j):
        """send f_i(j) to party A_j"""
        message = self.polynomial.value_at(j)
        public_key = self.get_public_key(j)
        cipher_text = self.encrypt_message(public_key, message)
        cert = self.sign(list_to_bytes(cipher_text, int_length=0))
        base64_cert = bytes_to_base64(cert)
        base64_data = list_to_base64(cipher_text, int_length=0)
        # TODO: fix type in ruby: recepient -> recipient
        dictionary = {"party_id": self.party_id, "recepient_id": j, "message": base64_data, "signature": base64_cert, "data": base64_data}
        publish_dict(dictionary, BB_URL + PUBLISH_MESSAGE_TABLE)

    def send_values(self):
        """send values f_i(j) to all parties A_j"""
        for j in range(1, self.n + 1):
            if j == self.party_id:
                continue  # don't send message to myself
            self.send_message(j)

    def get_messages(self):
        """returns the other parties' messages as a dictionary
        mapping party_id j to f_j(i)"""
        json_data = get_bb_data(BB_URL + GET_MESSAGES_TABLE + "?recepient_id=" + str(self.party_id))
        assert len(json_data) == self.n - 1
        private_key = self.polynomial.coefficients[0]
        res = {dictionary['party_id']: self.decrypt_message(private_key, base64_to_list(dictionary['message'],
                                                                                        curve=self.voting_curve))
               for dictionary in json_data}
        return res

    def validate_message(self, j, message, commitment):
        """returns True iff the message from A_j agrees with A_j's commitment"""
        assert len(commitment) == self.t + 1
        g = self.voting_curve.generator
        return g ** message == product(value ** (self.party_id ** i) for i, value in enumerate(commitment))

    def validate_all_messages(self):
        """returns True iff all the messages from the other parties
        agree with their commitments. Also computes and stores the secret value,
        and publishes commitment to secret value"""
        messages = self.get_messages()
        if len(messages) != self.n - 1:
            print("number of messages directed to me(%d) does not equal N-1(%d)" % (len(messages), self.n - 1))
            return False
        valid_messages = []
        if not self.other_commitments:
            self.retrieve_commitments()
        commitments = self.other_commitments
        all_valid = True  # flag signifies that all messages agree with commitments
        for j in messages:
            message = messages[j]
            commitment = commitments[j]
            if self.validate_message(j, message, commitment):
                valid_messages.append(message)
            else:
                print(
                    "message from party %d does not agree with it's commitment!" % j)
                all_valid = False

        if not all_valid:
            return False

        # compute s_i = f(i) and publish h_i = g^s_i
        assert len(valid_messages) == N - 1
        self.secret_value = (self.polynomial.value_at(self.party_id) + sum(valid_messages)) % self.voting_curve.order
        self.save_secret()
        g = self.voting_curve.generator
        self.publish_secret_commitment(g ** self.secret_value)
        return True

    def save_secret(self):
        """save secret value to file"""
        b_secret = list_to_bytes([self.secret_value], int_length=self.voting_curve.order.bit_length())
        with open(SECRET_FILE, 'wb') as f:
            f.write(b_secret)

    def load_secret(self):
        """load secret value from file"""
        try:
            with open(SECRET_FILE, 'rb') as f:
                b_secret = f.readall()
                self.secret_value = bytes_to_list(b_secret, member_length=self.voting_curve.order.bit_length())
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
        v = c ** r
        cc = self.hash_func(g, c, h, w, u, v)
        z = (r + cc * x) % G.order
        proof = ZKP(c, h, w, u, v, cc, z)
        return proof

    def publish_zkp(self, proof, vote_id, race_id):
        cert = self.sign(bytes(proof))
        base64_cert = bytes_to_base64(cert)
        base64_data = list_to_base64([proof], int_length=0)
        dictionary = {"vote_id": vote_id, "race_id": race_id, "party_id": self.party_id, "zkp": base64_data,
                      "signature": base64_cert}
        publish_dict(dictionary, BB_URL + PUBLISH_ZKP_TABLE)

    def generate_all_zkps(self, votes):
        """generate a zkp for every vote.
        votes is a list of dictionaries"""
        for vote_id in votes:
            c, d = votes[vote_id][0]
            race_id = votes[vote_id][1]
            proof = self.generate_zkp(c)
            self.publish_zkp(proof, vote_id, race_id)

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
        z = int.from_bytes(z, byteorder='little')  # Matching the BigInteger form in the java signing.
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
            int_length = self.sign_curve.int_length // 8
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
        second_part = list_to_bytes([self.cc, self.z], self.c.curve.int_length // 8)
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


def verify_certificate(public_key_first, public_key_second, encrypted_message, certificate):
    """
    :param publicKey:
    :param encrypted_message: text in 64 form (as sent by parties / voting booths)
    :param certificate: text in 64 form (as sent by parties / voting booths)
    :return: bool.
    """
    sys.stderr.write("\n" + certificate + "\n")
    certificate = base64_to_bytes(certificate)
    encrypted_message = base64_to_bytes(encrypted_message)
    publicKey = ECGroupMember(VOTING_CURVE, int(public_key_first), int(public_key_second))
    sign_curve = VOTING_CURVE
    int_length = sign_curve.int_length // 8
    l = bytes_to_list(certificate, int_length)
    r = l[0]
    s = l[1]
    if r < 1 or r > sign_curve.order:
        return False
    if s < 1 or s > sign_curve.order:
        return False
    m = hashlib.sha256()
    m.update(encrypted_message)
    e = m.digest()
    n = sign_curve.order
    ln = int(math.log(n))
    z = e[0:ln]
    z = int.from_bytes(z, byteorder='little')  # Matching the BigInteger form in the java signing.
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
        party_ids is a list of t integers, commitments is a dictionary,
        mapping party_id's to commitments,
        d is part of the cipher text - (c,d)
        performed after validating the ZKPs"""
    q = curve.order

    assert len(party_ids) == len(commitments) == T + 1
    lambdas = {}
    for j in party_ids:
        l = [(i * mod_inv((i - j) % q, q)) % q for i in party_ids if i != j]
        lambdas[j] = product(l, q)
    cs = product([commitments[j] ** lambdas[j] for j in party_ids])
    return d * cs ** -1


def validate_zkp(hash_func, g, proof):
    """returns True iff the zkp is valid"""
    c = proof.c
    h = proof.h
    w = proof.w
    u = proof.u
    v = proof.v
    cc = proof.cc
    z = proof.z
    return cc == hash_func(g, c, h, w, u, v) and u * h ** cc == g ** z and v * w ** cc == c ** z


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
SIGN_CURVE = curve_256
ZKP_HASH_FUNCTION = zkp_hash_func
T = 4  # number of parties needed for decryption minus 1
N = 7  # total number of parties
SLEEP_TIME = 1


def get_sign_key():
    """reads the private key used for signing from local file"""
    pass


def get_sign_curve():
    return SIGN_CURVE


def get_sent_commitments_confirmation():
    """returns True iff the BB finished computing the public key"""
    pass


def get_sent_messages_confirmation():
    """returns True iff the BB contains secret messages from every party"""
    pass


def get_votes(local=False):
    """returns all votes as a dictionary mapping vote_id to a tuple
    containing the encrypted group member and the race_id"""
    if local:
        bb_url = LOCAL_BB_URL
    else:
        bb_url = BB_URL
    json_data = get_bb_data(bb_url + GET_VOTES_TABLE + '/-1')
    result = {dictionary['vote_id']: (base64_to_list(dictionary['vote_value'], curve=VOTING_CURVE), dictionary['race_id'])
              for dictionary in json_data}
    return result


def get_commitments(local=False):
    """returns commitment to coefficients as a dictionary mapping j to j's commitment"""
    if local:
        bb_url = LOCAL_BB_URL
    else:
        bb_url = BB_URL
    json_data = get_bb_data(bb_url + GET_COMMITMENT_TABLE)
    return {dictionary['party_id']: base64_to_list(dictionary['commitment'], curve=VOTING_CURVE)
            for dictionary in json_data}


def get_secret_commitments(local=False):
    """returns commitment to secret value as a dictionary mapping j to j's commitment"""
    if local:
        bb_url = LOCAL_BB_URL
    else:
        bb_url = BB_URL
    json_data = get_bb_data(bb_url + GET_SECRET_COMMITMENT_TABLE)
    return {dictionary['party_id']: base64_to_list(dictionary['secret_commitment'], curve=VOTING_CURVE)[0]
            for dictionary in json_data}


def get_zkps(local=False):
    """returns all ZKPs as a dictionary mapping vote_id to a set of tuples,
    containing all published ZKP objects and party_id's for that vote"""
    if local:
        bb_url = LOCAL_BB_URL
    else:
        bb_url = BB_URL
    json_data = get_bb_data(bb_url + GET_ZKP_TABLE)
    result = defaultdict(set)
    for dictionary in json_data:
        proof = (base64_to_list(dictionary['zkp'], curve=VOTING_CURVE, is_zkp=True))[0]
        result[dictionary['vote_id']].add((proof, dictionary['party_id']))
    return result


def publish_voting_public_key(public_key, local=False):
    """updates the voting public key on the BB
    does not requires a certificate because it is local to the BB"""
    if local:
        bb_url = LOCAL_BB_URL
    else:
        bb_url = BB_URL
    base64_data = list_to_base64(public_key, int_length=0)
    dictionary = {"content": base64_data}
    publish_dict(dictionary, bb_url + PUBLISH_VOTING_PUBLIC_KEY_TABLE)


def compute_voting_public_key():
    """step 4 in threshold workflow - computes the voting public key from the commitments
    runs on the Bulletin Board"""
    commitments = get_commitments(local=True)
    public_key = product(coefficients[0] for coefficients in commitments.values())
    json_data = list_to_base64([public_key], int_length=0)
    dictionary = {"public_key": json_data}
    publish_dict(dictionary, LOCAL_BB_URL + PUBLISH_VOTING_PUBLIC_KEY_TABLE)
    return public_key


def decrypt_all_votes(votes, zkps, curve, secret_commitments):
    g = curve.generator
    decrypted_votes = []  # a list of tuples: (race_id, vote)

    for vote_id in votes:
        A = set()  # the set of valid zkps
        zkp_set = zkps[vote_id]
        for proof, party_id in zkp_set:
            if len(A) == T + 1:  # we got t + 1 valid parties, we can now decrypt the message
                break
            if validate_zkp(ZKP_HASH_FUNCTION, g, proof) and secret_commitments[party_id] == proof.h:
                A.add((proof, party_id))
        if len(A) < T + 1:
            print("Fatal Error: could not decrypt vote %d: not enough parties provided the required data" % vote_id)
            sys.exit()

        party_ids = [zkp[1] for zkp in A]
        commitments = {zkp[1]: zkp[0].w for zkp in A}
        d = votes[vote_id][0][1]
        decrypted_vote = decrypt_vote(curve, party_ids, commitments, d)
        race_id = votes[vote_id][1]
        decrypted_votes.append((race_id, decrypted_vote))
    return decrypted_votes


def print_results(decrypted_votes):
    result_file = open(RESULT_FILE, 'w')
    # from http://stackoverflow.com/a/2600813
    result_dict = defaultdict(lambda: defaultdict(int))
    for race_id, decrypted_vote in decrypted_votes:
        result_dict[race_id][str(decrypted_vote)] += 1
    
    result_file.write(dumps(result_dict))
    result_file.close()

    print(dumps(result_dict))


def phase1():
    """steps 1-8 in threshold workflow - voting can only begin after this phase ends successfully
    https://github.com/electronic-voting-wsorkshop-2015/electronic-voting-workshop-2015/wiki/Threshold-Cryptography"""
    print("initializing values of party")
    party_id = get_party_id_from_file()
    sign_key = get_private_key_from_file()
    sign_curve = get_sign_curve()
    party = ThresholdParty(VOTING_CURVE, T, N, party_id, ZKP_HASH_FUNCTION, sign_key, sign_curve, is_phase1=True)

    print("publishing commitment")
    party.publish_commitment()
    while True:
        if get_sent_commitments_confirmation():
            break
        sleep(SLEEP_TIME)
        print('.')  # gives an indication to user that work is being done

    print("sending secret values to other parties")
    party.send_values()
    while True:
        if get_sent_messages_confirmation():
            break
        sleep(SLEEP_TIME)
        print('.')

    print("validating messages from other parties")
    if not party.validate_all_messages():
        print("Fatal Error: one or more parties message's does not agree with commitment!!!")
        print("Aborting")
        sys.exit()

    print("phase 1 completed successfully - voting can now start!")
    sys.exit()


def get_party_id_from_file():
    for filename in os.listdir(os.getcwd() + MY_PRIVATE_KEY_PATH):
        if filename.startswith('privateKey_'):
            file = open(filename, 'r')
            file.readline()
            id = file.readline()
            return int(id)


def get_private_key_from_file():
    for filename in os.listdir(os.getcwd() + MY_PRIVATE_KEY_PATH):
        if filename.startswith('privateKey_'):
            file = open(filename, 'r')
            for i in range(3):
                file.readline()
            key = file.readline()
            return int(key)

def phase2():
    """step 10 in threshold workflow - run only after voting stopped
    https://github.com/electronic-voting-workshop-2015/electronic-voting-workshop-2015/wiki/Threshold-Cryptography"""
    print("initializing values of party")
    party_id = get_party_id_from_file()
    sign_key = get_private_key_from_file()
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
    votes = get_votes(local=True)

    print("retrieving zero knowledge proofs from the database")
    zkps = get_zkps(local=True)

    print("retrieving secret commitments from the database")
    secret_commitments = get_secret_commitments(local=True)

    print("verifying validity of zero knowledge proofs and decrypting")
    curve = VOTING_CURVE
    decrypted_votes = decrypt_all_votes(votes, zkps, curve, secret_commitments)

    print("the results are:")
    print_results(decrypted_votes)


def generate_keys(parties_number):
    """ generates a private key and saves it to a file. publishes public key to BB.
    """
    private_keys = []
    for party_id in range(1, parties_number + 1):
        rng = SystemRandom()
        private_key = rng.randint(2, VOTING_CURVE.order)
        while (private_key in private_keys):
            private_key = rng.randint(2, VOTING_CURVE.order)
        public_key = VOTING_CURVE.get_member(private_key)
        data = dict(party_id=party_id, first=str(public_key.x), second=str(public_key.y))
        publish_dict(data, LOCAL_BB_URL + PUBLISH_PUBLIC_KEY_TABLE_FOR_PARTIES)
        filename = PRIVATE_KEYS_PATH + 'privateKey_' + str(party_id) + '.txt'
        f = open(filename, 'w')
        f.writelines(["party id: \n", str(party_id) + "\n", "private key:\n", str(private_key) + "\n"])
        f.close()


########### Testing Functions ############
def shuffled(l):
    """returns a shuffled list, used for testing"""
    l2 = l[:]
    import random
    random.shuffle(l2)
    return l2


def encrypt_member(m, public_key):
    """encryptes the group member using ElGamal
    used for testing"""
    g = public_key.curve.generator
    r = public_key.curve.get_random_exponent()
    return g ** r, m * public_key ** r


def generate_votes(number_of_races, number_of_votes_for_each_race, party, voting_public_key):
    """publishes random votes to the BB, used for testing"""
    for vote_id in range(1, number_of_votes_for_each_race + 1):
        vote_list = []
        vote_string_list = []  # used for building the string for signing
        for race_id in range(1, number_of_races + 1):
            vote = VOTING_CURVE.generator ** vote_id
            encrypted_vote = encrypt_member(vote, voting_public_key)
            base64_encrypted_vote = list_to_base64(encrypted_vote, int_length=0)
            vote_dict = {"vote_value": base64_encrypted_vote}
            vote_list.append(vote_dict)
            vote_string_list.append(repr(vote_dict))
        bytes_signature = party.sign(base64_to_bytes(vote_list[0]["vote_value"]))
        base64_signature = bytes_to_base64(bytes_signature)
        dictionary = {"ballot_box": 1, "SerialNumber": vote_id, "votes": vote_list, "signature": base64_signature}
        publish_dict(dictionary, LOCAL_BB_URL + SEND_VOTE_TABLE)


def test():
    print("phase 1")
    sign_curve = VOTING_CURVE
    generate_keys(N)
    sign_keys = []
    for i in range(1, N+1):
        file = open('privateKey_%d.txt' % i)
        for j in range(3):
            file.readline()
        sign_keys.append(int(file.readline()))
    parties = [ThresholdParty(VOTING_CURVE, T, N, i, ZKP_HASH_FUNCTION, sign_keys[i - 1], sign_curve, is_phase1=True)
               for i in range(1, N + 1)]

    for party in shuffled(parties):
        party.publish_commitment()
    for party in shuffled(parties):
        party.send_values()
    for party in shuffled(parties):
        party.validate_all_messages()

    print("phase 2")
    voting_public_key = compute_voting_public_key()

    generate_votes(2, 2, parties[0], voting_public_key)
    generate_votes(3, 3, parties[0], voting_public_key)
    votes = get_votes()

    for party in shuffled(parties):
        party.generate_all_zkps(votes)

    print("phase 3")
    zkps = get_zkps(local=True)

    secret_commitments = get_secret_commitments(local=True)

    curve = VOTING_CURVE
    decrypted_votes = decrypt_all_votes(votes, zkps, curve, secret_commitments)

    print("the results are:")
    print_results(decrypted_votes)
