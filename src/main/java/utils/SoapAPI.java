package utils;

//import com.sun.xml.internal.ws.resources.SoapMessages;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SoapAPI {
	final static Logger log = LogManager.getLogger(SoapAPI.class.getClass());
    /**
     * constructor
     */
    public SoapAPI() {

    }

    /**
     * call login service api
     * @param soapEndpointUrl
     * @param soapAction
     * @param name
     * @param password
     * @return
     */
    public static String callLoginService(String soapEndpointUrl, String soapAction, String name, String password) {
        String result = "";
        try {
            // Create SOAP Connection
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();

            // Send SOAP Message to SOAP Server
            SOAPMessage soapResponse = soapConnection.call(createLoginRequest(soapAction, name, password), soapEndpointUrl);
            // response to string
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            soapResponse.writeTo(out);
            String responseMessage = new String(out.toByteArray());
            System.out.println("SOAP Response:"+responseMessage);

            //Parser that produces DOM object trees from XML content
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            //API to obtain DOM Document instance
            DocumentBuilder builder = null;
            //Create DocumentBuilder with default configuration
            builder = factory.newDocumentBuilder();

            //Parse the content to Document object
            Document doc = builder.parse(new InputSource(new StringReader(responseMessage)));
            doc.getDocumentElement().normalize();
            
            Node logNode = doc.getElementsByTagName("soapenv:Envelope").item(0).getFirstChild().getFirstChild().getFirstChild();
            result = logNode.getTextContent();
            soapConnection.close();
        } catch (Exception e) {
        	log.debug(e.toString());
            Alert alert = new Alert(Alert.AlertType.ERROR, "callLoginService"+e.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
        return result;
    }

    /**
     * call query service api
     * @param soapEndpointUrl
     * @param soapAction
     * @param query
     * @return
     */
    public static String callQueryService(String soapEndpointUrl, String soapAction, String query, String sessionid) {
        String result = "";
        try {
            // Create SOAP Connection
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();

            // Send SOAP Message to SOAP Server
            System.out.println("Inside callQueryService");
            SOAPMessage soapResponse = soapConnection.call(createQueryRequest(soapAction, query, sessionid), soapEndpointUrl);
            // response to string
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            soapResponse.writeTo(out);
            String responseMessage = new String(out.toByteArray());
            System.out.println("Response Message:"+responseMessage);
            
            if(responseMessage.contains("soapenv:Fault")){
                int startIndex = responseMessage.indexOf("<soapenv:Text");
                int endIndex  = responseMessage.indexOf("</soapenv:Text>",startIndex+1);
                int closeTagIndex = responseMessage.indexOf(">",startIndex+1);
                String faultMessage = responseMessage.substring(closeTagIndex+1, endIndex-1);
                log.debug(faultMessage);
                Alert alert = new Alert(Alert.AlertType.ERROR, "callQueryService"+faultMessage, ButtonType.OK);
                alert.showAndWait();
            }

            //Parser that produces DOM object trees from XML content
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            //API to obtain DOM Document instance
            DocumentBuilder builder = null;
            //Create DocumentBuilder with default configuration
            builder = factory.newDocumentBuilder();

            //Parse the content to Document object
            Document doc = builder.parse(new InputSource(new StringReader(responseMessage)));
            doc.getDocumentElement().normalize();
            Node logNode = doc.getElementsByTagName("soapenv:Envelope").item(0).getFirstChild().getFirstChild().getFirstChild().getFirstChild();
            //Node logNode = doc.getElementsByTagName("typ:resp").item(0);
            result = logNode.getTextContent();
            System.out.println("Base64 Data:"+result);

            result = base64Decoding(result);

            soapConnection.close();
        } catch (Exception e) {
        	log.debug(e.toString());
            Alert alert = new Alert(Alert.AlertType.ERROR, "callQueryService"+e.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
        return result;
    }

    /**
     * create login request
     * @param soapAction
     * @param name
     * @param password
     * @return
     * @throws Exception
     */
    private static SOAPMessage createLoginRequest(String soapAction, String name, String password) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage soapMessage = messageFactory.createMessage();
        soapMessage.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");

        SOAPPart soapPart = soapMessage.getSOAPPart();
        // create namespace
        String theNamespace = "pub";
        String theNamespaceURI = "http://xmlns.oracle.com/oxp/service/PublicReportService";
        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(theNamespace, theNamespaceURI);

        // SOAP Body
        SOAPBody soapBody = envelope.getBody();
        SOAPElement loginElem = soapBody.addChildElement("login", theNamespace);
        SOAPElement nameElem = loginElem.addChildElement("username", theNamespace);
        nameElem.addTextNode(name);
        SOAPElement passwordElem = loginElem.addChildElement("password", theNamespace);
        passwordElem.addTextNode(password);

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", "login");

        soapMessage.saveChanges();
        /* Print the request message, just for debugging purposes */
        System.out.println("Request SOAP Message:");
        soapMessage.writeTo(System.out);
        System.out.println("\n");

        return soapMessage;
    }

    /**
     * create query request
     * @param soapAction
     * @param query
     * @param sessionid
     * @return
     * @throws Exception
     */
    private static SOAPMessage createQueryRequest(String soapAction, String query, String sessionid) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage soapMessage = messageFactory.createMessage();
        soapMessage.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");

        SOAPPart soapPart = soapMessage.getSOAPPart();
        System.out.println("Inside createQueryRequest");
        // create namespace
        String theNamespace = "pub";
        String theNamespaceURI = "http://xmlns.oracle.com/oxp/service/PublicReportService";
        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(theNamespace, theNamespaceURI);

        // SOAP Body
        SOAPBody soapBody = envelope.getBody();
        SOAPElement runRepInSess = soapBody.addChildElement("runReportInSession",theNamespace);
        SOAPElement reportRequest = runRepInSess.addChildElement("reportRequest",theNamespace);
        SOAPElement parameterNameValues = reportRequest.addChildElement("parameterNameValues",theNamespace);
        SOAPElement listOfParamValues = parameterNameValues.addChildElement("listOfParamValues",theNamespace);
        SOAPElement paramValName = listOfParamValues.addChildElement("name",theNamespace);
        paramValName.addTextNode("query1");
        SOAPElement paramValValues = listOfParamValues.addChildElement("values",theNamespace);
        SOAPElement paramValValueItem = paramValValues.addChildElement("item",theNamespace);
        String cDat= "<![CDATA[ ";
        paramValValueItem.addTextNode(cDat+base64Encoding(query)+" "+"]]");
        
        SOAPElement attributeLocale = reportRequest.addChildElement("attributeLocale",theNamespace);
        attributeLocale.addTextNode("en-US");
        SOAPElement attributeTemplate = reportRequest.addChildElement("attributeTemplate",theNamespace);
        attributeTemplate.addTextNode("Default");
        SOAPElement reportAbsolutePath = reportRequest.addChildElement("reportAbsolutePath",theNamespace);
        //reportAbsolutePath.addTextNode("/Custom/CloudTools/V2/SQLConnectReportCSV.xdo");
        reportAbsolutePath.addTextNode("/Custom/Test/CloudSqlReportCSV.xdo");
        
//        SOAPElement queryElem = soapBody.addChildElement("queryExec", theNamespace);
//        SOAPElement paramElem = queryElem.addChildElement("queryparam", theNamespace);
//        paramElem.addTextNode("Test1");
//        SOAPElement textElem = queryElem.addChildElement("queryText", theNamespace);
        SOAPElement bipSessionToken = runRepInSess.addChildElement("bipSessionToken",theNamespace);
        bipSessionToken.addTextNode(sessionid);
        System.out.println(query);
        String encodedQuery = base64Encoding(query);
        //textElem.addTextNode(encodedQuery);

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", "runReportInSession");
        //headers.addHeader("Cookie", sessionid);

        soapMessage.saveChanges();
        System.out.println("Request SOAP Message:");
        soapMessage.writeTo(System.out);
        System.out.println("\n");
        return soapMessage;
    }

    /**
     * base64 encoding
     * @param inputString
     * @return
     */
    private static String base64Encoding(String inputString) {
        return Base64.getEncoder().encodeToString(inputString.getBytes());
    }

    /**
     * base64 decoding
     * @param encodedString
     * @return
     */
    private static String base64Decoding(String encodedString) {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        return new String(decodedBytes);
    }
}
