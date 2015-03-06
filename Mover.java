 
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

import java.util.List;

public class Mover extends Application 
{
    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Drawing Operations Test");
        Group root = new Group();
        Canvas canvas = new Canvas(300, 250);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        //drawShapes(gc);
        root.getChildren().add(canvas);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        primaryStage.getScene().setCursor(javafx.scene.Cursor.HAND);
        
        // Fill the Canvas with a Blue rectnagle when the user double-clicks
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, 
                               new EventHandler<MouseEvent>() {
                                   @Override
                                       public void handle(MouseEvent t) {            
                                       if (t.getClickCount() >1) {
                                           drawShape(gc, t, Color.GREEN);
                                           
                                       }  
                                   }
                               });
        
    }

    private static final double WIDTH = 30;
    private static final double ARC   = WIDTH/3;
    
    private static class Point 
    {
        public double x;
        public double y;
        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
    
    private final List<Point> shapes = new java.util.ArrayList<>();
    
    private void drawShape(GraphicsContext gc, MouseEvent t, Color fill) {
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(5);

        double x = t.getX();
        double y = t.getY();
        
        Point old = null;
        double w2 = WIDTH/2;
        
        for (Point p : shapes) {
            if (x < p.x+w2 && x > p.x-w2 && y < p.y+w2 && y > p.y-w2) {
                old = p;
                break;
            }
        }
        
        gc.setFill(fill);

        if (old == null) {
            gc.fillRoundRect(x-WIDTH/2, y-WIDTH/2, WIDTH, WIDTH, ARC, ARC);
            shapes.add(new Point(x, y));
        }
        else {
            gc.setFill(Color.GREEN);
            
            gc.clearRect(old.x-WIDTH/2, old.y-WIDTH/2, WIDTH, WIDTH);
            shapes.remove(old);

            //should we fix overlaps?
            for (Point p : shapes) {
                gc.fillRoundRect(p.x-WIDTH/2, p.y-WIDTH/2, WIDTH, WIDTH, ARC, ARC);
            }
        }
    }
    

}


