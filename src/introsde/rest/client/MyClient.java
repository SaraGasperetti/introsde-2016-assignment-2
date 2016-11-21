package introsde.rest.client;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

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
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
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

    public static void main(String[] args) throws JsonProcessingException, IOException, NumberFormatException, XPathExpressionException, ParserConfigurationException, SAXException {
        MyClient myClient = new MyClient();

        myClient.makeAllRequests(MediaType.APPLICATION_XML);
        myClient.makeAllRequests(MediaType.APPLICATION_JSON);
    }

    protected static URI getBaseURI() {
        return UriBuilder.fromUri(
                //"http://127.0.1.1:5700/sdelab/").build();
        		"https://introsde2016-assignment-2.herokuapp.com/sdelab/").build();
    }

    private void makeAllRequests(String mediaType) throws JsonProcessingException, IOException, NumberFormatException, XPathExpressionException, ParserConfigurationException, SAXException {

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

        //Step 3.1//////////////////////////////////////////////////////////////////////
        resource = "person";
        response = makeRequest(GET, resource, mediaType);
        //Get response info
        result = response.readEntity(String.class);
        statusCode = response.getStatus();
        statusType = response.getStatusInfo();

        int firstPersonId = 1;
        int lastPersonId = 1;
        int count = 0;
        if (mediaType.equals(MediaType.APPLICATION_XML)) {
            count = Integer.parseInt(getXmlValue(result, "count(//person)"));
            firstPersonId = Integer.parseInt(getXmlValue(result, "//person[1]/idPerson"));
            lastPersonId = Integer.parseInt(getXmlValue(result, "//person[last()]/idPerson"));
        } else if (mediaType.equals(MediaType.APPLICATION_JSON)) {
            count = getJsonSize(result);
            firstPersonId = getJsonIdAtIndex(result, 0, "idPerson");
            lastPersonId = getJsonIdAtIndex(result, count - 1, "idPerson");
        }
        writer.write("First person in db has id " + firstPersonId);
        writer.write("Last person in db has id " + lastPersonId);
        writer.write("Total number of person: " + count + "\n");

        appResult = (count > 2) ? "OK" : "ERROR";

        writer.printFormatted(GET, resource, result, statusCode, statusType, appResult, mediaType);
    	//////////////////////////////////////////////////////////////////////////////////

        //Step 3.2//////////////////////////////////////////////////////////////////////
        resource = "person/" + firstPersonId;
        response = makeRequest(GET, resource, mediaType);
        //Get response info
        result = response.readEntity(String.class);
        statusCode = response.getStatus();
        statusType = response.getStatusInfo();
        appResult = (statusCode == 200 || statusCode == 202) ? "OK" : "ERROR";

        writer.printFormatted(GET, resource, result, statusCode, statusType, appResult, mediaType);
    	//////////////////////////////////////////////////////////////////////////////////

        //Step 3.3//////////////////////////////////////////////////////////////////////
        resource = "person/" + firstPersonId;
        String expectedFirstname = null;
        if (mediaType.equals(MediaType.APPLICATION_XML)) {
            expectedFirstname = "<firstname>John</firstname>";
            content = "<person>" + expectedFirstname + "</person>";
        } else if (mediaType.equals(MediaType.APPLICATION_JSON)) {
            expectedFirstname = "\"firstname\":\"John\"";
            content = "{" + expectedFirstname + "}";
        }

        response = makeRequest(PUT, resource, mediaType, content);
        //Get response info
        result = response.readEntity(String.class);
        statusCode = response.getStatus();
        statusType = response.getStatusInfo();
        appResult = (result.contains(expectedFirstname)) ? "OK" : "ERROR";

        writer.printFormatted(PUT, resource, result, statusCode, statusType, appResult, mediaType);
    	//////////////////////////////////////////////////////////////////////////////////

        //Step 3.4//////////////////////////////////////////////////////////////////////
        resource = "person/";
        if (mediaType.equals(MediaType.APPLICATION_XML)) {
            content = "<person><firstname>Chuck</firstname><lastname>Norris</lastname>"
                    + "<birthdate>01/01/1945</birthdate>"
                    + "<healthprofile>"
                    + "<measureType><measure>weight</measure><value>78.9</value></measureType>"
                    + "<measureType><measure>height</measure><value>172</value></measureType>"
                    + "</healthprofile></person>";
        } else if (mediaType.equals(MediaType.APPLICATION_JSON)) {
            content = "{\"firstname\":\"Chuck\", \"lastname\":\"Norris\","
                    + "\"birthdate\":\"01/01/1945\","
                    + "\"measureType\":[{\"value\":\"78.9\",\"measure\":\"weight\"},"
                    + "{\"value\":\"1.72\",\"measure\":\"height\"}]}";
        }

        response = makeRequest(POST, resource, mediaType, content);
        //Get response info
        result = response.readEntity(String.class);
        statusCode = response.getStatus();
        statusType = response.getStatusInfo();

        int newId = -1;
        if (mediaType.equals(MediaType.APPLICATION_XML)) {
            newId = Integer.parseInt(getXmlValue(result, "//person/idPerson"));
        } else if (mediaType.equals(MediaType.APPLICATION_JSON)) {
            newId = getJsonId(result);
        }
        writer.write("The new id is: " + newId);
        appResult = ((statusCode == 200 || statusCode == 201 || statusCode == 202) && newId != -1) ? "OK" : "ERROR";

        writer.printFormatted(POST, resource, result, statusCode, statusType, appResult, mediaType);
    	//////////////////////////////////////////////////////////////////////////////////

        //Step 3.5//////////////////////////////////////////////////////////////////////
        resource = "person/" + newId;
        response = makeRequest(DELETE, resource);
        //Get response info
        statusCode = response.getStatus();
        statusType = response.getStatusInfo();
        appResult = "OK";

        writer.printFormatted(DELETE, resource, null, statusCode, statusType, appResult, mediaType);

    	//////////////////////////////////////////////////////////////////////////////////
        resource = "person/" + newId;
        response = makeRequest(GET, resource, mediaType);
        //Get response info
        result = response.readEntity(String.class);
        statusCode = response.getStatus();
        statusType = response.getStatusInfo();
        appResult = (statusCode == 404) ? "OK" : "ERROR";

        writer.printFormatted(GET, resource, result, statusCode, statusType, appResult, mediaType);
    	//////////////////////////////////////////////////////////////////////////////////

        //Step 3.6//////////////////////////////////////////////////////////////////////
        resource = "measureTypes";
        response = makeRequest(GET, resource, mediaType);
        //Get response info
        result = response.readEntity(String.class);
        statusCode = response.getStatus();
        statusType = response.getStatusInfo();

        count = 0;
        List<String> measureTypes = null;
        if (mediaType.equals(MediaType.APPLICATION_XML)) {
            count = Integer.parseInt(getXmlValue(result, "count(//measureType)"));
            measureTypes = getXmlArray(result, "//measureType");
        } else if (mediaType.equals(MediaType.APPLICATION_JSON)) {
            count = getJsonSize(result);
            measureTypes = getJsonArray(result);
        }
        writer.write("Total number of measure types: " + count + "\n");
        writer.write("The measures are: " + measureTypes);
        appResult = (count > 2) ? "OK" : "ERROR";

        writer.printFormatted(GET, resource, result, statusCode, statusType, appResult, mediaType);
    	//////////////////////////////////////////////////////////////////////////////////

        //Step 3.7//////////////////////////////////////////////////////////////////////
        appResult = "ERROR";
        int mid = -1;
        String measureType = "";
        List<String> resources = new LinkedList<>();
        List<String> results = new LinkedList<>();
        List<Integer> statusCodes = new LinkedList<>();
        List<StatusType> statusTypes = new LinkedList<>();

        for (String measure : measureTypes) {
            resource = "person/" + firstPersonId + "/" + measure;
            response = makeRequest(GET, resource, mediaType);
            //Get response info
            result = response.readEntity(String.class);
            statusCode = response.getStatus();
            statusType = response.getStatusInfo();

            if (result.contains("mid")) {
                appResult = "OK";
                if (mediaType.equals(MediaType.APPLICATION_XML)) {
                    mid = Integer.parseInt(getXmlValue(result, "//mid"));
                    measureType = measure;
                } else if (mediaType.equals(MediaType.APPLICATION_JSON)) {
                    mid = getJsonIdAtIndex(result, 0, "mid");
                    measureType = measure;
                }

            }
            resources.add(resource);
            results.add(result);
            statusCodes.add(statusCode);
            statusTypes.add(statusType);
        }

        for (int i = 0; i < resources.size(); i++) {
            writer.printFormatted(GET, resources.get(i), results.get(i), statusCodes.get(i), statusTypes.get(i), appResult, mediaType);
        }

    	//////////////////////////////////////////////////////////////////////////////////
        appResult = "ERROR";
        resources.clear();
        results.clear();
        statusCodes.clear();
        statusTypes.clear();
        for (String measure : measureTypes) {
            resource = "person/" + lastPersonId + "/" + measure;
            response = makeRequest(GET, resource, mediaType);
            //Get response info
            result = response.readEntity(String.class);
            statusCode = response.getStatus();
            statusType = response.getStatusInfo();
            if (result.contains("mid")) {
                appResult = "OK";
            }
            resources.add(resource);
            results.add(result);
            statusCodes.add(statusCode);
            statusTypes.add(statusType);
        }
        for (int i = 0; i < resources.size(); i++) {
            writer.printFormatted(GET, resources.get(i), results.get(i), statusCodes.get(i), statusTypes.get(i), appResult, mediaType);
        }
    	//////////////////////////////////////////////////////////////////////////////////

        //Step 3.8//////////////////////////////////////////////////////////////////////
        resource = "person/" + firstPersonId + "/" + measureType + "/" + mid;
        response = makeRequest(GET, resource, mediaType);
        //Get response info
        result = response.readEntity(String.class);
        statusCode = response.getStatus();
        statusType = response.getStatusInfo();
        appResult = (statusCode == 200) ? "OK" : "ERROR";

        writer.printFormatted(GET, resource, result, statusCode, statusType, appResult, mediaType);
        //////////////////////////////////////////////////////////////////////////////////

    	//Step 3.9//////////////////////////////////////////////////////////////////////
        resource = "person/" + firstPersonId + "/" + measureTypes.get(0);
        response = makeRequest(GET, resource, mediaType);
        //Get response info
        result = response.readEntity(String.class);
        statusCode = response.getStatus();
        statusType = response.getStatusInfo();

        if (mediaType.equals(MediaType.APPLICATION_XML)) {
            count = Integer.parseInt(getXmlValue(result, "count(//healthMeasureHistory)"));
        } else if (mediaType.equals(MediaType.APPLICATION_JSON)) {
            count = getJsonSize(result);
        }
        appResult = "OK";
        writer.write("There are " + count + " measures of type " + measureTypes.get(0) + " for person " + firstPersonId);
        writer.printFormatted(GET, resource, result, statusCode, statusType, appResult, mediaType);

        //////////////////////////////////////////////////////////////////////////////////
        resource = "person/" + firstPersonId + "/" + measureTypes.get(0);
        if (mediaType.equals(MediaType.APPLICATION_XML)) {
            content = "<measureType><value>72</value></measureType>";
        } else if (mediaType.equals(MediaType.APPLICATION_JSON)) {
            content = "{\"value\":\"72\"}";
        }

        response = makeRequest(POST, resource, mediaType, content);
        //Get response info
        result = response.readEntity(String.class);
        statusCode = response.getStatus();
        statusType = response.getStatusInfo();
        appResult = (statusCode == 200) ? "OK" : "ERROR";

        writer.printFormatted(POST, resource, result, statusCode, statusType, appResult, mediaType);

        //////////////////////////////////////////////////////////////////////////////////
        int count1 = -1;
        resource = "person/" + firstPersonId + "/" + measureTypes.get(0);
        response = makeRequest(GET, resource, mediaType);
        //Get response info
        result = response.readEntity(String.class);
        statusCode = response.getStatus();
        statusType = response.getStatusInfo();

        if (mediaType.equals(MediaType.APPLICATION_XML)) {
            count1 = Integer.parseInt(getXmlValue(result, "count(//healthMeasureHistory)"));
        } else if (mediaType.equals(MediaType.APPLICATION_JSON)) {
            count1 = getJsonSize(result);
        }
        appResult = ((count1 - count) == 1) ? "OK" : "ERROR";
        writer.write("There are " + count1 + " measures of type " + measureTypes.get(0) + " for person " + firstPersonId);
        writer.printFormatted(GET, resource, result, statusCode, statusType, appResult, mediaType);
        //////////////////////////////////////////////////////////////////////////////////

    	//Step 3.10//////////////////////////////////////////////////////////////////////
        resource = "person/" + firstPersonId + "/" + measureType + "/" + mid;
        if (mediaType.equals(MediaType.APPLICATION_XML)) {
            content = "<healthMeasureHistory><value>1.8</value></healthMeasureHistory>";
        } else if (mediaType.equals(MediaType.APPLICATION_JSON)) {
            content = "{\"value\":\"1.8\"}";
        }

        response = makeRequest(PUT, resource, mediaType, content);
        //Get response info
        result = response.readEntity(String.class);
        statusCode = response.getStatus();
        statusType = response.getStatusInfo();
        appResult = (statusCode == 200) ? "OK" : "ERROR";

        writer.printFormatted(PUT, resource, result, statusCode, statusType, appResult, mediaType);

        //////////////////////////////////////////////////////////////////////////////////
        Double newValue = -1.0;
        resource = "person/" + firstPersonId + "/" + measureType;
        response = makeRequest(GET, resource, mediaType);
        //Get response info
        result = response.readEntity(String.class);
        statusCode = response.getStatus();
        statusType = response.getStatusInfo();

        if (mediaType.equals(MediaType.APPLICATION_XML)) {
            newValue = Double.parseDouble(getXmlValue(result, "//healthMeasureHistory[mid[text()='" + mid + "']]/value"));
        } else if (mediaType.equals(MediaType.APPLICATION_JSON)) {
            newValue = getJsonValueByMid(result, mid);
        }
        appResult = (newValue == 1.8) ? "OK" : "ERROR";
        writer.write("The updated value is " + newValue);
        writer.printFormatted(GET, resource, result, statusCode, statusType, appResult, mediaType);

        //////////////////////////////////////////////////////////////////////////////////
    	//Step 3.11//////////////////////////////////////////////////////////////////////
        resource = "person/" + firstPersonId + "/weight?before=20-11-2016&after=10-11-1990";
        response = makeRequest(GET, resource, mediaType);
        //Get response info
        result = response.readEntity(String.class);
        statusCode = response.getStatus();
        statusType = response.getStatusInfo();

        appResult = (statusCode == 200 && result.contains("mid")) ? "OK" : "ERROR";
        writer.printFormatted(GET, resource, result, statusCode, statusType, appResult, mediaType);

        //////////////////////////////////////////////////////////////////////////////////
       	//Step 3.11//////////////////////////////////////////////////////////////////////
        resource = "person?measureType=weight&max=90&min=86";
        response = makeRequest(GET, resource, mediaType);
        //Get response info
        result = response.readEntity(String.class);
        statusCode = response.getStatus();
        statusType = response.getStatusInfo();

        appResult = (statusCode == 200 && result.contains("person")) ? "OK" : "ERROR";
        writer.printFormatted(GET, resource, result, statusCode, statusType, appResult, mediaType);

        //////////////////////////////////////////////////////////////////////////////////
        writer.closeWriting();
    }

    private Response makeRequest(String method, String resource, String mediaType, String content) {
        Response response = null;
        if (method.equals(GET)) {

            //if the request contains query parameters
            if (resource.contains("?")) {
                String[] tokens = resource.split("[?]");
                WebTarget target = service.path(tokens[0]);
                String[] params = tokens[1].split("&");
                for (String p : params) {
                    String[] map = p.split("=");
                    target = target.queryParam(map[0], map[1]);
                }
                response = target.request().accept(mediaType).get();
            } else { //if the request DOES NOT contain query parameters
                response = service.path(resource).request().accept(mediaType).get();
            }
        } else if (method.equals(PUT)) {
            response = service.path(resource).request().accept(mediaType).put(Entity.entity(content, mediaType));
        } else if (method.equals(POST)) {
            response = service.path(resource).request().accept(mediaType).post(Entity.entity(content, mediaType));
        } else if (method.equals(DELETE)) {
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

    //Xml query methods//////////////////////////////////////////////////////////////
    private Document getXmlTree(String xmlString) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = docBuilder.parse(new InputSource(new StringReader(xmlString)));
        return doc;
    }

    private XPathExpression getXpathExpr(String xpathString) throws XPathExpressionException {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile(xpathString);
        return expr;
    }

    private String getXmlValue(String xmlString, String xpathString) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        Document doc = getXmlTree(xmlString);
        XPathExpression expr = getXpathExpr(xpathString);
        String value = ((String) expr.evaluate(doc, XPathConstants.STRING));
        return value;
    }

    private List<String> getXmlArray(String xmlString, String xpathString) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        Document doc = getXmlTree(xmlString);
        XPathExpression expr = getXpathExpr(xpathString);
        NodeList nodes = ((NodeList) expr.evaluate(doc, XPathConstants.NODESET));

        List<String> values = new LinkedList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            values.add(nodes.item(i).getTextContent());
        }
        return values;
    }

    //Json query methods/////////////////////////////////////////////////////////////////////
    private JsonNode getJsonTree(String jsonString) throws JsonProcessingException, IOException {
        return new ObjectMapper().readTree(jsonString);
    }

    private int getJsonIdAtIndex(String jsonString, int index, String path) throws JsonProcessingException, IOException {
        return getJsonTree(jsonString).get(index).path(path).asInt();
    }

    private int getJsonId(String jsonString) throws JsonProcessingException, IOException {
        return getJsonTree(jsonString).path("idPerson").asInt();
    }

    private int getJsonSize(String jsonString) throws JsonProcessingException, IOException {
        return getJsonTree(jsonString).size();
    }

    private List<String> getJsonArray(String jsonString) throws JsonProcessingException, IOException {
        List<String> values = new LinkedList<>();
        JsonNode tree = getJsonTree(jsonString);
        int size = tree.size();
        for (int i = 0; i < size; i++) {
            values.add(tree.get(i).path("value").asText());
        }
        return values;
    }

    private Double getJsonValueByMid(String jsonString, int mid) throws JsonProcessingException, IOException {
        JsonNode tree = getJsonTree(jsonString);
        int size = tree.size();
        for (int i = 0; i < size; i++) {
            if ((tree.get(i).path("mid").asInt()) == mid) {
                return tree.get(i).path("value").asDouble();
            }
        }
        return null;
    }
}
