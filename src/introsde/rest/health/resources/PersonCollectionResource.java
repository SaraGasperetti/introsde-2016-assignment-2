package introsde.rest.health.resources;

import introsde.rest.health.model.LifeStatus;
import introsde.rest.health.model.Person;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceUnit;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

@Stateless // will work only inside a Java EE application
@LocalBean // will work only inside a Java EE application
@Path("/person")
public class PersonCollectionResource {

    // Allows to insert contextual objects into the class,
    // e.g. ServletContext, Request, Response, UriInfo
    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    // will work only inside a Java EE application
    @PersistenceUnit(unitName = "introsde-jpa")
    EntityManager entityManager;

    // will work only inside a Java EE application
    @PersistenceContext(unitName = "introsde-jpa", type = PersistenceContextType.TRANSACTION)
    private EntityManagerFactory entityManagerFactory;

    // Return the list of people to the user in the browser
    @GET
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<Person> getPersonsBrowser(@QueryParam("measureType") String type, @QueryParam("max") Double max, @QueryParam("min") Double min) {
        System.out.println("Getting list of people...");
        List<Person> people = null;
        people = Person.getAll();

        //take only person in the range
        if (!(min == null && max == null) && type != null) {
            List<Person> newPeople = new LinkedList<>();
            for (Person p : people) {
                for (LifeStatus ls : p.getLifeStatus()) {
                    if (min == null) {
                        if ((ls.getMeasure().equals(type)) && (Double.parseDouble(ls.getValue()) < max)) {
                            newPeople.add(p);
                        }
                    } else if (max == null) {
                        if ((ls.getMeasure().equals(type)) && (Double.parseDouble(ls.getValue()) > min)) {
                            newPeople.add(p);
                        }
                    } else {
                        if ((ls.getMeasure().equals(type)) && (Double.parseDouble(ls.getValue()) < max) && (Double.parseDouble(ls.getValue()) > min)) {
                            newPeople.add(p);
                        }
                    }
                }
            }
            return newPeople;
        }

        return people;
    }

    // returns the number of people
    // to get the total number of records
    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String getCount() {
        System.out.println("Getting count...");
        List<Person> people = Person.getAll();
        int count = people.size();
        return String.valueOf(count);
    }

    @POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Person newPerson(Person person) throws IOException {
        System.out.println("Creating new person...");
        System.out.print("birthdate " + person.getBirthdate());
        return Person.savePerson(person);
    }

    // Defines that the next path parameter after the base url is
    // treated as a parameter and passed to the PersonResources
    // Allows to type http://localhost:599/base_url/1
    // 1 will be treaded as parameter todo and passed to PersonResource
    @Path("{personId}")
    public PersonResource getPerson(@PathParam("personId") int id) {
        return new PersonResource(uriInfo, request, id);
    }

    @Path("{personId}/{measureType}")
    public PersonHistoryResource getPersonHistory(@PathParam("personId") int id, @PathParam("measureType") String measureType, @QueryParam("before") String beforeDate, @QueryParam("after") String afterDate) {
        if (beforeDate == null || afterDate == null) {
            return new PersonHistoryResource(uriInfo, request, id, measureType);
        } else {
            return new PersonHistoryResource(uriInfo, request, id, measureType, beforeDate, afterDate);
        }
    }

    @Path("{personId}/{measureType}/{mid}")
    public PersonHistoryWithMidResource getPersonHistory(@PathParam("personId") int id, @PathParam("measureType") String measureType, @PathParam("mid") int mid) {
        return new PersonHistoryWithMidResource(uriInfo, request, id, measureType, mid);
    }
}
