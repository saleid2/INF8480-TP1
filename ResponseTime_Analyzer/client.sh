#!/bin/bash

pushd $(dirname $0) > /dev/null
basepath=$(pwd)
popd > /dev/null

cat << EndOfMessage
HELP: 
./client.sh remote_server_ip command username password filename
	- remote_server_ip: l'addresse ip du serveur distant
	- command: commande a executer
	- username: username pour le authserver
	- password: mot de passe pour le authserver
	- filename: (OPTIONAL) nom du fichier a manipuler

EndOfMessage

java -cp "$basepath"/client.jar:"$basepath"/shared.jar -Djava.security.policy="$basepath"/policy ca.polymtl.inf8480.tp1.partie2.client.Client $*
