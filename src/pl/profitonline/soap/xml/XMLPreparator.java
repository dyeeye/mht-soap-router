package pl.profitonline.soap.xml;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import pl.profitonline.soap.exception.SoapException;
import pl.profitonline.soap.exception.SoapValidationException;

public class XMLPreparator {
	
	public static String unenvelopeMessage(String soap, String message) throws DocumentException, SoapException {
		Document document = DocumentHelper.parseText(soap);
		Element soapEnvelope = document.getRootElement();
		Node body =  soapEnvelope.selectSingleNode(String.format("/Envelope/*[local-name() = 'Body']/*[local-name()='%s']", message.substring(message.indexOf(":")+1)));
		if(body == null) throw new SoapValidationException("Malformed SOAP Envelope");
		return body.asXML();
	}

}
