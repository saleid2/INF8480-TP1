#!/bin/bash

pushd $(dirname $0) > /dev/null
basepath=$(pwd)
popd > /dev/null

cat << EndOfMessage
HELP: 
./client.sh remote_server_ip command username password filename
	- remote_server_ip: l'addresse ip du serveur distant
	- command: commande à exécuter
	- username: nom d'utilisateur pour le serveur d'authentification
	- password: mot de passe pour le serveur d'authentification
	- filename: (OPTIONAL) nom du fichier à manipuler

EndOfMessage

java -cp "$basepath"/client.jar:"$basepath"/iauthserver.jar:"$basepath"/ifileserver.jar -Djava.security.policy="$basepath"/policy ca.polymtl.inf8480.tp1.partie2.client.Client $*
