package utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import model.MainModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AppStatus {
	final static Logger log = LogManager.getLogger(AppStatus.class.getClass());

    private final String theFilePath = "appStatus.xml";
    private Map<String, Map<String, String>> theStatusMap;
    /**
     * static instance
     */
    private static AppStatus theInstance = new AppStatus();

    /**
     * internal constructor
     */
    private AppStatus() {

        readStatus();
    }

    /**
     * get instance
     * @return
     */
    public static AppStatus getInstance() { return theInstance; }

    /**
     * add or update view status
     * @param viewName
     * @param connName
     * @param queries
     */
    public void addOrUpdateStatus(String viewName, String connName, String queries) {
        Map<String, String> status = new HashMap<String, String>();
        status.put("connectionName", connName);
        status.put("queries", queries);
        theStatusMap.put(viewName, status);
    }

    /**
     * remove view status
     * @param viewName
     */
    public void removeStatus(String viewName) {
        theStatusMap.remove(viewName);
    }

    /**
     * get all view names
     * @return
     */
    public String [] getViewNames() {
        List result = theStatusMap.keySet().stream().sorted().
                collect(Collectors.toList());
        return (String[]) result.toArray(new String[0]);
    }

    /**
     * get connection name of query view
     * @param viewName
     * @return
     */
    public String getConnectionName(String viewName) {
        Map<String, String> statusInfo = theStatusMap.get(viewName);
        if(statusInfo != null)
            return statusInfo.get("connectionName");
        else
            return null;
    }

    /**
     * get queries of query view
     * @param viewName
     * @return
     */
    public String getQueries(String viewName) {
        Map<String, String> statusInfo = theStatusMap.get(viewName);
        if(statusInfo != null)
            return statusInfo.get("queries");
        else
            return null;
    }

    /**
     * internal read status from file
     * @return
     */
    private boolean readStatus() {
        theStatusMap = new HashMap<String, Map<String, String>>();
        try {
        	File f = new File(theFilePath);
        	if (!f.isFile()) {
            	System.out.println("Inside filecreate");
        		f.createNewFile();
        	}
        }catch(Exception e) {
        	log.debug(e.toString());
        	System.out.println(e.toString());
        	
        }

        Document dom;
        // Make an  instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // read xml file
            String curPath = System.getProperty("user.dir") +"/" +theFilePath;
            File file = new File(curPath);

            // use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("view");
            for(int i=0; i<nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                String viewName = getNodeTextValue("", (Element) node, "viewName");
                if(viewName.isEmpty())
                    continue;
                Map<String, String> viewInfoMap = new HashMap<String, String>();
                String value = getNodeTextValue("", (Element) node, "connectionName");
                viewInfoMap.put("connectionName", value);
                value = getNodeTextValue("", (Element) node, "queries");
                viewInfoMap.put("queries", value);

                theStatusMap.put(viewName, viewInfoMap);
            }
        } catch (Exception e) {
        	log.debug(e.toString());
        	System.out.println(e.toString());
            //Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            //alert.showAndWait();
            return false;
        }
        return true;
    }

    /**
     * Save application status into file
     */
    public void writeStatus() {
        Document dom;
        Element element = null;

        // instance of a DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use factory to get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create instance of DOM
            dom = db.newDocument();

            // create the root element
            Element rootElem = dom.createElement("views");

            String[] viewNameList = (String[]) theStatusMap.keySet().toArray(new String[0]);
            for(int i = 0; i< viewNameList.length; i++) {
                Map<String, String> viewInfo = theStatusMap.get(viewNameList[i]);

                element = dom.createElement("view");
                // status name
                Element subElem = dom.createElement("viewName");
                subElem.appendChild(dom.createTextNode(viewNameList[i]));
                element.appendChild(subElem);
                // connection name
                subElem = dom.createElement("connectionName");
                subElem.appendChild(dom.createTextNode(viewInfo.get("connectionName")));
                element.appendChild(subElem);
                // commands
                subElem = dom.createElement("queries");
                subElem.appendChild(dom.createTextNode(viewInfo.get("queries")));
                element.appendChild(subElem);

                rootElem.appendChild(element);
            }

            dom.appendChild(rootElem);

            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            // TODO define dtd
//                tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "connections.dtd");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            // send DOM to file
            String curPath = System.getProperty("user.dir") +"/" + theFilePath;

            tr.transform(new DOMSource(dom),
                    new StreamResult(new FileOutputStream(curPath)));

        } catch (Exception e) {
        	log.debug(e.toString());
            Alert alert = new Alert(Alert.AlertType.ERROR, "writeStatus"+e.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    /**
     * Get child Node's text value in DOM
     * @param defValue default value
     * @param node Parent node to get text
     * @param tag Parent node's tag
     * @return String Child node's text
     */
    private String getNodeTextValue(String defValue, Element node, String tag) {
    	log.debug("Inside getNodeTextValue");
        String value = defValue;
        NodeList nodeList;
        nodeList = node.getElementsByTagName(tag);
        if (nodeList.getLength() > 0 && nodeList.item(0).hasChildNodes()) {
            value = nodeList.item(0).getFirstChild().getNodeValue();
        }
        return value;
    }
}
