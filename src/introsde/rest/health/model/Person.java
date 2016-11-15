package introsde.rest.health.model;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.*;
import javax.xml.bind.annotation.*;

import introsde.rest.health.dao.LifeCoachDao;
@Entity  // indicates that this class is an entity to persist in DB
@Table(name="\"Person\"") // to what table must be persisted
@NamedQuery(name="Person.findAll", query="SELECT p FROM Person p")
@XmlRootElement
public class Person implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id // defines this attributed as the one that identifies the entity
    @GeneratedValue(generator="sqlite_person")
    @TableGenerator(name="sqlite_person", table="sqlite_sequence",
        pkColumnName="name", valueColumnName="seq",
        pkColumnValue="Person")
    @Column(name="\"idPerson\"") // maps the following attribute to a column
    private int idPerson;
    
    @Column(name="\"firstname\"")
    private String firstname;
    
    @Column(name="\"lastname\"")
    private String lastname;
    
    @Temporal(TemporalType.DATE) // defines the precision of the date attribute
    @Column(name="\"birthdate\"")
    private Date birthdate; 
    
    @Column(name="\"username\"")
	private String username;
    
    @Column(name="\"email\"")
	private String email;

    // mappedBy must be equal to the name of the attribute in LifeStatus that maps this relation
    @OneToMany(mappedBy="person",cascade=CascadeType.ALL,fetch=FetchType.EAGER)
    private List<LifeStatus> lifeStatus;
    
    public Person() {
	}

    
    /*GETTERS*/
    
    public int getIdPerson() {
    	return idPerson;
    }
    
    public String getFirstname() {
    	return firstname;
    }
    
    public String getLastname() {
    	return lastname;
    }
    
    public Date getBirthdate() {
    	return birthdate;
    }
    
	public String getEmail() {
		return this.email;
	}

	public String getUsername() {
		return this.username;
	}
    
    @XmlElementWrapper(name = "healthprofile")
    @XmlElement(name = "measureType")
    public List<LifeStatus> getLifeStatus() {
        return lifeStatus;
    }
    
    
    /*SETTERS*/
    
    public void setIdPerson(int idPerson) {
    	this.idPerson = idPerson;
    }
    
    public void setFirstname(String firstname) {
    	this.firstname = firstname;
    }
    
    public void setLastname(String lastname) {
    	this.lastname = lastname;
    }
    
    public void setBirthdate(Date birthdate) {
    	this.birthdate = birthdate;
    }
    
    public void setEmail(String email) {
		this.email = email;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setLifeStatus(List<LifeStatus> lifestatus) {
		this.lifeStatus = lifestatus;
	}
    
    
    /*QUERIES TO DB*/
    
    public static Person getPersonById(int personId) {
        EntityManager em = LifeCoachDao.instance.createEntityManager();
        Person p = em.find(Person.class, personId);
        LifeCoachDao.instance.closeConnections(em);
        return p;
    }

    public static List<Person> getAll() {
        EntityManager em = LifeCoachDao.instance.createEntityManager();
        List<Person> list = em.createNamedQuery("Person.findAll", Person.class)
            .getResultList();
        LifeCoachDao.instance.closeConnections(em);
        return list;
    }

    public static Person savePerson(Person p) {
        EntityManager em = LifeCoachDao.instance.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(p);
        tx.commit();
        LifeCoachDao.instance.closeConnections(em);
        return p;
    } 

    public static Person updatePerson(Person p) {
    	
    	List<LifeStatus> list = p.getLifeStatus();
    	if(list == null) { //if the healthprofile is not modified, do not delete the current one
    		Person personInDb = Person.getPersonById(p.idPerson);
        	List<LifeStatus> lifestatus = personInDb.getLifeStatus();
        	p.setLifeStatus(lifestatus);
    	} else { //set the person id to the new healthprofile 
    		for(LifeStatus l : list) {
    			l.setPerson(p);
    		}
    	}
    	
        EntityManager em = LifeCoachDao.instance.createEntityManager();        
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        p=em.merge(p);
        tx.commit();
        LifeCoachDao.instance.closeConnections(em);
        return p;
    }

    public static void removePerson(Person p) {
        EntityManager em = LifeCoachDao.instance.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        p=em.merge(p);
        em.remove(p);
        tx.commit();
        LifeCoachDao.instance.closeConnections(em);
    }
    
    @Override
    public String toString() { 
        return "Name: '" + this.firstname + " " + this.lastname + "', Birthday: '" + this.birthdate + "'";
    } 
}
