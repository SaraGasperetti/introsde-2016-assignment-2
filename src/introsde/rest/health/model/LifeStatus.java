package introsde.rest.health.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;
import javax.xml.bind.annotation.*;

import introsde.rest.health.dao.LifeCoachDao;

@Entity  // indicates that this class is an entity to persist in DB
@Table(name="\"LifeStatus\"") // to what table must be persisted
@NamedQuery(name="LifeStatus.findAll", query="SELECT lf FROM LifeStatus lf")
@XmlRootElement(name = "measureType")
public class LifeStatus implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id // defines this attributed as the one that identifies the entity
    @GeneratedValue(generator="sqlite_lifestatus")
    @TableGenerator(name="sqlite_lifestatus", table="sqlite_sequence",
        pkColumnName="name", valueColumnName="seq",
        pkColumnValue="LifeStatus")
    @Column(name="\"idMeasure\"") // maps the following attribute to a column
    private int idMeasure;
        
    @Column(name="\"value\"")
    private String value;
    
    @Column(name="\"measure\"")
    private String measure;
    
    @ManyToOne
    @JoinColumn(name="\"idPerson\"",referencedColumnName="\"idPerson\"")
    private Person person;
    
//    @ManyToOne
//    @JoinColumn(name = "\"idMeasureDef\"", referencedColumnName = "\"idMeasureDef\"", insertable = true, updatable = true)
//    private MeasureDefinition measureDefinition;
    
    
    /*GETTERS*/
    
    @XmlTransient
    public int getIdMeasure() {
    	return idMeasure;
    }
    
    public String getValue() {
    	return value;
    }
    
    @XmlTransient  //to avoid potential infinite loop
    public Person getPerson() {
        return person;
    }
    
    public String getMeasure() {
    	return measure;
    }
    
//    @XmlElement
//    public MeasureDefinition getMeasureDefinition() {
//    	return measureDefinition;
//    }
    
    
    /*SETTERS*/
    
    public void setIdMeasure(int idMeasure) {
    	this.idMeasure = idMeasure;
    }
    
    public void setValue(String value) {
    	this.value = value;
    }
    
    public void setMeasure(String measure) {
    	this.measure = measure;
    }
    
    public void setPerson(Person p) {
    	this.person = p;
    }
    
    
    /*QUERIES TO DB*/
    
//    public static LifeStatus getMeasureById(int measureId) {
//        EntityManager em = LifeCoachDao.instance.createEntityManager();
//        LifeStatus lf = em.find(LifeStatus.class, measureId);
//        LifeCoachDao.instance.closeConnections(em);
//        return lf;
//    }
    
    /**
     * Get the lifestatus of one person of a specified type (if the db is consistent, 
     * it should be just one, otherwies the first one is returned)
     * @param personId the id of the person
     * @param measureType the type of measure
     * @return the lifestatus information
     */
    public static LifeStatus getLifeStatusByPersonIdAndType(int personId, String measureType) {
    	EntityManager em = LifeCoachDao.instance.createEntityManager();
		List<LifeStatus> list = em.createQuery(
				"SELECT l FROM LifeStatus l WHERE l.person.idPerson = :id and l.measure = :measureType", 
				LifeStatus.class)
				.setParameter("id", personId)
				.setParameter("measureType", measureType)
				.getResultList();
		return list.get(0);	
    }
    
    /**
     * Get all the lifestatus of a specified person
     * @param personId the id of the person
     * @return the list of all lifestatus
     */
    public static List<LifeStatus> getAllLifeStatusByPersonId(int personId) {
    	EntityManager em = LifeCoachDao.instance.createEntityManager();
		List<LifeStatus> list = em.createQuery(
				"SELECT l FROM LifeStatus l WHERE l.person.idPerson = :id", 
				LifeStatus.class)
				.setParameter("id", personId)
				.getResultList();
		return list;	
    }

    /**
     * Get all lifestatus of all people in db
     * @return a list of lifestatus
     */
    public static List<LifeStatus> getAll() {
        EntityManager em = LifeCoachDao.instance.createEntityManager();
        List<LifeStatus> list = em.createNamedQuery("LifeStatus.findAll", LifeStatus.class)
            .getResultList();
        LifeCoachDao.instance.closeConnections(em);
        return list;
    }


    /**
     * Create a new lifestatus for a specified type and store the old one in the person history
     * @param lifestatus the new lifestatus
     * @param personId the id of the person
     * @param measureType the type of the measure
     * @return the new lifestatus
     */
    public static LifeStatus saveLifeStatusAndStore(LifeStatus lifestatus, int personId, String measureType) {
        
        //retrieve old lifestatus to save in the history
        LifeStatus oldLifestatus = LifeStatus.getLifeStatusByPersonIdAndType(personId, measureType);
        
        if(oldLifestatus != null) {
        	System.out.println("old " + oldLifestatus.getMeasure() + " --> " + oldLifestatus.getValue());
	        LifeStatus.removeLifeStatus(oldLifestatus);
	        HealthMeasureHistory.saveLifestatusIntoHistory(oldLifestatus, personId);
        }
        
        //set the new lifestatus
		Person p = Person.getPersonById(personId);
		lifestatus.setPerson(p);
		lifestatus.setMeasure(measureType);
		
		//store the new lifestatus
		EntityManager em = LifeCoachDao.instance.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(lifestatus);
        tx.commit();
        LifeCoachDao.instance.closeConnections(em);
        
        //update the lifestatus reference in person
		Person.updatePerson(p);

        return lifestatus;
    }
    
    public static LifeStatus updateLifestatus(LifeStatus lf) {
        EntityManager em = LifeCoachDao.instance.createEntityManager();        
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        lf=em.merge(lf);
        tx.commit();
        LifeCoachDao.instance.closeConnections(em);
        return lf;
    }

    public static void removeLifeStatus(LifeStatus lf) {
        EntityManager em = LifeCoachDao.instance.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        lf=em.merge(lf);
        em.remove(lf);
        tx.commit();
        LifeCoachDao.instance.closeConnections(em);
    }
        
}