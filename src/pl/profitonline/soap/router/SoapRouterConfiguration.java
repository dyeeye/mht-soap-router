package pl.profitonline.soap.router;

import java.util.ArrayList;
import java.util.List;

public class SoapRouterConfiguration {
	
	private String wsdlFile;
	private String service;
	private String port;
	private List<String> schemaFiles = new ArrayList<>();
	
	public String getWsdlFile() {
		return wsdlFile;
	}
	public void setWsdlFile(String wsdlFile) {
		this.wsdlFile = wsdlFile;
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public List<String> getSchemaFiles() {
		return schemaFiles;
	}
	public void setSchemaFiles(List<String> schemaFiles) {
		this.schemaFiles = schemaFiles;
	}
	
}
