#!/bin/bash

die() {
	echo $*
	exit 1
}

ITSELF_PATH=$(dirname $(readlink -f $0))
IOHICCUP=$ITSELF_PATH/../ioHiccup.jar
TESTPATH=$ITSELF_PATH/./tmp/
TEST=$TESTPATH/clj-async-tcp-echo-nio.2
LEIN=lein

which lein 2>&1 > /dev/null || die "There are no Leiningen in PATH, please install it following next instructures http://leiningen.org"
lein -version 2>&1 | grep -q 'Leiningen 2' || die "Leiningen of wrong version $(lein -version), please install exactly 2.* version"
which git 2>&1 > /dev/null || die "There are no git in PATH, please install it"
file $IOHICCUP 2>&1 >/dev/null || die "ioHiccup was not built yet, please build it"

echo "Download test project"
(
[ -e $TEST ] && rm -rf $TEST
mkdir -p $TESTPATH
cd $TESTPATH
git clone https://github.com/bluemont/clj-async-tcp-echo-nio.2
cd clj-async-tcp-echo-nio.2
)

echo "Test run"
#export _JAVA_OPTIONS="-javaagent:$IOHICCUP=-start=0,-si=100,-lport=9500,-rport=9500"
(
export _JAVA_OPTIONS="-javaagent:$IOHICCUP=-start=0,-si=100"
cd $TEST
lein test
)

echo "Analysis..."

file $TEST/*hlog 2>&1 >/dev/null || die "There are no *.hlog files!"

LOGS=$(for i in `ls $TEST/*hlog` ; do for j in $(cat $i | grep -v Start | awk -F , '{print $3}') ; do if [ "$j" != "0.000" ] ; then echo "$i" ; fi ; done; done | sort | uniq | wc -l)

[ "0" == "$LOGS" ] && die "All *.hlog files contains only 0.000 !!! test [FAILED]"

echo "..DONE"

