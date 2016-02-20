from random import SystemRandom
from Utils import bits, product, mod_inv


class EllipticCurve:
    """ a curve of the form y^2 = x^3+ax+b (mod p)
    self.order is the order of the generator g
    """

    def __init__(self, a, b, p, order):
        self.a = a
        self.b = b
        self.p = p
        self.order = order
        self.generator = None  # assigned when calling setGenerator

    def setGenerator(self, generator):
        self.generator = generator

    def getGenerator(self):
        return self.generator

    def getZeroMember(self):
        return ECGroupMember(self, 0, 0)

    def getRandomMember(self):
        return self.generator ** self.getRandomExponent()

    def getRandomExponent(self):
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
        if self == self.curve.getZeroMember():
            return g
        if g == self.curve.getZeroMember():
            return self
        if self.x == g.x and self.y == -g.y:
            return self.curve.getZeroMember()

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
        res = self.curve.getZeroMember()
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

    @classmethod
    def to_group_member(cls, value):
        """converts integer "value" to an ECCGroupMember object"""
        pass


class ThresholdParty:
    """represents a member participating in the decryption process
    n is the number of parties, t is the number of parties required to decrypt
    algorithms from: http://moodle.tau.ac.il/pluginfile.php/217242/mod_resource/content/1/Tomer.pdf
    """

    def __init__(self, curve, t, n, party_id, hash_func):
        self.curve = curve
        self.t = t
        self.n = n
        self.party_id = party_id  # a number between 0 and n-1
        self.polynomial = Polynomial([curve.getRandomExponent() for _ in range(t)], curve.order)
        self.secret_value = None  # the value f(j), assigned after calling validate_all_messages
        self.hash_func = hash_func  # the cryptographic hash function used for ZKP

    def publish_commitment(self):
        """publish the values g^v_i,g^a_i1...,g^a_it to the BB"""
        for coefficient in self.polynomial.coefficients:
            self.publish_value(coefficient)

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
        """returns encrypted value using public_key"""
        g = self.curve.getGenerator()
        r = self.curve.getRandomExponent()
        m = ECGroupMember.to_group_member(value)  # TODO:convert integer "value" to an ECGroupMember
        return g ** r, m * (public_key ** r)

    def decrypt_message(self, private_key, cipher_text):
        g = self.curve.getGenerator()
        c = cipher_text[0]
        d = cipher_text[1]
        x = private_key
        s = c**x
        message = d * s**-1
        return message

    def send_message(self, j):
        """send f_i(j) to party A_j"""
        message = self.polynomial.value_at(j)
        public_key = self.get_public_key(j)
        cipher_text = self.encrypt_message(public_key, message)
        # TODO:publish message to the BB

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
        g = self.curve.getGenerator()
        return message == g**exponent

    def validate_all_messages(self):  # TODO: better method name?
        messages = []
        for j in range(self.n):
            if j == self.party_id:
                continue
            message = self.get_message(j)
            commitment = self.get_commitment(j)
            if self.validate_message(j, message, commitment):
                messages.append(message)
            else:
                pass  # TODO: handle message not agreeing with commitment

        # compute s_i = f(i) and publish h_i = g^s_i
        self.secret_value = sum(self.polynomial.value_at(x) for x in messages) % self.curve.order
        g = self.curve.getGenerator()
        self.publish_value(g ** self.secret_value)

    def generate_zkp(self, c):
        """variable names follow Tomer page 4 and page 5 bottom.
        c is part of the message: m=(c,d). cc is the 'random' challenge"""
        x = self.secret_value
        w = c ** x
        g = self.curve.getGenerator()
        h = g ** x
        G = self.curve
        r = G.getRandomExponent()
        u = g ** r
        v = h ** r
        cc = self.hash_func(G, g, c, h, w, u, v)
        z = (r + c * x) % G.order
        self.publish_zkp(c, h, w, u, v, cc, z)

    def publish_zkp(self, c, h, w, u, v, cc, z):
        pass  # TODO:publish zkp to the BB

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
            l = ((i * mod_inv(i-j, q)) % q for i in party_ids if i != j)
            lambda_list.append(product(l, q))
        cs = product(commitments[j] ** lambda_list[j] for j in party_ids)
        return d * cs**-1





class Polynomial:
    """represents a degree t polynomial in the group F_order as a list of t+1 coefficients"""

    def __init__(self, coefficients, order):
        self.coefficients = coefficients
        self.order = order

    def value_at(self, x):
        """returns the value of the polynomial at point x"""
        return sum(c[1] * x ** c[0] for c in enumerate(self.coefficients)) % self.order


# recommended NIST elliptic curves: http://csrc.nist.gov/groups/ST/toolkit/documents/dss/NISTReCur.pdf
_p = 6277101735386680763835789423207666416083908700390324961279
_r = 6277101735386680763835789423176059013767194773182842284081
_b = 0x64210519e59c80e70fa7e9ab72243049feb8deecc146b9b1
_Gx = 0x188da80eb03090f67cbf20eb43a18800f4ff0afd82ff1012
_Gy = 0x07192b95ffc8da78631011ed6b24cdd573f977a11e794811
curve_192 = EllipticCurve(-3, _b, _p, _r)
g_192 = ECGroupMember(curve_192, _Gx, _Gy)
curve_192.setGenerator(g_192)

_p = 26959946667150639794667015087019630673557916260026308143510066298881
_r = 26959946667150639794667015087019625940457807714424391721682722368061
_b = 0xb4050a850c04b3abf54132565044b0b7d7bfd8ba270b39432355ffb4
_Gx = 0xb70e0cbd6bb4bf7f321390b94a03c1d356c21122343280d6115c1d21
_Gy = 0xbd376388b5f723fb4c22dfe6cd4375a05a07476444d5819985007e34
curve_224 = EllipticCurve(-3, _b, _p, _r)
g_224 = ECGroupMember(curve_224, _Gx, _Gy)
curve_224.setGenerator(g_224)

_p = 115792089210356248762697446949407573530086143415290314195533631308867097853951
_r = 115792089210356248762697446949407573529996955224135760342422259061068512044369
_b = 0x5ac635d8aa3a93e7b3ebbd55769886bc651d06b0cc53b0f63bce3c3e27d2604b
_Gx = 0x6b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296
_Gy = 0x4fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb6406837bf51f5
curve_256 = EllipticCurve(-3, _b, _p, _r)
g_256 = ECGroupMember(curve_256, _Gx, _Gy)
curve_256.setGenerator(g_256)


def test():
    g1 = curve_256.getRandomMember()
    g2 = curve_256.getRandomMember()
    g3 = g1 * g2
    g4 = g1 ** (2)
    print(ECGroupMember.verify_point(g1.x, g1.y, curve_256))
    print(ECGroupMember.verify_point(g2.x, g2.y, curve_256))
    print(ECGroupMember.verify_point(g3.x, g3.y, curve_256))
    print(ECGroupMember.verify_point(g3.x, g3.y, curve_256))
    print(g1)
    print(g2)
    print(g3)
    print(g4)


test()
