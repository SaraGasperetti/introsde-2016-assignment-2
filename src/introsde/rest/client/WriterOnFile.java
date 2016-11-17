package introsde.rest.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.StatusType;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.fasterxml.jackson.databind.ObjectMapper;

public class WriterOnFile {
    private static WriterOnFile instance;
    PrintWriter writer;
    private final static int INDENT = 4;
	private static int count = 1;
    private final static String xmlFile = "client-server-xml.log";
	private final static String jsonFile = "client-server-json.log";

    private WriterOnFile(String mediaType) {
        try{
        	if(mediaType.equals(MediaType.APPLICATION_XML)) {
        		writer = new PrintWriter(xmlFile, "UTF-8");
        	} else if(mediaType.equals(MediaType.APPLICATION_JSON)) {
        		writer = new PrintWriter(jsonFile, "UTF-8");
        	}
        } catch (Exception e) {
        	System.out.println(e);
        }
    }

    public static WriterOnFile getWriter(String fileName) {
    	if(instance == null) { 
    		instance = new WriterOnFile(fileName);
    	}
        return instance;    
    }

    public void write(String text){
    	writer.println(text);
    	//System.out.println(text);
    }
    
    public void closeWriting(){
        writer.close();
        instance = null;
        count = 1;
    }
    
    public String printFormatted(String method, String resource, String result, int statusCode, StatusType statusType, String appResult, String mediaType) {
    	
    	this.write("Request #" + count + ": " + method + " " + MyClient.getBaseURI() + resource);
    	if(!method.equals(MyClient.DELETE)) {
    		this.write("Accept: " + mediaType);
    	}
    	if(!method.equals(MyClient.DELETE) && !method.equals(MyClient.GET)) {
    		this.write("Content-Type: " + mediaType);
    	}
    	this.write("==> Result: " + appResult);
    	this.write("==> HTTP Status: " + statusCode + " " + statusType);
    	
    	if(appResult != "ERROR" && result != null && !result.isEmpty()) {
    		if(mediaType.equals(MediaType.APPLICATION_XML)) {
    			this.printXmlFormat(result);
        	} else if(mediaType.equals(MediaType.APPLICATION_JSON)) {
        		this.printJsonFormat(result);
        	}
    	}
    	
    	this.write("\n");
    	count++;
    	return result;
    }
    
    public void printXmlFormat(String input) {
        try {
            Source xmlInput = new StreamSource(new StringReader(input));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", INDENT);
            Transformer transformer = transformerFactory.newTransformer(); 
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(xmlInput, xmlOutput);
            this.write(xmlOutput.getWriter().toString());
        } catch (Exception e) {
            throw new RuntimeException(e); // simple exception handling, please review it
        }
    }
    
    public void printJsonFormat(String input) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			Object json = mapper.readValue(input, Object.class);
			this.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

}
