package introsde.rest.health.resources;

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import introsde.rest.health.model.HealthMeasureHistory;

public class PersonHistoryWithMidResource {
	   @Context
	    UriInfo uriInfo;
	    @Context
	    Request request;
	    int id;
	    String measureType;
	    int mid;

	    EntityManager entityManager; // only used if the application is deployed in a Java EE container

	    
	    public PersonHistoryWithMidResource(UriInfo uriInfo, Request request,int id, String measureType, int mid, EntityManager em) {
	        this.uriInfo = uriInfo;
	        this.request = request;
	        this.id = id;
	        this.measureType = measureType;
	        this.mid = mid;
	        this.entityManager = em;
	    }

	    public PersonHistoryWithMidResource(UriInfo uriInfo, Request request,int id, String measureType, int mid) {
	        this.uriInfo = uriInfo;
	        this.request = request;
	        this.id = id;
	        this.measureType = measureType;
	        this.mid = mid;
	    }
	    
	    // Application integration
	    @GET
	    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	    public HealthMeasureHistory getPersonHistoryByMid() {
	        HealthMeasureHistory history = this.getPersonHistoryByIdTypeAndMid(id, measureType, mid);
	        if (history == null)
	            throw new RuntimeException("Get: History with " + id + " not found");
	        return history;
	    }

	    
	    public HealthMeasureHistory getPersonHistoryByIdTypeAndMid(int personId, String measureType, int mid) {
	        System.out.println("Reading person history from DB with id: "+personId);

	        // this will work within a Java EE container, where not DAO will be needed
	        //Person person = entityManager.find(Person.class, personId); 

	        HealthMeasureHistory history = HealthMeasureHistory.getPersonHistoryByIdTypeAndMid(personId, measureType, mid);
	        System.out.println("Person history: "+history.toString());
	        return history;
	    }
}