Author: Patryk Bandurski
WWW: www.profit-online.pl

Description:
SOAP Router designed to work with Community Edition. In order to use this utility your WSDL file and XSD schemas needs to be seperate.

## Usage
### Maven Dependency
First import maven dependency to SOAP Router
```xml
<dependency>
	<groupId>pl.profit-online</groupId>
	<artifactId>soap-router</artifactId>
	<version>1.0.0</version>
</dependency>
```
### Router Configuration
In application xml file add beans secion like below:
```xml
   <spring:beans>
   		<spring:bean id="SoapRouterConfiguration" name="SoapRouterConfiguration" class="pl.profitonline.soap.router.SoapRouterConfiguration" scope="singleton">
        	<spring:property name="wsdlFile" value="wsdl/weather.wsdl"/>
            <spring:property name="port" value="GlobalWeatherSoap" />
            <spring:property name="service" value="GlobalWeather"></spring:property>
            <spring:property name="schemaFiles">
            	<spring:list>
            		<spring:value>wsdl/schema/weather.xsd</spring:value>
            	</spring:list>
            </spring:property>
        </spring:bean>
        <spring:bean id="SoapRouter" name="SoapRouter" class="pl.profitonline.soap.router.SoapRouter" scope="singleton">
            <spring:constructor-arg index="0" ref="SoapRouterConfiguration"/>
        </spring:bean>
    </spring:beans>
``` 
Bean SoapRouterConfiguration is responsible for holding basic SOAP Router configuration. Following properties you need to provide:
* **wsdlFile** location of wsdl file
* **port** wsdl port name
* **service** wsdl service name
* **schemaFiles** list of schemas' locations

Bean SoupRouter is responsible for routing incoming soap envelopes to proper private flows implementing logic.

### Enable SOAP Routing
To enable SOAP routing place following XML after HTTP listener connector
```xml
<component doc:name="SOAP Router>"
    <spring-object bean="SoapRouter"/>
</component>
```

Create private flows for each operation published in WSDL file. Naming convention is as follows:
- /
- Operation name
- /
- Service name
- /
- Port name
- /api

Example:
```xml
 <flow name="/GetCitiesByCountry/GlobalWeather/GlobalWeatherSoap/api">
```

 
# New Features!

  - Route based on operation name (SOAPAction header)
  - Validate incoming messages
  - Validate outgoing messages