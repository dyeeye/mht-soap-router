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
	private String wsdl;
	private List<String> schemas;
	
	public SoapRouter(SoapRouterConfiguration configuration) throws SoapRouterException {
		this.configuration = configuration;
		
		loadFiles();
	}

	@SuppressWarnings("deprecation")
	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		
		// prepare flow name
		String action = (String)eventContext.getMessage().getInboundProperty("SOAPAction");
		if(action == null) throw new MissingHeaderException("SOAPAction");
		
		Pattern pattern = Pattern.compile("\"(.*)/(.*)\"");
		Matcher m = pattern.matcher(action);
		
		String operation = null;
		
		if(m.find()) {
			operation = m.group(2);
		}
		
		//extract flow name
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
		
		//validate incoming message
		validateBody(eventContext.getMessage().getPayloadAsString(), true);
		
		//sent validated message to proper private flow
		MuleEvent inEvent = RequestContext.getEvent();
		MuleEvent outEvent = flowOperation.process(inEvent);
		
		eventContext.getMessage().setOutboundProperty("Content-Type", "text/xml");
		String outputEnvelope =  outEvent.getMessage().getPayloadAsString();
		
		//validate response message
		validateBody(eventContext.getMessage().getPayloadAsString(), false);
		
		return outputEnvelope;
	}
	
	private void validateBody(String soapEnvelope, boolean rethrowError) throws Exception {
		String soapBody = XMLPreparator.unenvelopeMessage(soapEnvelope);
		
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
			wsdl = getResource(configuration.getWsdlFile());
		} catch (IOException e) {
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
