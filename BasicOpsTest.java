//package canvastest;
 
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.control.MenuItem;

import javafx.stage.Stage;
 
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

import javafx.fxml.Initializable;
import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class BasicOpsTest extends Application {
 
    public static void main(String[] args) {
        launch(args);
    }
 
    private void reset(Canvas canvas, Color color) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(color);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
    
    private static final double ERASE_SIZE = 8;

    private static final boolean USE_FXML = true;
    
    //from FXML
    public Pane canvasPane; 
    public MenuItem closeItem;

    private Stage stage;
    
    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Drawing Operations Test");

        final Parent root;
        if (USE_FXML) {
            FXMLLoader fxml = new FXMLLoader(getClass().getResource("jtest.fxml"));
            fxml.setController(this);
            root = fxml.load();
        }
        else {
            Group group = new Group();
            canvasPane = new Pane();
            group.getChildren().add(canvasPane);
            root = group;
            initialize();
        }
        

        primaryStage.setScene(new Scene(root));
        primaryStage.getScene().setCursor(javafx.scene.Cursor.HAND);
        primaryStage.show();
        stage = primaryStage;
    }
    
    public void initialize() {
        Pane root = canvasPane;

        Canvas canvas = new Canvas(800, 400);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawShapes(gc);
        root.getChildren().add(canvas);

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent e) -> {
               gc.clearRect(e.getX() - ERASE_SIZE/2, e.getY() - ERASE_SIZE/2, ERASE_SIZE, ERASE_SIZE);
       });
 
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {            
                if (t.getClickCount() >1) {
                    reset(canvas, Color.ALICEBLUE);
                    drawShapes(gc);
                }  
            });

       closeItem.setOnAction((javafx.event.ActionEvent e) -> {stage.close();});
    }

    private void drawShapes(GraphicsContext gc) {
        gc.setFill(Color.GREEN);
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(5);

        gc.strokeLine(40, 10, 10, 40);

        gc.fillOval(10, 60, 30, 30);
        gc.strokeOval(60, 60, 30, 30);
        gc.fillRoundRect(110, 60, 30, 30, 10, 10);
        gc.strokeRoundRect(160, 60, 30, 30, 10, 10);

        gc.fillArc(10, 110, 30, 30, 45, 240, ArcType.OPEN);
        gc.fillArc(60, 110, 30, 30, 45, 240, ArcType.CHORD);
        gc.fillArc(110, 110, 30, 30, 45, 240, ArcType.ROUND);

        gc.strokeArc(10, 160, 30, 30, 45, 240, ArcType.OPEN);
        gc.strokeArc(60, 160, 30, 30, 45, 240, ArcType.CHORD);
        gc.strokeArc(110, 160, 30, 30, 45, 240, ArcType.ROUND);

        gc.fillPolygon(new double[]{10, 40, 10, 40},
                       new double[]{210, 210, 240, 240}, 4);
        gc.strokePolygon(new double[]{60, 90, 60, 90},
                         new double[]{210, 210, 240, 240}, 4);
        gc.strokePolyline(new double[]{110, 140, 110, 140},
                          new double[]{210, 210, 240, 240}, 4);
    }

}
