package momocorp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Entity;

@PersistenceCapable(identityType=IdentityType.APPLICATION)
public class GestionCompteur implements Serializable{
	
	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.IDENTITY)
	private Long id;
	@Persistent
	private Long idPost;
	@Persistent
	private String nomSousCompteur;
	@Persistent
	private Long valeurCompteur;

	
	public GestionCompteur() {}
	
	public GestionCompteur(Entity e) {
		this.id = (Long) e.getKey().getId();
		this.idPost = (Long) e.getProperty("idUser");
		this.nomSousCompteur = (String) e.getProperty("nomSousCompteur");
		this.valeurCompteur = (Long) e.getProperty("valeurCompteur");
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Long getIdPost() {
		return idPost;
	}
	public void setIdPost(Long idPost) {
		this.idPost = idPost;
	}

	public String getNomSousCompteur() {
		return nomSousCompteur;
	}
	public void setNomSousCompteur(String nomSousCompteur) {
		this.nomSousCompteur = nomSousCompteur;
	}
	
	public Long getValeurCompteur() {
		return valeurCompteur;
	}
	public void setValeurCompteur(Long valeurCompteur) {
		this.valeurCompteur = valeurCompteur;
	}

}