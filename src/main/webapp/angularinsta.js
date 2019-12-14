var app = angular.module('instatest2', ['ngRoute', 'ngCookies']);
app.controller('TController',['$scope','$window','$cookies', function($scope,$window,$cookies) {


    $scope.login ='';
    $scope.log = false;
    $scope.listPosts = [];
   

    if($cookies.get('mylogin') != undefined){

    	var url = window.location.href;

    	if(url.includes("Register") || url.includes("Connexion")){
    		window.location.href = "./";
    	} else {
		    $scope.login = $cookies.get('mylogin');
		    $scope.log = true;
    	}
	 }



    $scope.register = function(userlogin, usermail, userpw, userfirstname, userlastname){

		if($scope.log == false){

            gapi.client.instatest2.createUser({
				login: userlogin,
				email: usermail,
				pw: userpw,
				firstname: userfirstname,
				lastname: userlastname
			}).execute(function(resp){

                if(resp.code != 503){
                    $scope.login = userlogin; 
                    $scope.log = true;
                    $cookies.put('mylogin', userlogin);
                    $scope.$apply();
                    window.location.href = "./";
                } else {
                	alert("L'utilisateur existe déjà / Adresse mail déjà utilisée :(")
                }
			});
        }
    }



    $scope.connexion = function(cologin, copassword){
   
        gapi.client.instatest2.getUser({
            login: cologin,
            password: copassword
        }).execute(function(resp){

            if (resp.code !=503){

            	$scope.login = cologin;
            	$scope.log = true;
            	$cookies.put('mylogin', cologin);
            	$scope.$apply();
	            window.location.href = "./";

            }else{
                alert("L'utilisateur n'existe pas ou vous avez mal saisi votre mot de passe :(")
            }  
        })     
    }
	




    $scope.FCfollow = function(toFollow){

			gapi.client.instatest2.follow({
				follower: $scope.login,
				followed: toFollow
			}).execute(function(resp){

				if(resp.code != 503){

					$('#champ-follow').val('');
					alert("Utilisateur bien suivi !");
					$scope.FCgetPosts();
					$scope.$apply();

				}else{
					alert("L'utilisateur n'existe pas :(")
				}

			});
	}	


	$scope.FCgetPosts = function(){



			gapi.client.instatest2.getPostsFromFollowed({
				login: $scope.login
			}).execute(function(resp){

				if(resp.code != 503){
					if (resp.items.length > 0){

						$scope.listPosts = [];
						for (var i = 0; i < resp.items.length; i++){
							$scope.listPosts.push({
								key: resp.items[i].key,
								author: resp.items[i].properties.author,
								text: resp.items[i].properties.message,
								date: resp.items[i].properties.date,
								urlImage : resp.items[i].properties.urlImage,
								nbLikesPost :  resp.items[i].properties.nbLikesPost,								
							});
							
						}

						$scope.$apply();

					}else{ 
						alert("Aucun post à afficher :(");
					}
					
				} else {
					alert("Aucun post à afficher :(");
				}
			});	


	}


	$scope.FClike = function(param){
		gapi.client.instatest2.ajouterLike({
			idPost: param
		}).execute(function(resp){

			$scope.FCgetPosts();

		});	
	}


	$scope.deconnexion = function(){
		
		$cookies.remove('mylogin');
		$scope.login = '';
		$scope.log = false;
		$scope.listPosts = [];

	}


    $window.init = function() {

          var rootApi = 'https://instatest2.appspot.com/_ah/api/';
          console.log("En attente de chargement de l'api ...");

	      gapi.client.load('instatest2', 'v1', function() {

	        console.log("L'api s'est chargée, vous pouvez utiliser le site !");
	        
	      }, rootApi);
	}

}]);