package momocorp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;


import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import momocorp.User;
@Api(name = "instatest2",namespace = @ApiNamespace(ownerDomain = "mycompany.com", ownerName = "mycompany.com", packagePath = "services"))
public class InstaTest {
	

	/**
     * Creates a new user in the datastore with several informations related to connection
     * @param login the chosen nickname
     * @param email the user's email
     * @param pw the chosen password
     * @param firstname the user's firstname
     * @param lastname the user's lastname
     */
    @ApiMethod(name = "createUser", httpMethod = ApiMethod.HttpMethod.POST)
    public void createUser(@Named("login") String login, @Named("email") String email, @Named("pw") String pw,@Named("firstname") String firstname,@Named("lastname") String lastname) {

        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Collection<Filter> filters = new ArrayList<Filter>();
        filters.add(new Query.FilterPredicate("login", Query.FilterOperator.EQUAL, login));
        filters.add(new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, email));
        Filter filter = new Query.CompositeFilter(CompositeFilterOperator.OR, filters);
        Query query = new Query("User").setFilter(filter);
        Entity userEntity = ds.prepare(query).asSingleEntity();

        if (userEntity != null){
            throw new IllegalStateException("User already exist");
        }else{

            userEntity = new Entity("User");
            userEntity.setIndexedProperty("login", login);
            userEntity.setProperty("email", email);
            userEntity.setProperty("pw", pw);
            userEntity.setProperty("firstname", firstname);
            userEntity.setProperty("lastname", lastname);
            userEntity.setProperty("followers", new ArrayList<String>()); // Personnes qui me suivent
            userEntity.setProperty("followed", new ArrayList<String>()); // Personnes que je suis

            ds.put(userEntity);

        }
    }



    @ApiMethod(name = "getUser",httpMethod = ApiMethod.HttpMethod.GET)
    public User getUser(@Named("login") String login, @Named("password") String password) {

        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Filter filter = new Query.FilterPredicate("login",FilterOperator.EQUAL, login);
        Filter filter2 = new Query.FilterPredicate("pw",FilterOperator.EQUAL, password);
        Query query = new Query("User").setFilter(filter);
        query.setFilter(filter2);
        Entity userEntity = ds.prepare(query).asSingleEntity();

        if(userEntity == null) {
            throw new NullPointerException("User not found.");
        }

        User user = new User(userEntity);
        return user;
    }



    @ApiMethod(name = "follow")
    public void follow(@Named("follower") String followerLogin, @Named("followed") String followedLogin) {
            
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            Filter followerFilter = new FilterPredicate("login", FilterOperator.EQUAL, followerLogin);
            Filter followedFilter = new FilterPredicate("login", FilterOperator.EQUAL, followedLogin);
            CompositeFilter followFilter = CompositeFilterOperator.or(followerFilter, followedFilter);
            Query followQuery = new Query("User").setFilter(followFilter);


            PreparedQuery prepFolQuery = datastore.prepare(followQuery);
            List<Entity> result = prepFolQuery.asList(FetchOptions.Builder.withDefaults());
            
            if (result == null) {
                throw new NullPointerException("Users not found (null)");
            }
            
            Entity followerEntity = null;
            Entity followedEntity = null;
            for (Entity x : result ) {
                String login = (String) x.getProperty("login");
            
                if(login.equals(followedLogin)){
                    followedEntity = x;
                }
                else {
                    followerEntity = x;
                }
            }
            
            if (followerEntity == null) {
                throw new NullPointerException("User not found: "+  followerLogin);
            }
            if (followedEntity == null) {
                throw new NullPointerException("User not found: " + followedLogin);
            }
            
            List<String> followers = (List<String>) followedEntity.getProperty("followers");
            if (followers == null) {
                followers = new ArrayList<String>();
            }
            followers.add(followerLogin);
            followedEntity.setProperty("followers", followers);
            datastore.put(followedEntity);
            
            List<String> followed = (List<String>) followerEntity.getProperty("followed");
            if (followed == null) {
                followed = new ArrayList<String>();
            }
            followed.add(followedLogin);
            followerEntity.setProperty("followed", followed);
            datastore.put(followerEntity);
    }



    @ApiMethod(name = "getPostsFromFollowed")
    public List<Entity> getPostsFromFollowed(@Named("login") String login) {
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            Filter userFilter = new FilterPredicate("login", FilterOperator.EQUAL, login);
            Query userQuery = new Query("User").setFilter(userFilter);
                   
            
            PreparedQuery prepUsrQuery = datastore.prepare(userQuery);
            Entity user = prepUsrQuery.asSingleEntity();
            

            if (user == null) {
                throw new NullPointerException("Current user not found");
            }
            
            List<String> followed = (ArrayList<String>) user.getProperty("followed");
            
            if ( followed == null){
                throw new NullPointerException("No followed found");
            } 
                
            Filter postFilter = new FilterPredicate("author", FilterOperator.IN, followed);

            Query postQuery = new Query("Post").setFilter(postFilter);
            PreparedQuery prepPostQuery = datastore.prepare(postQuery);
            List<Entity> FollowedPosts = prepPostQuery.asList(FetchOptions.Builder.withDefaults());
            

            for(Entity post : FollowedPosts){

                Long idPost = (Long) post.getKey().getId();
                post.setProperty("nbLikesPost", getLikesPost(idPost));

            }

            return FollowedPosts;
    }





    @ApiMethod(name = "ajouterLike")
    public Entity ajouterLike(@Named("idPost")Long idPost){

        int nbCompteurs = 3;
        int compteurChoisi = (int)(Math.random() * nbCompteurs + 1);
        String nomSousCompteurChoisi = "SC" + compteurChoisi;

        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Filter filterID = new Query.FilterPredicate("idPost", Query.FilterOperator.EQUAL, idPost);
        Filter filterCPT = new Query.FilterPredicate("nomSousCompteur", Query.FilterOperator.EQUAL, nomSousCompteurChoisi);
        CompositeFilter filterFus = CompositeFilterOperator.and(filterID, filterCPT);

        Query query = new Query("GestionCompteur").setFilter(filterFus);

        Entity gestLikesPost = ds.prepare(query).asSingleEntity();

        Long tmpLike = (Long)gestLikesPost.getProperty("valeurCompteur");
        gestLikesPost.setProperty("valeurCompteur", tmpLike + 1);

        ds.put(gestLikesPost);

        return gestLikesPost;

    }



    private long getLikesPost(Long idPost){

        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Filter filter = new Query.FilterPredicate("idPost", Query.FilterOperator.EQUAL, idPost);
        Query query = new Query("GestionCompteur").setFilter(filter);
        PreparedQuery pq = ds.prepare(query);
        List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());

        long totalLikesPost = 0;

        for (Entity r : result){

            totalLikesPost += (Long)(r.getProperty("valeurCompteur"));
        }

        return totalLikesPost;

    }

}