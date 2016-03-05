from Crypto.Crypto import test, phase1, phase2, phase3, verify_certificate  # TODO: fix import error in PyCharm
import sys
import cProfile


def main():
    if len(sys.argv) < 2:
        print("not enough arguments")
        sys.exit()
    if sys.argv[1] == "test":
        test()
    elif sys.argv[1] == "phase1":
        phase1()
    elif sys.argv[1] == "phase2":
        phase2()
    elif sys.argv[1] == "phase3":
        phase3()
    elif sys.argv[1] == "verifyCertificate":
        publicKey = sys.argv[2]
        encrypted_message = sys.argv[3]
        certificate = sys.argv[4]
        if verify_certificate(publicKey, encrypted_message, certificate):
            print("true")
        else:
            print("false")
    else:
        print("Error: argument should be one of: phase1, phase2, phase3, test, verifyCertificate")


cProfile.run('main()')
#main()