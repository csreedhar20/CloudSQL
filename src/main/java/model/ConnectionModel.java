package model;

import utils.*;

import java.time.LocalDateTime;

public class ConnectionModel {

    /**
     * Constructor
     */
    public ConnectionModel() {
    }

    /**
     * Check if there is the connection in connection list
     * @param connName connection name to check if exist
     * return true if connection is exist
     */
    public boolean exist(String connName) {
        return ConnInfoList.getInstance().exist(connName);
    }

    /**
     * create new connection
     * @param connName connection name to create
     */
    public void newConnection(String connName) {
        ConnInfo info = new ConnInfo();
        info.setConnectionName(connName);
        ConnInfoList.getInstance().addOrUpdateConnInfo(info);
    }

    /**
     * get connection
     * @param connName connection name to get
     */
    public ConnInfo getConnectionInfo(String connName) {
        return ConnInfoList.getInstance().getConnInfo(connName);
    }

    /**
     * Delete connection from connection list
     * @param connName connection name to delete
     */
    public void deleteConnection(String connName) {
        ConnInfoList.getInstance().deleteConnInfo(connName);
        ConnInfoList.getInstance().write();
    }

    /**
     * Add or update current connection info into connection list and save the list to file
     * @param connName connection name to update
     * @param userName user name
     * @param password user password
     * @param url       service url
     * @param operation service operation
     */
    public void saveConnection(String connName, String userName, String password, String url, String operation, String sessionId, LocalDateTime time) {
        ConnInfo info = new ConnInfo(connName, userName, password, url, operation);
        if(sessionId.isEmpty() != true)
            info.setSessionId(sessionId);
        if(time != null)
            info.setUpdatedTime(time);
        ConnInfoList.getInstance().addOrUpdateConnInfo(info);
        ConnInfoList.getInstance().write();
    }

    /**
     * Test connection
     * @param connName connection name to test
     * @return boolean true if successfully connected, false otherwise
     */
    public boolean testConnection(String connName) {
        return false;
    }

    /**
     * update connection info
     * @param url url of the service
     * @param sessionid sessionid
     * @param updatedTime updated time
     */
    public void updateConnectionInfo(String url, String sessionid, LocalDateTime updatedTime) {
        ConnInfoList.getInstance().updateConnectionInfo(url, sessionid, updatedTime);
    }

}