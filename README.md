# Tiny Insta

# Mathis LAURENT, Tristan RACAPE, Swann ESPAGNE

## URL 
*    https://instatest2.appspot.com
*    https://instatest2.appspot.com/_ah/api/explorer

## Contexte
* Pour le backend, nous avons deux fichiers Java différents. Un fichier principal qui défini toutes les méthodes de l'api (InstaTest.java), et un autre (UploadServlet.java) qui est servlet permettant de créer un post avec une image, car nous n'avons pas réussi à faire cette fonction dans le fichier java de l'API. Nous avons aussi 3 fichiers Java permettant de manipuler les entités (User, Post et GestionCompteur)

* Pour le frontend, nous avons utilisé AngularJS / Materialize CSS / JQuery. Il y a 4 pages différentes : Une page d'accueil où l'on peut créer un post, suivre quelqu'un, afficher sa timeline et se déconnecter. Une page de connexion où l'on peut se connecter, Une page de création de compte et Une page fonctionnalités ou sont écrites les performances des fonctions.


## Méthodes de l'API
* Les méthodes AngularJS sont :
- **register** qui permet de créer un utilisateur
- **connexion** qui permet de se connecter
- **FCFollow** qui permet de suivre un autre utilisateur
- **FCgetPosts** qui permet de récupérer les posts des utilisateurs que l'on suit
- **FClike** qui permet d'ajouter un "Like" à un post
- **deconnexion** de se déconnecter


## Entités
* Voici les différentes entités que nous avons pu manipuler et insérer dans le Google Datastore

- L'entité **User** qui contient un id unique, un login, un mot de passe, un nom, un prénom, un email, la liste de ses followers et des personnes qu'il follow.
- L'entité **Post** qui contient un id unique, un auteur (correspodant au login de User), un message, une date, et l'url de l'image du post
- L'entité **GestionCompteur** qui contient un id unique, l'id d'un Post (correspondant à l'id de Post), le nom d'un sous compteur, et la valeur du sous compteur. (Le fonctionnement de cette entité sera expliquée dans la partie ci dessous)


## Scaling
* Le problème étant que si trop de personnes connectées sur TinyInsta "Likent" un même post en même temps, il se peut que cela prenne trop de temps et que certains "Likes" ne soient pas comptés.

* Pour le scaling nous avons donc changé la manière dont le compteur de "Like" pouvait marcher originellement, c'est à dire avoir un compteur de "Like" pour chaque Post. (Donc un attribut nbLikes dans l'entité Post)


* Pour gerer le compteur des likes des Posts, nous avons décidé qu'un post devait avoir 3 "Sous compteurs" de "Like". L'addition de ces 3 "Sous Compteurs" donne la vraie valeur du nombre de "Like" d'un post. Lorsque l'on veut "Liker" un post, un des 3 compteurs est choisi au hasard et est incrémenté. Pour avoir le nombre de likes totaux d'un Post, on fait donc la somme des 3 sous compteurs associé au Post. Cela permet donc de fluidifier le traffic si beaucoup de gens veulent liker un même post, car il y a donc moins que chances que plusieurs utilisateurs manipulent la même variable en même temps.

* Ainsi, lorsqu'on crée une Entité Post, 3 entités GestionCompteur sont crées en parallèle pour ce post.


## Performance
* Le temps moyen des fonctions sont spécifiées dans la page fonctionnalites.html

## Autre
* Il se peut que l'API soit lente à charger au démarrage, un message dans la console indique si l'API est chargée ou non !