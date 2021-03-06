package introsde.rest.health.resources;

import introsde.rest.health.model.Person;

import java.text.ParseException;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
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

@Stateless // only used if the the application is deployed in a Java EE container
@LocalBean // only used if the the application is deployed in a Java EE container
public class PersonResource {

    @Context
    UriInfo uriInfo;
    @Context
    Request request;
    int id;

    EntityManager entityManager; // only used if the application is deployed in a Java EE container

    public PersonResource(UriInfo uriInfo, Request request, int id, EntityManager em) {
        this.uriInfo = uriInfo;
        this.request = request;
        this.id = id;
        this.entityManager = em;
    }

    public PersonResource(UriInfo uriInfo, Request request, int id) {
        this.uriInfo = uriInfo;
        this.request = request;
        this.id = id;
    }

    // Application integration
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getPerson() {
        Person person = this.getPersonById(id);
        if (person == null) {
            return Response.status(404).build();
        }
        return Response.ok().entity(person).build();
    }

//    // for the browser
//    @GET
//    @Produces(MediaType.TEXT_XML)
//    public Response getPersonHTML() {
//        Person person = this.getPersonById(id);
//        if (person == null) {
//        	System.out.println("Ciao");
//        	return Response.noContent().build();
//        }
//            //throw new RuntimeException("Get: Person with " + id + " not found");
//        System.out.println("Returning person... " + person.getIdPerson());
//        return Response.ok().entity(person).build();
//        //return person;
//    }
    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response putPerson(Person person) {
        System.out.println("--> Updating Person... " + this.id);
        System.out.println("--> " + person.toString());

        Response res;
        Person existing = getPersonById(this.id);

        if (existing == null) {
            res = Response.noContent().build();
        } else {
            //res = Response.created(uriInfo.getAbsolutePath()).build();
            person.setIdPerson(this.id);

            //take the non specified fields from the db
            if (person.getFirstname() == null) {
                person.setFirstname(existing.getFirstname());
            }
            if (person.getLastname() == null) {
                person.setLastname(existing.getLastname());
            }
            if (person.getBirthdate() == null) {
                try {
                    person.setBirthdate(existing.getBirthdate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (person.getUsername() == null) {
                person.setUsername(existing.getUsername());
            }
            if (person.getEmail() == null) {
                person.setEmail(existing.getEmail());
            }
            Person updatedPerson = Person.updatePerson(person);
            res = Response.ok().entity(updatedPerson).build();
        }
        return res;
    }

    @DELETE
    public void deletePerson() {
        Person c = getPersonById(id);
        if (c == null) {
            throw new RuntimeException("Delete: Person with " + id
                    + " not found");
        }
        Person.removePerson(c);
    }

    public Person getPersonById(int personId) {
        System.out.println("Reading person from DB with id: " + personId);

        // this will work within a Java EE container, where not DAO will be needed
        //Person person = entityManager.find(Person.class, personId); 
        Person person = Person.getPersonById(personId);
        if (person != null) {
            System.out.println("Person: " + person.toString());
        }
        return person;
    }
}
