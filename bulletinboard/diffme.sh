#!/bin/sh
git diff $@ | stripgitdiff.py > diff
