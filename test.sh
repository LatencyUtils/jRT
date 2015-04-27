#!/bin/sh -x

javac ioHiccupTest/src/iohiccuptest/IoHiccupTest.java
java -cp ioHiccupTest/src/ iohiccuptest.IoHiccupTest -t:1999 -i:1999:500 -i:1999:540 &> test.log

