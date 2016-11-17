package introsde.rest.client;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;    
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.glassfish.jersey.client.ClientConfig;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class MyClient {
	
	
	protected final static String GET = "GET";
	protected final static String PUT = "PUT";
	protected final static String POST = "POST";
	protected final static String DELETE = "DELETE";
	
	private WebTarget service;
	
	private MyClient() {
		ClientConfig clientConfig = new ClientConfig();
        Client client = ClientBuilder.newClient(clientConfig);
        service = client.target(getBaseURI());
	}
	
    public static void main(String[] args) {
        MyClient myClient = new MyClient();
        
        myClient.makeAllRequests(MediaType.APPLICATION_XML);
        myClient.makeAllRequests(MediaType.APPLICATION_JSON);
    }

    protected static URI getBaseURI() {
        return UriBuilder.fromUri(
                "http://127.0.1.1:5700/sdelab/").build();
    }
    
    private void makeAllRequests(String mediaType) {
    	
    	//Get the writer
    	WriterOnFile writer = WriterOnFile.getWriter(mediaType);
        writer.write("The server contacted is located at " + getBaseURI().toString());
        
    	String resource;
    	Response response;
    	String result;
    	int statusCode;
    	StatusType statusType;
    	String appResult;
    	String content = null;
    	
    	//Request #1//////////////////////////////////////////////////////////////////////
    	resource = "person";
    	response = makeRequest(GET, resource, mediaType);
    	//Get response info
    	result = response.readEntity(String.class);
    	statusCode = response.getStatus();
        statusType = response.getStatusInfo();   
    	
    	int firstPersonId = 1;
    	int lastPersonId = 1;
    	int count = 0;
    	if(mediaType.equals(MediaType.APPLICATION_XML)) {
    		count = queryXml(result, "count(//person)");
        	firstPersonId = queryXml(result, "//person[1]/idPerson");
        	lastPersonId = queryXml(result, "//person[last()]/idPerson");
    	} else if(mediaType.equals(MediaType.APPLICATION_JSON)) {
    		count = queryJsonSize(result);
        	firstPersonId = queryJsonId(result, 0);
        	lastPersonId = queryJsonId(result, count-1);
    	}
    	writer.write("First person in db has id " + firstPersonId);
    	writer.write("Last person in db has id " + lastPersonId);
    	writer.write("Total number of person: " + count+ "\n");
    	
    	if (count > 2) {
    		appResult = "OK";
    	} else {
    		appResult = "ERROR";
    	}
    	writer.printFormatted(GET, resource, result, statusCode, statusType, appResult, mediaType);
    	//////////////////////////////////////////////////////////////////////////////////
    	
        
        //Request #2//////////////////////////////////////////////////////////////////////
    	resource = "person/" + firstPersonId;
    	response = makeRequest(GET, resource, mediaType);
    	//Get response info
    	result = response.readEntity(String.class);
    	statusCode = response.getStatus();
        statusType = response.getStatusInfo();  
        if(statusCode == 200 | statusCode == 202) {
        	appResult = "OK";
        } else {
        	appResult = "ERROR";
        }
    	writer.printFormatted(GET, resource, result, statusCode, statusType, appResult, mediaType);
    	//////////////////////////////////////////////////////////////////////////////////
    	
        //Request #3//////////////////////////////////////////////////////////////////////
    	resource = "person/" + firstPersonId;
    	String expectedFirstname = null;
    	if(mediaType.equals(MediaType.APPLICATION_XML)) {
    		expectedFirstname = "<firstname>John</firstname>";
    		content = "<person>" + expectedFirstname + "</person>";
    	} else if(mediaType.equals(MediaType.APPLICATION_JSON)) {
    		expectedFirstname = "\"firstname\":\"John\"";
    		content = "{" + expectedFirstname + "}";
    	}
    	
    	response = makeRequest(PUT, resource, mediaType, content);
    	//Get response info
    	result = response.readEntity(String.class);
    	statusCode = response.getStatus();
        statusType = response.getStatusInfo();  
        if(result.contains(expectedFirstname)) {
        	appResult = "OK";
        } else {
        	appResult = "ERROR";
        }
    	writer.printFormatted(PUT, resource, result, statusCode, statusType, appResult, mediaType);
    	//////////////////////////////////////////////////////////////////////////////////
    	
    	
        //Request #4//////////////////////////////////////////////////////////////////////
    	resource = "person/";
    	if(mediaType.equals(MediaType.APPLICATION_XML)) {
    		content = "<person><firstname>Chuck</firstname><lastname>Norris</lastname>" +
    				"<birthdate>01/01/1945</birthdate>" +
    				"<healthprofile>" + 
    				"<measureType><measure>weight</measure><value>78.9</value></measureType>" +
    				"<measureType><measure>height</measure><value>172</value></measureType>" +
    				"</healthprofile></person>";
    	} else if(mediaType.equals(MediaType.APPLICATION_JSON)) {
    		content =  "{\"firstname\":\"Chuck\", \"lastname\":\"Norris\"," +
    		          "\"birthdate\":\"01/01/1945\"," +
    		          "\"measureType\":[{\"value\":\"78.9\",\"measure\":\"weight\"}," +
    		          "{\"value\":\"1.72\",\"measure\":\"height\"}]}";
    	}
    	
    	response = makeRequest(POST, resource, mediaType, content);
    	//Get response info
    	result = response.readEntity(String.class);
    	statusCode = response.getStatus();
        statusType = response.getStatusInfo(); 
        
        int newId = -1;
    	if(mediaType.equals(MediaType.APPLICATION_XML)) {
        	newId = queryXml(result, "//person/idPerson");
    	} else if(mediaType.equals(MediaType.APPLICATION_JSON)) {
        	newId = queryJsonId(result);
    	}
    	writer.write("The new id is: " + newId);
    	
        if((statusCode == 200 | statusCode == 201 | statusCode == 202) && newId != -1) {
        	appResult = "OK";
        } else {
        	appResult = "ERROR";
        }
    	writer.printFormatted(POST, resource, result, statusCode, statusType, appResult, mediaType);
    	//////////////////////////////////////////////////////////////////////////////////
        
        //Request #5//////////////////////////////////////////////////////////////////////
    	resource = "person/" + newId;
    	response = makeRequest(DELETE, resource);
    	//Get response info
    	statusCode = response.getStatus();
        statusType = response.getStatusInfo();
        appResult = "OK";
        
    	writer.printFormatted(DELETE, resource, null, statusCode, statusType, appResult, mediaType);
    	//////////////////////////////////////////////////////////////////////////////////
    	
        //Request #2 again on the new person//////////////////////////////////////////////////////////////////////
    	resource = "person/" + newId;
    	response = makeRequest(GET, resource, mediaType);
    	//Get response info
    	result = response.readEntity(String.class);
    	statusCode = response.getStatus();
        statusType = response.getStatusInfo();  
        if(statusCode == 404) {
        	appResult = "OK";
        } else {
        	appResult = "ERROR";
        }
    	writer.printFormatted(GET, resource, result, statusCode, statusType, appResult, mediaType);
    	//////////////////////////////////////////////////////////////////////////////////
    	
    	//Request #9//////////////////////////////////////////////////////////////////////
    	resource = "measureTypes";
    	response = makeRequest(GET, resource, mediaType);
    	//Get response info
    	result = response.readEntity(String.class);
    	statusCode = response.getStatus();
        statusType = response.getStatusInfo();  
        

    	count = 0;
    	if(mediaType.equals(MediaType.APPLICATION_XML)) {
    		count = queryXml(result, "count(//measureType)");
    	} else if(mediaType.equals(MediaType.APPLICATION_JSON)) {
    		count = queryJsonSize(result);
    	}
    	writer.write("Total number of measure types: " + count+ "\n");
    	
    	if (count > 2) {
    		appResult = "OK";
    	} else {
    		appResult = "ERROR";
    	}
    	writer.printFormatted(GET, resource, result, statusCode, statusType, appResult, mediaType);
    	//////////////////////////////////////////////////////////////////////////////////
    	
    	
    	
    	writer.closeWriting();
    }
    
    private Response makeRequest(String method, String resource, String mediaType, String content) {
    	Response response = null;
    	if(method.equals(GET)) {
        	response = service.path(resource).request().accept(mediaType).get();
        } else if(method.equals(PUT)) {
        	response = service.path(resource).request().accept(mediaType).put(Entity.entity(content, mediaType));
        } else if(method.equals(POST)) {
        	response = service.path(resource).request().accept(mediaType).post(Entity.entity(content, mediaType));
        } else if(method.equals(DELETE)) {
        	response = service.path(resource).request().delete();
        }
        return response;
    }
    
    /*for GET*/
    private Response makeRequest(String method, String resource, String mediaType) {
    	return makeRequest(method, resource, mediaType, null);
    }
       
    /*for DELETE*/
    private Response makeRequest(String method, String resource) {
    	return makeRequest(method, resource, null, null);
    }
      
    private int queryXml(String xmlString, String xpathString) {
    	int personId = -1;
    	try {
    		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docBuilder.parse(new InputSource(new StringReader(xmlString)));
			
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			XPathExpression expr = xpath.compile(xpathString);
			personId = Integer.parseInt((String)expr.evaluate(doc, XPathConstants.STRING));
			
		} catch (SAXException | IOException | ParserConfigurationException | XPathExpressionException e) {
			e.printStackTrace();
		} 
    	return personId;
    }
    
    private int queryJson(String jsonString, int index) {
    	int res = -1;
    	ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode json = mapper.readTree(jsonString);
			if(index == -1) { //return how many elements
				res = json.size();
			} else if(index == -2) { //just one element, not an array
				res = json.path("idPerson").asInt();
			} else {
				res = json.get(index).path("idPerson").asInt();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
    }
    
    private int queryJsonId(String jsonString, int index) {
    	return queryJson(jsonString, index);
    }
    
    private int queryJsonId(String jsonString) {
    	return queryJson(jsonString, -2);
    }
    
    private int queryJsonSize(String jsonString) {
    	return queryJson(jsonString, -1);
    }
}