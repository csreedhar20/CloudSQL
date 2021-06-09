package utils;

import java.time.LocalDateTime;

public class ConnInfo {
    private String theConnectionName;
    private String theUserName;
    private String thePassword;
    private String theServiceUrl;
    private String theServiceOperation;
    private String theSessionId;
    private LocalDateTime theUpdatedTime;

    /**
     * constructor
     */
    public ConnInfo() {
        theConnectionName = "";
        theUserName = "";
        thePassword = "";
        theServiceUrl = "";
        theServiceOperation = "";
        theUpdatedTime = LocalDateTime.now().minusYears(1);
        theSessionId = "";
    }

    /**
     * constructor
     * @param connName
     * @param userName
     * @param password
     * @param url
     * @param operation
     */
    public ConnInfo(String connName, String userName, String password, String url, String operation) {
        theConnectionName = connName;
        theUserName = userName;
        thePassword = password;
        theServiceUrl = url;
        theServiceOperation = operation;

        theUpdatedTime = LocalDateTime.now().minusYears(1);
        theSessionId = "";
    }

    /**
     * get connection name
     * @return
     */
    public String getConnectionName() {
        return theConnectionName;
    }

    /**
     * get user name
     * @return
     */
    public String getUserName() {
        return theUserName;
    }

    /**
     * get user password
     * @return
     */
    public String getPassword() {
        return thePassword;
    }

    /**
     * get service url
     * @return
     */
    public String getServiceUrl() {
        return theServiceUrl;
    }

    /**
     * get service operation
     * @return
     */
    public String getServiceOperation() {
        return theServiceOperation;
    }

    /**
     * get session id
     * @return
     */
    public String getSessionId()
    {
        return theSessionId;
    }

    /**
     * get updated time
     * @return
     */
    public LocalDateTime getUpdatedTime() {
        return theUpdatedTime;
    }

    /**
     * set connection time
     * @param connName
     */
    public void setConnectionName(String connName) {
        theConnectionName = connName;
    }

    /**
     * set user name
     * @param userName
     */
    public void setUserName(String userName) {
        theUserName = userName;
    }

    /**
     * set user password
     * @param password
     */
    public void setPassword(String password) {
        thePassword = password;
    }

    /**
     * set service url
     * @param url
     */
    public void setServiceUrl(String url) {
        theServiceUrl = url;
    }

    /**
     * set service operation
     * @param operation
     */
    public void setServiceOperation(String operation) {
        theServiceOperation = operation;
    }

    /**
     * set session Id
     * @param sessionId
     */
    public void setSessionId(String sessionId) {
        theSessionId = sessionId;
    }

    /**
     * update time
     */
    public void updateTime() {
        theUpdatedTime = LocalDateTime.now();
    }

    /**
     * set updated time by string
     * @param time
     */
    public void setUpdatedTime(String time) {
        theUpdatedTime = LocalDateTime.parse(time);
    }

    /**
     * set updated time
     * @param time
     */
    public void setUpdatedTime(LocalDateTime time) {
        if(time != null)
            theUpdatedTime = time;
    }
}
