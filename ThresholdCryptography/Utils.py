import json
import base64
import urllib.request


BB_API_ERROR = "error"  # TODO: define error message from BB

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


def publish_list(l, int_length, sender_id, signature, table_id, recipient_id, url):
    """publishes list l to url using POST request"""
    base64_data = list_to_base64(l, int_length)
    json_data = json.dumps([sender_id, base64_data, signature, table_id, recipient_id])
    req = urllib.request.Request(url, json_data, {'Content-Type': 'application/json'})
    with urllib.request.urlopen(req) as response:
        read_value = response.read()  # read_value is a bytes object
    if read_value.decode('utf-8') == BB_API_ERROR:
        return None
