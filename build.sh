#!/bin/sh -x

rm -rf target
mvn assembly:assembly
cp target/ioHiccup.jar ./

