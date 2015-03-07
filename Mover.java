 
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Cursor;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
 
import javafx.fxml.FXMLLoader;
import java.io.IOException;

public class Mover extends Application 
{
    public static void main(String[] args) {
        launch(args);
    }

    private static final boolean USE_FXML = true;
    
    @Override
    public void start(Stage primaryStage) throws IOException {
        final Parent root;
        
        if (USE_FXML) {
            FXMLLoader fxml = new FXMLLoader(getClass().getResource("boardview.fxml"));
            fxml.setController(new BoardController(primaryStage));
            root = fxml.load();
        }
        else {
            Group group = new Group();

            BoardController controller = new BoardController(primaryStage);
            controller.canvasPane = new Pane();
            controller.initialize();

            group.getChildren().add(controller.canvasPane);
            root = group;
        }
 

        primaryStage.setTitle("Drawing Operations Test");
        
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        primaryStage.getScene().setCursor(Cursor.HAND);
    }
    
}


