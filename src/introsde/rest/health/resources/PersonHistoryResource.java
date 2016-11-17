package introsde.rest.health.resources;

import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import introsde.rest.health.model.HealthMeasureHistory;
import introsde.rest.health.model.LifeStatus;
import introsde.rest.health.model.Person;

public class PersonHistoryResource {
	   @Context
	    UriInfo uriInfo;
	    @Context
	    Request request;
	    int id;
	    String measureType;
	    int mid;

	    EntityManager entityManager; // only used if the application is deployed in a Java EE container

	    public PersonHistoryResource(UriInfo uriInfo, Request request,int id, String measureType, EntityManager em) {
	        this.uriInfo = uriInfo;
	        this.request = request;
	        this.id = id;
	        this.measureType = measureType;
	        this.entityManager = em;
	    }

	    public PersonHistoryResource(UriInfo uriInfo, Request request,int id, String measureType) {
	        this.uriInfo = uriInfo;
	        this.request = request;
	        this.id = id;
	        this.measureType = measureType;
	    }
	    
	    
	    // Application integration
	    @GET
	    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    public List<HealthMeasureHistory> getPersonHistory() {
	        List<HealthMeasureHistory> history = this.getPersonHistoryByIdAndType(id, measureType);
	        if (history == null)
	            throw new RuntimeException("Get: History with " + id + " not found");
	        return history;
	    }
	    
	    @POST
	    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    public LifeStatus newLifeStatus(LifeStatus lifestatus) throws IOException {
	        System.out.println("Creating new item in history..."); 
	        return LifeStatus.saveLifeStatusAndStore(lifestatus, id, measureType);
	    }
	    

	    public List<HealthMeasureHistory> getPersonHistoryByIdAndType(int personId, String measureType) {
	        System.out.println("Reading person history from DB with id: "+personId);

	        // this will work within a Java EE container, where not DAO will be needed
	        //Person person = entityManager.find(Person.class, personId); 

	        List<HealthMeasureHistory> history = HealthMeasureHistory.getPersonHistoryByIdAndType(personId, measureType);
	        System.out.println("Person history: "+history.toString());
	        return history;
	    }
	    
}
