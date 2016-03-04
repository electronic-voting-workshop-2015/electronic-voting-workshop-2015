from Crypto.Crypto import test, phase1, phase2, phase3  # TODO: fix import error in PyCharm
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
    else:
        print("Error: argument should be one of: phase1, phase2, phase3, test")


cProfile.run('main()')
#main()