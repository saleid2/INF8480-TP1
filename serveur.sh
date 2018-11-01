#!/bin/bash

pushd $(dirname $0) > /dev/null
basepath=$(pwd)
popd > /dev/null

cat << EndOfMessage
HELP: 
./serveur.sh directoryHostname maliciousRate capacity
    - directoryHostname : adresse IP du service de nom
    - maliciousRate : taux d'introduction de donnee malicieuse (valeur entre 0 et 100)
    - capacity : capacite du serveur

EndOfMessage

java -cp "$basepath"/build/serveur.jar:"$basepath"/build/iserveur.jar: -Djava.security.policy="$basepath"/policy Serveur $*
