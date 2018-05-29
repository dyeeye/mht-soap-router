package pl.profitonline.soap.router;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.mule.RequestContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.construct.Flow;

import pl.profitonline.soap.exception.MissingHeaderException;
import pl.profitonline.soap.exception.NotImplementedOperationException;
import pl.profitonline.soap.exception.SoapRouterException;
import pl.profitonline.soap.exception.SoapValidationException;
import pl.profitonline.soap.xml.XMLPreparator;
import pl.profitonline.soap.xml.XMLValidator;

public class SoapRouter implements Callable {
	
	private SoapRouterConfiguration configuration;
	private Element wsdl;
	private List<String> schemas;
	
	public SoapRouter(SoapRouterConfiguration configuration) throws SoapRouterException {
		this.configuration = configuration;
		
		loadFiles();
	}

	@SuppressWarnings("deprecation")
	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		
		// match wsdl operation
		String action = (String)eventContext.getMessage().getInboundProperty("SOAPAction");
		if(action == null) throw new MissingHeaderException("SOAPAction");

		Node wsdlOperation = wsdl.selectSingleNode(String.format("/definitions/*[local-name()='binding'][@name='%s']//*[local-name()='operation'][@soapAction=%s]/../@name", 
				configuration.getPort(), action));
		String operation = null;
		if(wsdlOperation == null) {
			throw new SoapRouterException("Operation not found");
		}else {
			operation = wsdlOperation.getText();
		}
	
		//prepare flow name
		StringBuilder flowNameBuilder = new StringBuilder();
		flowNameBuilder
			.append("/")
			.append(operation)
			.append("/")
			.append(configuration.getService())
			.append("/")
			.append(configuration.getPort())
			.append("/api");
		
		
		//lookup private flow for give operation
		Flow flowOperation = (Flow) eventContext.getMuleContext().getRegistry().lookupFlowConstruct(flowNameBuilder.toString());
		if(flowOperation == null) throw new NotImplementedOperationException(flowNameBuilder.toString());
		
		String inMsg = wsdl.selectSingleNode(String.format("/definitions/*[local-name()= 'portType'][@name='%s']/*[local-name()='operation'][@name='%s']/*[local-name()='input']/@message", 
				configuration.getPort(), operation))
				.getText();

		Node inElementName = wsdl.selectSingleNode(String.format("/definitions/*[local-name()='message'][@name='%s']/*[local-name()='part']/@element", inMsg.substring(inMsg.indexOf(":")+1)));
		
		//validate incoming message
		validateBody(eventContext.getMessage().getPayloadAsString(), inElementName.getText(), true);
		
		//sent validated message to proper private flow
		MuleEvent inEvent = RequestContext.getEvent();
		MuleEvent outEvent = flowOperation.process(inEvent);
		
		eventContext.getMessage().setOutboundProperty("Content-Type", "text/xml");
		String outputEnvelope =  outEvent.getMessage().getPayloadAsString();
		
		//validate response message
		String outMsg = wsdl.selectSingleNode(String.format("/definitions/*[local-name()= 'portType'][@name='%s']/*[local-name()='operation'][@name='%s']/*[local-name()='output']/@message", 
				configuration.getPort(), operation))
				.getText();
		Node outElementName = wsdl.selectSingleNode(String.format("/definitions/*[local-name()='message'][@name='%s']/*[local-name()='part']/@element", outMsg.substring(outMsg.indexOf(":")+1)));
		
		validateBody(eventContext.getMessage().getPayloadAsString(), outElementName.getText(), false);
		
		return outputEnvelope;
	}
	
	private void validateBody(String soapEnvelope, String message, boolean rethrowError) throws Exception {
		String soapBody = XMLPreparator.unenvelopeMessage(soapEnvelope, message);
		
		try{
			XMLValidator.validate(soapBody, schemas);
		}catch(SoapValidationException e){
			if(rethrowError) {
				throw new SoapValidationException(e.getMessage());
			} else {
				throw new Exception(e.getMessage());
			}
		}
	}
	
	private String getResource(String filename) throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream io = classLoader.getResourceAsStream(filename);
		return IOUtils.toString(io, StandardCharsets.UTF_8.name());
	}
	
	private void loadFiles() throws SoapRouterException{
		// load WSDL
		try {
			Document wsdlDoc = DocumentHelper.parseText(getResource(configuration.getWsdlFile()));
			wsdl = wsdlDoc.getRootElement();
		} catch (IOException | DocumentException e) {
			throw new SoapRouterException(e.getMessage());
		}
		//load XSDs
		try {
			schemas = new ArrayList<>();
			for(String file : configuration.getSchemaFiles()) {
				schemas.add(getResource(file));
			}
		} catch (IOException e) {
			throw new SoapRouterException(e.getMessage());
		}
	}

}
