 
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.Cursor;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
 
public class Mover extends Application 
{
    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Drawing Operations Test");
        Group root = new Group();
        BoardController controller = new BoardController(primaryStage);
        Pane pane = new Pane();
        
        root.getChildren().add(pane);
        controller.canvasPane = pane;
        controller.initialize();

        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        primaryStage.getScene().setCursor(Cursor.HAND);
    }
    
}


