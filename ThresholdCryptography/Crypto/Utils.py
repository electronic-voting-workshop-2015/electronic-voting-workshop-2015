import json
import base64
import urllib.request

# gmpy2 needs to be installed: https://code.google.com/archive/p/gmpy/wikis/InstallingGmpy2.wiki
# makes mod_inv computation much faster
gmpy2_installed = True
try:
    from gmpy2 import invert, mpz, powmod
except ImportError:
    gmpy2_installed = False

BB_API_ERROR = "invalid signature"  # TODO: BB should return this string on certificate error

def bits(n):
    """Generates binary digits of n, starting from least significant bit.
    from http://andrea.corbellini.name/2015/05/17/elliptic-curve-cryptography-a-gentle-introduction/
    """
    while n:
        yield n & 1
        n >>= 1


def split_every(n, iterable):
    """from http://stackoverflow.com/a/312464"""
    for i in range(0, len(iterable), n):
        yield iterable[i:i+n]


def least_significant(num, n):
    """returns the least significant (rightmost) n bits of num"""
    mask = 2**n - 1
    return num & mask


def most_significant(num, return_size, total_size):
    """returns the most significant (leftmost) return_size bits of num,
    num is interpreted as a total_size bits number (zeroes are appended to the left if necessary"""
    mask = (2**total_size - 1) * 2**(total_size-return_size)
    return (num & mask) // (2 ** (total_size-return_size))


def concat_bits(a, b, b_len):
    """returns an a_len+b_len bits number formed from concatenating the binary
    representations of a and b"""
    return a * 2**b_len + b


def product(l, p=0):
    """computes product of iterator, mod p if second argument is passed"""
    iterlist = iter(l)
    res = next(iterlist)
    if p == 0:
        for i in iterlist:
            res *= i
    else:
        for i in iterlist:
            res *= i
            res %= p
    return res


def extended_gcd(a, p):
    lastremainder, remainder = abs(a), abs(p)
    x, lastx, y, lasty = 0, 1, 1, 0
    while remainder:
        lastremainder, (quotient, remainder) = remainder, divmod(lastremainder, remainder)
        x, lastx = lastx - quotient*x, x
        y, lasty = lasty - quotient*y, y
    return lastremainder, lastx * (-1 if a < 0 else 1), lasty * (-1 if p < 0 else 1)


def mod_inv_slow(a, p):
    """computes modular inverse of a in field F_p
    from https://rosettacode.org/wiki/Modular_inverse#Python"""
    g, x, y = extended_gcd(a, p)
    if g != 1:
        print(a, p)
        raise Exception('modular inverse does not exist')
    else:
        return x % p


def mod_inv_fast(a, p):
    """computes modular inverse of a in field F_p
    using gmpy2 library"""
    return int(invert(a, p))

if gmpy2_installed:
    mod_inv = mod_inv_fast
else:
    mod_inv = mod_inv_slow


def list_to_bytes(l, int_length = 0):
    """returns bytes object formed from concatenating members of list l
    int_length is the length in bytes of every integer"""
    res = bytearray(0)  # empty string of bytes
    for i in l:
        if isinstance(i, int):
            res += i.to_bytes(int_length, 'little')
        elif gmpy2_installed and isinstance(i, mpz().__class__):
            res += int(i).to_bytes(int_length, 'little')
        else:  # object is ECGroupMember or ZKP
            res += bytes(i)
    return bytes(res)


def bytes_to_list(b, member_length=0, curve=None, is_zkp=False):
    """member length is the size in bytes of each member in the list
    list is either of ints (curve=None) or of ECGroupMembers (member_length=None), or ZKPs (is_zkp=True)"""
    from .Crypto import ECGroupMember, ZKP
    res = []
    if is_zkp:
        member_length = 12 * curve.p.bit_length() // 8
    if member_length == 0:  #
        member_length = 2 * curve.p.bit_length() // 8
    for i in split_every(member_length, b):
        if curve is None:  # object in integer
            res.append(int.from_bytes(i, 'little'))
        elif not is_zkp:  # object is ECGroupMember
            res.append(ECGroupMember.from_bytes(i, curve))
        else:  # object is ZKP
            res.append(ZKP.from_bytes(i, curve))
    return res


