package momocorp;

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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;



@SuppressWarnings("serial")
@WebServlet(name = "upload", value = "/upload")
@MultipartConfig()
public class UploadServlet extends HttpServlet {

  private static final String BUCKET_NAME = "bkimagesinsta";
  private static Storage storage = null;

  @Override
  public void init() {
    storage = StorageOptions.getDefaultInstance().getService();
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException,
      ServletException {


    // On récupère le message et le login
    String message = req.getParameter("message");
    String login = req.getParameter("login");
 
    long startTime = System.currentTimeMillis();
    
    // On récupère l'image
    final Part filePart = req.getPart("file");
    final String fileName = filePart.getSubmittedFileName();

    List<Acl> acls = new ArrayList<>();
    acls.add(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));

    Blob blob =
        storage.create(
            BlobInfo.newBuilder(BUCKET_NAME, fileName).setAcl(acls).build(),
            filePart.getInputStream());

    String urlImage = blob.getMediaLink();

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Filter filter = new Query.FilterPredicate("login",FilterOperator.EQUAL, login);
    Query query = new Query("User").setFilter(filter);
    Entity userEntity = ds.prepare(query).asSingleEntity();

    if(userEntity == null) {
        throw new NullPointerException("User not found.");
    }

    User user = new User(userEntity);

    resp.getWriter().print(user.getId());

    Entity postEntity = new Entity("Post");
        postEntity.setProperty("author", user.getLogin());
        postEntity.setProperty("message", message);
        postEntity.setProperty("urlImage", urlImage);
        postEntity.setProperty("date", new Date());

    Key clePost = ds.put(postEntity);
    Long idPostCree = clePost.getId();

    for (int i = 1; i <= 3; i++){

        Entity compteurLikePost = new Entity("GestionCompteur");
        compteurLikePost.setProperty("idPost", idPostCree);
        compteurLikePost.setProperty("nomSousCompteur", "SC" + i);
        compteurLikePost.setProperty("valeurCompteur", 0);
        ds.put(compteurLikePost);

    }


    if(login.indexOf("User") !=-1 && login.indexOf("Followers") != -1){
        long endTime = System.currentTimeMillis();
        resp.sendRedirect("/?" + (endTime - startTime));
    }else{
        resp.sendRedirect("/");
    }  

  }
}