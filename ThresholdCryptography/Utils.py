import math
import random
from urllib.request import *
import json
import base64
from itertools import islice


def bits(n):
    """Generates binary digits of n, starting from least significant bit.
    from http://andrea.corbellini.name/2015/05/17/elliptic-curve-cryptography-a-gentle-introduction/
    """
    while n:
        yield n & 1
        n >>= 1


def split_every(n, iterable):
    """from http://stackoverflow.com/a/1915307"""
    for i in range(0, len(iterable), n):
        yield iterable[i:i+n]


def product(l, p = 0):
    """computes product of list, mod p if second passed second argument"""
    res = 1
    if p == 0:
        for i in l:
            res *= i
    else:
        for i in l:
            res *= i % p
    return res


def mod_inv(a, p):
    """computes modular inverse of a in field F_p"""
    return pow(a, p-2, p)  # mod inverse: http://stackoverflow.com/a/4798776


def list_to_bytes(l, int_length = 0):
    """returns bytes object formed from concatenating members of list l"""
    res = bytes(0)  # empty string of bytes
    for i in l:
        if isinstance(i, int):
            res += i.to_bytes(int_length, 'little')
        else:  # object is ECGroupMember
            res += bytes(i)
    return res


def bytes_to_list(b, member_length=0, curve=None):
    """member length is the size in bytes of each member in the list
    list is either of ints (curve=None) or of ECGroupMembers (member_length=None)"""
    from Crypto import ECGroupMember
    res = []
    if member_length == 0:  #
        member_length = 2 * curve.p.bit_length() // 8
    for i in split_every(member_length, b):
        if isinstance(i, int):
            res += int.from_bytes(i, 'little')
        else:  # object is ECGroupMember
            res.append(ECGroupMember.from_bytes(i, curve))
    return res


def bytes_to_base64(b):
    """converts bytes object to base64 string"""
    return base64.standard_b64encode(b).decode('utf-8')


def base64_to_bytes(b64):
    """converts base64 string to bytes object"""
    return base64.standard_b64decode(b64.encode('utf-8'))


def list_to_base64(l, int_length):
    return bytes_to_base64(list_to_bytes(l, int_length))


def base64_to_list(b, member_length=0, curve=None):
    return bytes_to_list(base64_to_bytes(b), member_length, curve)


def publish_list(l, int_length, url):
    """publishes list l to url using POST request"""
    base64_data = list_to_base64(l, int_length)


def rabinMiller(n):
    """https://langui.sh/2009/03/07/generating-very-large-primes/#fn:1"""
    s = n - 1
    t = 0
    while s & 1 == 0:
        s = s // 2
        t += 1
    k = 0
    while k < 128:
        a = random.randrange(2, n - 1)
        # a^s is computationally infeasible.  we need a more intelligent approach
        # v = (a**s)%n
        # python's core math module can do modular exponentiation
        v = pow(a, s, n)  # where values are (num,exp,mod)
        if v != 1:
            i = 0
            while v != (n - 1):
                if i == t - 1:
                    return False
                else:
                    i = i + 1
                    v = (v ** 2) % n
        k += 2
    return True


def isPrime(n):
    # lowPrimes is all primes (sans 2, which is covered by the bitwise and operator)
    # under 1000. taking n modulo each lowPrime allows us to remove a huge chunk
    # of composite numbers from our potential pool without resorting to Rabin-Miller
    lowPrimes = [3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97
        , 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179
        , 181, 191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251, 257, 263, 269
        , 271, 277, 281, 283, 293, 307, 311, 313, 317, 331, 337, 347, 349, 353, 359, 367
        , 373, 379, 383, 389, 397, 401, 409, 419, 421, 431, 433, 439, 443, 449, 457, 461
        , 463, 467, 479, 487, 491, 499, 503, 509, 521, 523, 541, 547, 557, 563, 569, 571
        , 577, 587, 593, 599, 601, 607, 613, 617, 619, 631, 641, 643, 647, 653, 659, 661
        , 673, 677, 683, 691, 701, 709, 719, 727, 733, 739, 743, 751, 757, 761, 769, 773
        , 787, 797, 809, 811, 821, 823, 827, 829, 839, 853, 857, 859, 863, 877, 881, 883
        , 887, 907, 911, 919, 929, 937, 941, 947, 953, 967, 971, 977, 983, 991, 997]
    if (n >= 3):
        if (n & 1 != 0):
            for p in lowPrimes:
                if (n == p):
                    return True
                if (n % p == 0):
                    return False
            return rabinMiller(n)
    return False


def generateLargePrime(k):
    # k is the desired bit length
    r = 100 * (math.log(k, 2) + 1)  # number of attempts max
    r_ = r
    while r > 0:
        # randrange is mersenne twister and is completely deterministic
        # unusable for serious crypto purposes
        n = random.randrange(2 ** (k - 1), 2 ** (k))
        r -= 1
        if isPrime(n) == True:
            return n
    return "Failure after " + repr(r_) + " tries."

