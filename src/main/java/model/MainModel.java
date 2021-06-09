package model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import utils.AppStatus;
import utils.ConnInfoList;

public class MainModel {
	final static Logger log = LogManager.getLogger(MainModel.class.getClass());

    /**
     * constructor
     */
    public MainModel() {

    }

    /**
     * get query view names
     * @return
     */
    public String [] getQueryViewNames() {
        return AppStatus.getInstance().getViewNames();
    }

    /**
     * save app status
     */
    public void saveAppStatus() {
        AppStatus.getInstance().writeStatus();
    }
}
