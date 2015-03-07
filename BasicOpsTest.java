//package canvastest;
 
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.stage.Stage;
 
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

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

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Drawing Operations Test");
        Group root = new Group();
        Canvas canvas = new Canvas(800, 400);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawShapes(gc);
        root.getChildren().add(canvas);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        primaryStage.getScene().setCursor(javafx.scene.Cursor.HAND);
        
        //intertact demo - clear on mouse
    // Clear away portions as the user drags the mouse
       canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, 
       new EventHandler<MouseEvent>() {
           @Override
           public void handle(MouseEvent e) {
               gc.clearRect(e.getX() - ERASE_SIZE/2, e.getY() - ERASE_SIZE/2, ERASE_SIZE, ERASE_SIZE);
           }
       });
 
    // Fill the Canvas with a Blue rectnagle when the user double-clicks
       canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, 
        new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {            
                if (t.getClickCount() >1) {
                    reset(canvas, Color.ALICEBLUE);
                    drawShapes(gc);
                }  
            }
        });

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
