package pl.profitonline.soap.xml;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import pl.profitonline.soap.exception.SoapException;

public class XMLPreparator {
	
	public static String unenvelopeMessage(String soap) throws DocumentException, SoapException {
		Document document = DocumentHelper.parseText(soap);
		Element soapEnvelope = document.getRootElement();
		Node body =  soapEnvelope.selectSingleNode("/Envelope/*[local-name() = 'Body']/*");
		if(body == null) throw new SoapException("Malformed SOAP Envelope");
		
		return body.asXML();
	}

}
