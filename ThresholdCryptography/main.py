from Crypto.Crypto import test, phase1, phase2, phase3, verify_certificate, compute_voting_public_key, generate_keys  # TODO: fix import error in PyCharm
import sys


def main():
    if len(sys.argv) < 2:
        print("not enough arguments")
        sys.exit()
    elif sys.argv[1] == "compute_voting_public_key":
        compute_voting_public_key()
    elif sys.argv[1] == "test":
        test()
    elif sys.argv[1] == "phase1":
        phase1()
    elif sys.argv[1] == "phase2":
        phase2()
    elif sys.argv[1] == "phase3":
        phase3()
    elif sys.argv[1] == "generateKeys":
        parties_number = int(sys.argv[2])
        generate_keys(parties_number)
    elif sys.argv[1] == "verifyCertificate":
        public_key_first = sys.argv[2]
        public_key_second = sys.argv[3]
        encrypted_message = sys.argv[4]
        certificate = sys.argv[5]
        if verify_certificate(public_key_first, public_key_second, encrypted_message, certificate):
            print("true",end="")
        else:
            print("false",end="")
    else:
        print("Error: argument should be one of: phase1, phase2, phase3, test, verifyCertificate")

main()
