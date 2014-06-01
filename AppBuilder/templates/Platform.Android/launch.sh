#!/bin/sh

ant clean
ant debug install

monkeyrunner launch.py 