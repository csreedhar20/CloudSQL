package controller;

import javafx.beans.value.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.ConnectionModel;
import utils.*;

import java.time.LocalDateTime;

public class ConnectionController {
    @FXML
    private ListView<String> theConnectionList;
    @FXML
    private TextField theConnectionName;
    @FXML
    private TextField theUserName;
    @FXML
    private PasswordField theUserPassword;
    @FXML
    private TextField theServiceUrl;
    @FXML
    private TextField theServiceOperation;

    private ConnectionModel theModel;

    /**
     * Constructor
     */
    public ConnectionController() {

    }

    /**
     * initialization
     */
    public void initialization() {
        String[] connNames = ConnInfoList.getInstance().getConnNames();
        theConnectionList.getItems().addAll(connNames);
        theConnectionList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(!theModel.exist(newValue))
                    return;
                ConnInfo connInfo = theModel.getConnectionInfo(newValue);
                theConnectionName.setText(newValue);
                theUserName.setText(connInfo.getUserName());
                theUserPassword.setText(connInfo.getPassword());
                theServiceUrl.setText(connInfo.getServiceUrl());
                theServiceOperation.setText(connInfo.getServiceOperation());
            }
        });
        theConnectionList.getSelectionModel().selectFirst();
    }

    /**
     * create new connection
     * @param actionEvent
     */
    public void onNewConnection(ActionEvent actionEvent) {
        theConnectionName.clear();
        theUserName.clear();
        theUserPassword.clear();
        theServiceUrl.clear();
        theServiceOperation.clear();
    }

    /**
     * delete connection
     * @param actionEvent
     */
    public void onDeleteConnection(ActionEvent actionEvent) {
        MultipleSelectionModel<String> selectionModel = theConnectionList.getSelectionModel();
        if(selectionModel == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "connection List is empty.", ButtonType.YES);
            alert.showAndWait();
            return;
        }

        String connectionName = selectionModel.getSelectedItem();
        theModel.deleteConnection(connectionName);
        theConnectionList.getItems().remove(connectionName);
    }

    /**
     * save connection
     * @param actionEvent
     */
    public void onSaveConnection(ActionEvent actionEvent) {
        saveConnection("", null);
    }

    /**
     * internal save connection
     * @param sessionId session id
     * @param time connection time
     */
    private void saveConnection(String sessionId, LocalDateTime time) {
        if(!theModel.exist(theConnectionName.getText())) {
            // add connection name to list
            theConnectionList.getItems().add(theConnectionName.getText());
        }
        theModel.saveConnection(
                theConnectionName.getText(),
                theUserName.getText(),
                theUserPassword.getText(),
                theServiceUrl.getText(),
                theServiceOperation.getText(),
                sessionId,
                time
        );
        theConnectionList.getSelectionModel().select(theConnectionName.getText());
    }

    /**
     * test connection
     * @param actionEvent
     */
    public void onTestConnection(ActionEvent actionEvent) {
        String connName = theConnectionName.getText();
        String sessionId = testConnection();
        if(sessionId.isEmpty() == true) {
            return;
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Test connection is success.", ButtonType.YES);
            alert.showAndWait();
        }
        // update all services
        theModel.updateConnectionInfo(theServiceUrl.getText(), sessionId, LocalDateTime.now());
        saveConnection(sessionId, LocalDateTime.now());
    }

    /**
     * internal test connection
     * @return
     */
    private String testConnection() {
        return SoapAPI.callLoginService(theServiceUrl.getText(), /*theServiceOperation.getText()*/"urn:TestWsdl/loginMessage", theUserName.getText(), theUserPassword.getText());
    }

    /**
     * set model to controller
     * @param theModel
     */
    public void setModel(ConnectionModel theModel) {
        this.theModel = theModel;
    }
}
