L'addresse ip de notre serveur distant est 132.207.12.110

Si les fichiers sources ne sont pas disponible dans le serveur distant, suivre ces �tapes :
    1- Connecter au serveur distant avec la commande :
        ssh -i key-tp1.pem ubuntu@132.207.12.110

    2- Envoyer le dossier ResponseTime_Analyzer dans le serveur distant sous le nom pt2 :
        scp -i key-tp1-pem ResponseTime_Analyzer ubuntu@132.207.12.110:pt2

1- D�marrer le syst�me d'authentification sur le serveur distant.
	Pour se connecter au serveur distant, � partir du dossier INF8480-TP1 o� la cl� key-tp1.pem se trouve, ex�cuter la commande :
		ssh -i key-tp1.pem ubuntu@132.207.12.110

	� partir du dossier bin, ex�cuter la commande :
		rmiregistry&

	D�marrer le serveur d'authentification avec le script authserver.sh en ex�cutant la commande :
		./authserver.sh 132.207.12.110

2- D�marrer le syst�me de fichier sur le serveur local.
	� partir du dossier bin dans la machine locale, dans un autre terminal, enregistrer dans le registre avec la commande :
         rmiregistry&
    
    d�marrer le serveur de fichier avec le script fileserver.sh en ex�cutant la commande :
		./fileserver.sh 127.0.0.1 132.207.12.110

3- D�marrer l'interface du client.
	Maintenant que les deux serveurs sont en marche, il est possible de lancer des commandes � partir d'un interface client. Dans un troisi�me terminal, on peut ex�cuter les commandes suivants avec le script client.sh :
		./client.sh ip <commande> <nom utilisateur> <mot de passe> <nom de fichier>
		
		commande : la commande � ex�cuter. (OBLIGATOIRE)
			- newuser : cr�er un nouveau utilisateur
			- create : cr�er un nouveau fichier vide dans le serveur de fichier. (NOM DE FICHIER N�CESSAIRE)
			- list : afficher la liste de tous les fichiers existants dans le serveur de fichier
			- syncLocalDirectory : r�cup�rer tous les fichiers existants dans le serveur de fichier et de les copier dans le dossier local
			- get : r�cup�rer le fichier demand� et de le copier dans le dossier local (NOM DE FICHIER N�CESSAIRE)
			- lock : verrouiller le fichier en question (NOM DE FICHIER N�CESSAIRE)
			- push : envoyer une nouvelle version du fichier au serveur et le mettre � jour dans le serveur (NOM DE FICHIER N�CESSAIRE)

		* notez que ce param�tre est "case-sensitive"
		* il est important de savoir aussi qu'il faut tout d'abord cr�er un nouveau utilisateur avec la commande newuser avant de pouvoir utiliser les autres commandes.

		nom utilisateur : nom de l'utilisateur utilis� pour s'authentifier (OBLIGATOIRE)
		mot de passe : mot de passe utilis� pour s'authentifier (OBLIGATOIRE)
		nom de fichier : le nom du fichier � traiter avec la commande. Ce param�tre est obligatoire de sp�cifier pour certaines commandes (FACULTATIF)

    **IMPORTANT
      Afin que le programme fonctionne correctement, il faut s'assurer que les dossiers files et .file_store existent dans le dossier ResponseTime_Analyzer. Le dossier files repr�sente le dossier local du client et le dossier .file_store est le dossier par d�faut dans le serveur de fichier. Toute �change de fichier se fait entre ces deux dossiers.
	
		
