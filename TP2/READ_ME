Nous avons 3 modules à exécuter dans un certain ordre:
    1- le service de nom (Directory)
    2- tous les serveurs de calcul
    3- le répartiteur qui sert également comme un client


1- Pour rouler le service de nom, exécutez la commande dans le dossier où se trouve le fichier:
    ./directory.sh


2- Pour rouler un serveur de calcul, exécutez la commande dans le dossier où se trouve le fichier:

    ./serveur.sh <directoryHostname> <maliciousRate> <capacity>
        - directoryHostname : adresse IP du service de nom
        - maliciousRate : taux d'introduction de donnée malicieuse (valeur entre 0 et 100)
        - capacity : capacité du serveur


3- Pour rouler le repartiteur, exécutez la commande dans le dossier où se trouve le fichier:

    ./repartiteur.sh <user> <pass> <pathToFile> <directoryHostname> <isSecuredMode>
        - user : nom utilisateur
        - pass : mot de passe
        - pathToFile : chemin vers le fichier de calcul
        - directoryHostname : adresse ip du service de nom
        - taux : taux de refus souhaite (valeur entre 0 et 1)
        - isSecuredMode : mode de securité (true ou false)


On doit d'abord modifier le port d'écoute de rmiregistry en exécutant la commande dans le dossier bin :
    /opt/java/jdk8.x86_64/bin/rmiregistry 5001

il faut utiliser 5001, car nous avons hardcodé le port pour getRegistry()

Executer le code sur une machine a distance

1- Connecter sur une autre machine avec la commande:
    ssh -x user@l4712-xx.info.polymtl.ca

2- Executer la commande /opt/java/jdk8.x86_64/bin/rmiregistry 5001 & dans le dossier bin

4- Executer les scripts desires



