package controller;

import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.*;
import model.*;

import java.util.*;

public class MainController {
    private MainModel theModel;
    @FXML
    private TabPane theTabPane;
    // query controller map
    private Map<String, QueryController> theQueryControllers;

    /**
     * constructor
     */
    public MainController() {

    }

    /**
     * initialization
     */
    public void initialization() {
        if(theQueryControllers == null)
            theQueryControllers = new HashMap<>();
        String [] viewNames = theModel.getQueryViewNames();
        for(String viewName : viewNames) {
            try {
                QueryModel model = new QueryModel();
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "../fxml/queryview.fxml"));
                Parent root = (Parent) loader.load();
                QueryController controller = loader.getController();
                controller.setModel(model);
                controller.initialization(viewName);

                Tab newTab = new Tab(viewName, root);
                newTab.setOnCloseRequest(e -> {
                    controller.removeStatus(viewName);
                    theQueryControllers.remove(viewName);
                });
                theTabPane.getTabs().add(newTab);
                theQueryControllers.put(newTab.getText(), controller);

                SingleSelectionModel<Tab> selectionModel = theTabPane.getSelectionModel();
                selectionModel.clearAndSelect(theTabPane.getTabs().size() - 1);
            }
            catch(Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "initialization"+e.getMessage(), ButtonType.OK);
                alert.showAndWait();
            }
        }
    }

    /**
     * open connection window
     * @param actionEvent
     */
    @FXML
    private void onConnections(ActionEvent actionEvent) {
        try {
            ConnectionModel model = new ConnectionModel();

//            FXMLLoader loader = new FXMLLoader(getClass().getResource(
//                    "../fxml/connview.fxml"));
          FXMLLoader loader = new FXMLLoader(getClass().getResource(
          "/fxml/connview.fxml"));            
            Parent root = (Parent) loader.load();
            ConnectionController controller = loader.getController();
            controller.setModel(model);
            controller.initialization();

            Stage theStage = new Stage();
            theStage.setTitle("Connection");
            theStage.setScene(new Scene(root, 800, 400));
            theStage.initModality(Modality.APPLICATION_MODAL);
            theStage.showAndWait();

            // update connection list
//            List<Tab> tabList = theTabPane.getTabs();
//            for(Tab tab : tabList) {
//                QueryController querycontroller = theQueryControllers.get(tab.getText());
//                querycontroller.updateConnList();
//            }
        }
        catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "onConnections"+e.toString(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    /**
     * open new query window
     * @param actionEvent
     */
    @FXML
    private void onNewWindow(ActionEvent actionEvent) {
        if(theQueryControllers == null)
            theQueryControllers = new HashMap<>();

        try {
            QueryModel model = new QueryModel();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/fxml/queryview.fxml"));
            Parent root = (Parent) loader.load();
            QueryController controller = loader.getController();
            controller.setModel(model);

            int tabIdx = theTabPane.getTabs().size();
            String tabName;
            if(tabIdx > 0) {
                Tab lastTab = theTabPane.getTabs().get(tabIdx - 1);
                tabName = lastTab.getText();
                String[] splits = tabName.split("_");
                tabIdx = Integer.parseInt(splits[1]);
            }
            Tab newTab = new Tab("View_" + (tabIdx + 1), root);
            newTab.setOnCloseRequest(e -> {
                QueryController ctrl = theQueryControllers.get(newTab.getText());
                ctrl.removeStatus(newTab.getText());
                theQueryControllers.remove(newTab.getText());
            });
            controller.initialization(newTab.getText());
            theTabPane.getTabs().add(newTab);
            theQueryControllers.put(newTab.getText(), controller);

            SingleSelectionModel<Tab> selectionModel = theTabPane.getSelectionModel();
            selectionModel.clearAndSelect(theTabPane.getTabs().size() - 1);
        }
        catch(Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "onNewWindow"+e.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    /**
     * save application status
     * @param actionEvent
     */
    @FXML
    private void onSave(ActionEvent actionEvent) {
        List<Tab> tabList = theTabPane.getTabs();
        for(Tab tab : tabList) {
            QueryController controller = theQueryControllers.get(tab.getText());
            controller.saveStatus(tab.getText());
        }

        theModel.saveAppStatus();
    }

    /**
     * set model to controller
     * @param theModel
     */
    public void setModel(MainModel theModel) {
        this.theModel = theModel;
    }

    /**
     * execute queries in all window
     * @param actionEvent
     */
    public void onExecuteAll(ActionEvent actionEvent) {
        List<Tab> tabList = theTabPane.getTabs();
        for(Tab tab : tabList) {
            QueryController controller = theQueryControllers.get(tab.getText());
            controller.execute();
        }
    }
}
