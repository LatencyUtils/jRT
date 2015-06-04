#!/bin/bash

die() {
	echo $*
	exit 1
}

ITSELF_PATH=$(dirname $(readlink -f $0))
TEST=$ITSELF_PATH/tmp
JRT=$ITSELF_PATH/../jRT.jar

file $JRT 2>&1 >/dev/null || die "jRT was not built yet, please build it"


echo "Test run"

rm -rf tmp
mkdir -p tmp
(cd tmp && cp -r $ITSELF_PATH/../jRTTest/src/jrttest/ ./)
(cd tmp && javac jrttest/jRTTest.java)
export _JAVA_OPTIONS="-javaagent:$JRT=-start=0,-si=100"
(cd tmp && java -cp . jrttest.jRTTest -t:1999 -i:1999:500 -i:1999:540 &> test.log)

echo "Analysis..."

file $TEST/*hlog 2>&1 >/dev/null || die "There are no *.hlog files!"

LOGS=$(for i in `ls $TEST/*hlog` ; do for j in $(cat $i | grep -v Start | awk -F , '{print $3}') ; do if [ "$j" != "0.000" ] ; then echo "$i" ; fi ; done; done | sort | uniq | wc -l)

[ "0" == "$LOGS" ] && die "All *.hlog files contains only 0.000 !!! test [FAILED]"

echo "..DONE"


