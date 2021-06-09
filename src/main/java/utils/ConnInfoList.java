package utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

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
import java.time.LocalDateTime;
import java.util.ArrayList;

public class ConnInfoList extends ArrayList<ConnInfo> {
	final static Logger log = LogManager.getLogger(ConnInfoList.class.getClass());

    private final String theSettingPath = "connectionSetting.xml";
    private static ConnInfoList theSetting = new ConnInfoList();

    /**
     * internal constructor
     */
    private ConnInfoList() {
        read();
    }
    /**
     * Get instance
     */
    public static ConnInfoList getInstance() {
        return theSetting;
    }

    /**
     * Get connection info using connection name
     * @param connName connection name to get info
     * @return connection info
     */
    public ConnInfo getConnInfo(String connName) {
        for(ConnInfo info : this) {
            if(info.getConnectionName().equals(connName))
                return info;
        }
        return null;
    }

    /**
     * check if connection is valid
     * @param connName
     * @return
     */
    public boolean isValid(String connName) {
        ConnInfo info = getConnInfo(connName);
        LocalDateTime updatedTime = info.getUpdatedTime();
        if(updatedTime != null && updatedTime.isAfter(LocalDateTime.now().minusDays(1)))
            return true;
        return false;
    }

    /**
     * Get connection names
     * @return String array
     */
    public String [] getConnNames() {
        ArrayList<String> connNames = new ArrayList<String>();
        for(ConnInfo info : this) {
            connNames.add(info.getConnectionName());
        }
        return connNames.toArray(new String[0]);
    }

    /**
     * Check if there is connection in connection list
     * @param connName connection name to check
     * @return boolean true if connection is in connection list, false otherwise
     */
    public boolean exist(String connName) {
        for(ConnInfo info : this) {
            if(info.getConnectionName().equals(connName))
                return true;
        }
        return false;
    }

    /**
     * update connection info
     * @param url url of the service
     * @param sessionid sessionid
     * @param updatedTime updated time
     */
    public void updateConnectionInfo(String url, String sessionid, LocalDateTime updatedTime) {
        for(ConnInfo info : this) {
            if(info.getServiceUrl().equals(url)) {
                info.setSessionId(sessionid);
                info.setUpdatedTime(updatedTime);
                addOrUpdateConnInfo(info);
            }
        }
    }

    /**
     * Delete connection from connection list
     * @param connName
     */
    public void deleteConnInfo(String connName) {
        for(ConnInfo info : this) {
            if(info.getConnectionName().equals(connName))
            {
                remove(info);
                break;
            }
        }
    }

    /**
     * Add or update connection info
     * @param connInfo Connection info
     */
    public void addOrUpdateConnInfo(ConnInfo connInfo) {
        ConnInfo oldInfo = getConnInfo(connInfo.getConnectionName());
        if(oldInfo == null) {
            add(connInfo);
        }
        else {
            remove(oldInfo);
            add(connInfo);
        }
    }

    /**
     * Read connection info from connection setting file
     * @return boolean true if read setting success, false otherwise
     */
    private boolean read() {
        this.clear();
        try {
        	File f = new File(theSettingPath);
        	if (!f.isFile()) {
            	System.out.println("Inside filecreate");
        		f.createNewFile();
        	}
        }catch(Exception e) {
        	System.out.println(e.toString());
        	
        }
        Document dom;
        // Make an  instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // read xml file
            String curPath = System.getProperty("user.dir") +"/" +theSettingPath;
            File file = new File(curPath);

            // use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("connection");
            for(int i=0; i<nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                String connectionName = getNodeTextValue("", (Element) node, "connectionName");
                if(connectionName.isEmpty())
                    continue;
                ConnInfo connInfo = new ConnInfo();
                connInfo.setConnectionName(connectionName);
                String value = getNodeTextValue("", (Element) node, "name");
                connInfo.setUserName(value);
                value = getNodeTextValue("", (Element) node, "password");
                connInfo.setPassword(value);
                value = getNodeTextValue("", (Element) node, "url");
                connInfo.setServiceUrl(value);
                value = getNodeTextValue("", (Element) node, "operation");
                connInfo.setServiceOperation(value);
                value = getNodeTextValue("", (Element) node, "sessionId");
                connInfo.setSessionId(value);
                value = getNodeTextValue("", (Element) node, "updatedTime");
                connInfo.setUpdatedTime(value);

                add(connInfo);
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
     * Save connection setting into file
     */
    public void write() {
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
            Element rootElem = dom.createElement("connections");

            for(ConnInfo info : this) {
                element = dom.createElement("connection");
                // connection name
                Element subElem = dom.createElement("connectionName");
                subElem.appendChild(dom.createTextNode(info.getConnectionName()));
                element.appendChild(subElem);
                // user name
                subElem = dom.createElement("name");
                subElem.appendChild(dom.createTextNode(info.getUserName()));
                element.appendChild(subElem);
                // user password
                subElem = dom.createElement("password");
                subElem.appendChild(dom.createTextNode(info.getPassword()));
                element.appendChild(subElem);
                // url
                subElem = dom.createElement("url");
                subElem.appendChild(dom.createTextNode(info.getServiceUrl()));
                element.appendChild(subElem);
                // operation
                subElem = dom.createElement("operation");
                subElem.appendChild(dom.createTextNode(info.getServiceOperation()));
                element.appendChild(subElem);

                // sessionId
                subElem = dom.createElement("sessionId");
                subElem.appendChild(dom.createTextNode(info.getSessionId()));
                element.appendChild(subElem);

                // updated time
                subElem = dom.createElement("updatedTime");
                subElem.appendChild(dom.createTextNode(info.getUpdatedTime().toString()));
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
            String curPath = System.getProperty("user.dir") + theSettingPath;

            tr.transform(new DOMSource(dom),
                    new StreamResult(new FileOutputStream(curPath)));

        } catch (Exception e) {
        	log.debug(e.toString());
            Alert alert = new Alert(Alert.AlertType.ERROR, "write"+e.getMessage(), ButtonType.OK);
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
        String value = defValue;
        NodeList nodeList;
        nodeList = node.getElementsByTagName(tag);
        if (nodeList.getLength() > 0 && nodeList.item(0).hasChildNodes()) {
            value = nodeList.item(0).getFirstChild().getNodeValue();
        }
        return value;
    }

}