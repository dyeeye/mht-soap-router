package pl.profitonline.soap.xml;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

import pl.profitonline.soap.exception.SoapValidationException;

public class XMLValidator {
	
	public static boolean validate(String xml, List<String> schemas) throws SoapValidationException, IOException, DocumentException, ParserConfigurationException, SAXException {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		StreamSource[] schemasSource = schemas.stream().map(StringReader::new).map(StreamSource::new).toArray(size -> new StreamSource[size]);
		Schema schema = schemaFactory.newSchema(schemasSource);
		
		Validator validator = schema.newValidator();
		try{
		validator.validate(new StreamSource(new StringReader(xml)));
		}catch(SAXException e) {
			throw new SoapValidationException(e.getMessage());
		}
	
		return true;
	}
}
