#!/bin/sh -x

mvn assembly:assembly
mvn assembly:assembly
cp target/ioHiccup.jar ./

