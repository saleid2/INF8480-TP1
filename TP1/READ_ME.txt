L'addresse ip de notre serveur distant est 132.207.12.110

Si les fichiers sources ne sont pas disponible dans le serveur distant, suivre ces étapes :
    1- Connecter au serveur distant avec la commande :
        ssh -i key-tp1.pem ubuntu@132.207.12.110

    2- Envoyer le dossier ResponseTime_Analyzer dans le serveur distant sous le nom pt2 :
        scp -i key-tp1-pem ResponseTime_Analyzer ubuntu@132.207.12.110:pt2

1- Démarrer le système d'authentification sur le serveur distant.
	Pour se connecter au serveur distant, à partir du dossier INF8480-TP1 où la clé key-tp1.pem se trouve, exécuter la commande :
		ssh -i key-tp1.pem ubuntu@132.207.12.110

	À partir du dossier bin, exécuter la commande :
		rmiregistry&

	Démarrer le serveur d'authentification avec le script authserver.sh en exécutant la commande :
		./authserver.sh 132.207.12.110

2- Démarrer le système de fichier sur le serveur local.
	À partir du dossier bin dans la machine locale, dans un autre terminal, enregistrer dans le registre avec la commande :
         rmiregistry&
    
    démarrer le serveur de fichier avec le script fileserver.sh en exécutant la commande :
		./fileserver.sh 127.0.0.1 132.207.12.110

3- Démarrer l'interface du client.
	Maintenant que les deux serveurs sont en marche, il est possible de lancer des commandes à partir d'un interface client. Dans un troisième terminal, on peut exécuter les commandes suivants avec le script client.sh :
		./client.sh ip <commande> <nom utilisateur> <mot de passe> <nom de fichier>
		
		commande : la commande à exécuter. (OBLIGATOIRE)
			- newuser : créer un nouveau utilisateur
			- create : créer un nouveau fichier vide dans le serveur de fichier. (NOM DE FICHIER NÉCESSAIRE)
			- list : afficher la liste de tous les fichiers existants dans le serveur de fichier
			- syncLocalDirectory : récupérer tous les fichiers existants dans le serveur de fichier et de les copier dans le dossier local
			- get : récupérer le fichier demandé et de le copier dans le dossier local (NOM DE FICHIER NÉCESSAIRE)
			- lock : verrouiller le fichier en question (NOM DE FICHIER NÉCESSAIRE)
			- push : envoyer une nouvelle version du fichier au serveur et le mettre à jour dans le serveur (NOM DE FICHIER NÉCESSAIRE)

		* notez que ce paramètre est "case-sensitive"
		* il est important de savoir aussi qu'il faut tout d'abord créer un nouveau utilisateur avec la commande newuser avant de pouvoir utiliser les autres commandes.

		nom utilisateur : nom de l'utilisateur utilisé pour s'authentifier (OBLIGATOIRE)
		mot de passe : mot de passe utilisé pour s'authentifier (OBLIGATOIRE)
		nom de fichier : le nom du fichier à traiter avec la commande. Ce paramètre est obligatoire de spécifier pour certaines commandes (FACULTATIF)

    **IMPORTANT
      Afin que le programme fonctionne correctement, il faut s'assurer que les dossiers files et .file_store existent dans le dossier ResponseTime_Analyzer. Le dossier files représente le dossier local du client et le dossier .file_store est le dossier par défaut dans le serveur de fichier. Toute échange de fichier se fait entre ces deux dossiers.
	
		