def bytes_to_base64(b):
    """converts bytes object to base64 string"""
    return base64.standard_b64encode(b).decode('utf-8')


def base64_to_bytes(b64):
    """converts base64 string to bytes object"""
    return base64.standard_b64decode(b64.encode('utf-8'))


def list_to_base64(l, int_length):
    """converts list of objects (or single object) to base64 string"""
    return bytes_to_base64(list_to_bytes(l, int_length))


def base64_to_list(b, member_length=0, curve=None, is_zkp=False):
    return bytes_to_list(base64_to_bytes(b), member_length, curve, is_zkp)


def publish_dict(dict, url):
    """
    :param dict: the dictionary to publish.
    :param url: the url to publish to.
    :return: TODO
    """
    req = urllib.request.Request(url)
    req.add_header('Content-Type', 'application/json; charset=utf-8')
    jsondata = json.dumps(dict)
    jsondataasbytes = jsondata.encode('utf-8')   # needs to be bytes
    req.add_header('Content-Length', len(jsondataasbytes))
    with urllib.request.urlopen(req, jsondataasbytes) as response:
        read_value = response.read()  # read_value is a bytes object
    if read_value.decode('utf-8') == BB_API_ERROR:
        raise Exception("Invalid Certificate")


def get_bb_data(url):
    response = urllib.request.urlopen(url)
    data = json.loads(response.read().decode('utf-8'))
    return data


def mod_sqrt(a, p):
    """ Find a quadratic residue (mod p) of 'a'. p
        must be an odd prime.

        Solve the congruence of the form:
            x^2 = a (mod p)
        And returns x. Note that p - x is also a root.

        0 is returned is no square root exists for
        these a and p.

        The Tonelli-Shanks algorithm is used (except
        for some simple cases in which the solution
        is known from an identity). This algorithm
        runs in polynomial time (unless the
        generalized Riemann hypothesis is false).

        from http://eli.thegreenplace.net/2009/03/07/computing-modular-square-roots-in-python
    """
    # Simple cases
    #
    if legendre_symbol(a, p) != 1:
        return 0
    elif a == 0:
        return 0
    elif p == 2:
        return p
    elif p % 4 == 3:
        return pow(a, (p + 1) // 4, p)

    # Partition p-1 to s * 2^e for an odd s (i.e.
    # reduce all the powers of 2 from p-1)
    #
    s = p - 1
    e = 0
    while s % 2 == 0:
        s //= 2
        e += 1

    # Find some 'n' with a legendre symbol n|p = -1.
    # Shouldn't take long.
    #
    n = 2
    while legendre_symbol(n, p) != -1:
        n += 1

    # Here be dragons!
    # Read the paper "Square roots from 1; 24, 51,
    # 10 to Dan Shanks" by Ezra Brown for more
    # information
    #

    # x is a guess of the square root that gets better
    # with each iteration.
    # b is the "fudge factor" - by how much we're off
    # with the guess. The invariant x^2 = ab (mod p)
    # is maintained throughout the loop.
    # g is used for successive powers of n to update
    # both a and b
    # r is the exponent - decreases with each update
    #
    x = pow(a, (s + 1) // 2, p)
    b = pow(a, s, p)
    g = pow(n, s, p)
    r = e

    while True:
        t = b
        m = 0
        for m in range(r):
            if t == 1:
                break
            t = pow(t, 2, p)

        if m == 0:
            return x

        gs = pow(g, 2 ** (r - m - 1), p)
        g = (gs * gs) % p
        x = (x * gs) % p
        b = (b * g) % p
        r = m


def legendre_symbol(a, p):
    """ Compute the Legendre symbol a|p using
        Euler's criterion. p is a prime, a is
        relatively prime to p (if p divides
        a, then a|p = 0)

        Returns 1 if a has a square root modulo
        p, -1 otherwise.
    """
    ls = pow(a, (p - 1) // 2, p)
    return -1 if ls == p - 1 else ls
