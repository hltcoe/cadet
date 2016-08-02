#!/bin/bash

DIR=`dirname $0`
JAR=$(find $DIR/target/ -name 'cadet-tools-fat*.jar')
java -cp .:$JAR edu.jhu.hlt.cadet.SearchTool "$@"

