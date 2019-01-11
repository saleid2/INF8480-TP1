#!/bin/bash

pushd $(dirname $0) > /dev/null
basepath=$(pwd)
popd > /dev/null

cat << EndOfMessage
HELP: 
./repartiteur.sh user pass pathToFile directoryHostname isSecuredMode taux
    - user : nom utilisateur
    - pass : mot de passe
    - pathToFile : chemin vers le fichier de calcul
    - directoryHostname : adresse ip du service de nom
    - taux : taux de refus entre 0 et 1
    - isSecuredMode : mode de securite

EndOfMessage

java -cp "$basepath"/build/repartiteur.jar:"$basepath"/build/irepartiteur.jar:"$basepath"/build/iserveur.jar:"$basepath"/build/idirectory.jar: -Djava.security.policy="$basepath"/policy Repartiteur $*
