#!/bin/bash

for j in ./ $@
do
	for i in "$j"/*.hlog
	do
		"$(dirname $0)"/jRTLogProcessor -i "$i" -o "${i/.hlog/.log}"
	done
done

