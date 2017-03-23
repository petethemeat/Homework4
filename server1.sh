#!/bin/sh

javac src/*.java
java -cp src Server

1 2 input/inventory.txt
localhost:8025
localhost:8030
