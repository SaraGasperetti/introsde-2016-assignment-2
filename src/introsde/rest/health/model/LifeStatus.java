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

    public static List<LifeStatus> getAll() {
        EntityManager em = LifeCoachDao.instance.createEntityManager();
        List<LifeStatus> list = em.createNamedQuery("LifeStatus.findAll", LifeStatus.class)
            .getResultList();
        LifeCoachDao.instance.closeConnections(em);
        return list;
    }

    public static LifeStatus saveLifeStatus(LifeStatus lifestatus, int personId, String measureType) {
        EntityManager em = LifeCoachDao.instance.createEntityManager();
        
        LifeStatus oldLifestatus = LifeStatus.getLifeStatusByPersonIdAndType(personId, measureType);
        System.out.println("old " + oldLifestatus.getMeasure() + " --> " + oldLifestatus.getValue());
        LifeStatus.removeLifeStatus(oldLifestatus);
//        LifeStatus oldLifestatus1 = LifeStatus.getLifeStatusByPersonIdAndType(personId, measureType);
//        System.out.println("old " + oldLifestatus1.getMeasure() + " --> " + oldLifestatus1.getValue());
        HealthMeasureHistory.saveLifestatusIntoHistory(oldLifestatus, personId);
        
		Person p = Person.getPersonById(personId);
		lifestatus.setPerson(p);
		lifestatus.setMeasure(measureType);
		p.getLifeStatus().add(lifestatus);
		
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(lifestatus);
        tx.commit();
        LifeCoachDao.instance.closeConnections(em);
        return lifestatus;
    } 

//    public static LifeStatus updateLifeStatus(LifeStatus lf) {
//        EntityManager em = LifeCoachDao.instance.createEntityManager(); 
//        EntityTransaction tx = em.getTransaction();
//        tx.begin();
//        lf=em.merge(lf);
//        tx.commit();
//        LifeCoachDao.instance.closeConnections(em);
//        return lf;
//    }

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