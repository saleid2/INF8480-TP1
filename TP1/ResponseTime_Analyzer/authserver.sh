#!/bin/bash

pushd $(dirname $0) > /dev/null
basepath=$(pwd)
popd > /dev/null

cat << EndOfMessage
HELP: 
./authserver.sh ip_address
	- ip_address: L'addresse ip du serveur.

EndOfMessage

IPADDR=$1
if [ -z "$1" ]
  then
    IPADDR="127.0.0.1"
fi

java -cp "$basepath"/authserver.jar:"$basepath"/iauthserver.jar \
  -Djava.rmi.server.codebase=file:"$basepath"/iauthserver.jar \
  -Djava.security.policy="$basepath"/policy \
  -Djava.rmi.server.hostname="$IPADDR" \
  ca.polymtl.inf8480.tp1.partie2.authserver.AuthServer
