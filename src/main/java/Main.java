import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.MainModel;
import utils.SoapAPI;

public class Main extends Application {
	final static Logger log = LogManager.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
    	
    	try {

        MainModel model = new MainModel();
        

        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "fxml/main.fxml"));
        Parent root = (Parent) loader.load();
        MainController controller = loader.getController();
        controller.setModel(model);
        controller.initialization();
        primaryStage.setTitle("Cloud SQL");
        primaryStage.setScene(new Scene(root, 1280, 800));
        primaryStage.show();
    	}
    	catch(Exception e) {
    		log.debug("Error message inside main:"+e.toString());
    	}
    }

    public static void main(String[] args) {
    	log.debug("Inside Main");
        launch(args);
    }
}