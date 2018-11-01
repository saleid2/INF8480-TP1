#!/bin/bash

pushd $(dirname $0) > /dev/null
basepath=$(pwd)
popd > /dev/null

cat << EndOfMessage
HELP: 
./directory.sh

EndOfMessage

java -cp "$basepath"/build/directory.jar:"$basepath"/build/idirectory.jar: -Djava.security.policy="$basepath"/policy Directory $*
