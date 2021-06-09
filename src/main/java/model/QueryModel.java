package model;

import utils.*;

import java.time.LocalDateTime;

public class QueryModel {
    public QueryModel() {
    }
    /**
     * get connection
     * @param connName connection name to get
     */
    public ConnInfo getConnectionInfo(String connName) {
        return ConnInfoList.getInstance().getConnInfo(connName);
    }

    /**
     * get connection names
     */
    public String [] getConnectionNames() {
        return ConnInfoList.getInstance().getConnNames();
    }

    /**
     * execute query via SOAP API and return result
     * @param query Query string to be executed
     * @return String result string
     */
    public String execute(String query) {
        return "";
    }

    /**
     * export data to file
     * @param outPath file path
     * @param data data to be exported
     * @return boolean true if success, false otherwise
     */
    public boolean export(String outPath, String data) {
        return false;
    }

    public void saveStatus(String viewName, String connName, String queries) {
        AppStatus.getInstance().addOrUpdateStatus(viewName, connName, queries);
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
     * remove status by name
     * @param viewName
     */
    public void removeStatus(String viewName) {
        AppStatus.getInstance().removeStatus(viewName);
    }

    /**
     * get selected connection name in query view
     * @param viewName
     * @return
     */
    public String getSelectedConnName(String viewName) {
        return AppStatus.getInstance().getConnectionName(viewName);
    }

    /**
     * get query in query view
     * @param viewName
     * @return
     */
    public String getQueries(String viewName) {
        return AppStatus.getInstance().getQueries(viewName);
    }
}
