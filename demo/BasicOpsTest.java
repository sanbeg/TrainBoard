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
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.control.MenuItem;
import javafx.geometry.Point2D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;

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
        //drawShapes(gc);
        drawArc3(gc);
        gc.strokeText("9\u00BE\u2033", 10, 20);
        
        root.getChildren().add(canvas);

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent e) -> {
               gc.clearRect(e.getX() - ERASE_SIZE/2, e.getY() - ERASE_SIZE/2, ERASE_SIZE, ERASE_SIZE);
       });
 
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {            
                if (t.getClickCount() >1) {
                    reset(canvas, Color.ALICEBLUE);
                    //drawShapes(gc);
                    drawArc3(gc);
                    
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

        //drawArc(gc);
        //drawArc2(gc);
        drawArc3(gc);
        
    }

    void drawArc(GraphicsContext gc) 
        {
            double x = 10;
            double y = 300;
            
            double r = 150; //circle radius
            double ad = 45; //angle in degrees
            double ar = Math.toRadians(ad);
            
            double bow = r * Math.cos(ar/2);
            double offset = r+bow/2;

            double lw = 8;
            //bounding box width & height
            double width = r-bow + lw;
            double height = (r+lw) * Math.sin(ar/2); //was 2 * ...
            
            gc.setLineWidth(lw);
            gc.setLineCap(StrokeLineCap.BUTT);
            
            //gc.fillRect(x-width/2-lw/2, y-height/2, width+lw, height);
            gc.strokeArc(x, y-r/2, r, r, 180-ad/2, ad, ArcType.OPEN);
            gc.setFill(Color.RED);
            gc.fillOval(x, y, 3, 3);
            gc.setFill(Color.RED);
            gc.fillOval(x+r/2, y, 3, 3);
            //gc.fillOval(x+r-bow, y+height/2, 1, 1);
            Point2D p1 = new Rotate(ad/2, x+r/2, y).transform(x,y);
            gc.fillOval(p1.getX(), p1.getY(), 3, 3);
            Point2D p2 = new Rotate(-ad/2, x+r/2, y).transform(x,y);
            gc.fillOval(p2.getX(), p2.getY(), 3, 3);
            System.out.printf("X = %f,%f\n", p1.getX(), p2.getX());
            System.out.printf("Y = %f, %f\n", p1.getY(), p2.getY());
            
	    gc.setLineWidth(1);
	    gc.setStroke(Color.BLACK);
	    gc.strokeRect(x-width/2, y-height/2, width, height);
	    
        }
    

    void drawArc2(GraphicsContext gc) 
        {
            double x = 150;
            double y = 300;
            
            double r = 150; //circle radius
            double ad = 30; //angle in degrees
            double ar = Math.toRadians(ad);
            
            Point2D size = new Rotate(-ad/2, r, 0).transform(0, 0);
            System.out.println("height = " + size.getY());
            

            double bow = r * Math.cos(ar/2);
            double offset = r+bow/2;

            double lw = 8;
            //bounding box width & height
            double width = r-bow + lw;
            double height = (r+lw) * Math.sin(ar/2); //was 2 * ...
            
            gc.setLineWidth(lw);
            gc.setLineCap(StrokeLineCap.BUTT);
            
            Point2D p2 = new Rotate(-ad/2, x+r/2, y).transform(x,y);

            //gc.strokeLine(p2.getX(), p2.getY(), p2.getX(), p2.getY()-50);
            gc.setStroke(Color.BLUE);
            gc.strokeLine(x, p2.getY(), x, p2.getY()-50);
            
            gc.save();
            Affine tr = gc.getTransform();
            tr.appendRotation(ad/2, p2.getX(), p2.getY());
            tr.appendTranslation(x-p2.getX(), 0);
            
            gc.setTransform(tr);
            
            gc.strokeArc(x, y-r/2, r, r, 180-ad/2, ad, ArcType.OPEN);
            gc.setFill(Color.RED);
            gc.fillOval(x, y, 3, 3);
            gc.setFill(Color.RED);
            gc.fillOval(x+r/2, y, 3, 3);
            //gc.fillOval(x+r-bow, y+height/2, 1, 1);
            Point2D p1 = new Rotate(ad/2, x+r/2, y).transform(x,y);
            gc.fillOval(p1.getX(), p1.getY(), 3, 3);
            gc.fillOval(p2.getX(), p2.getY(), 3, 3);
            System.out.printf("X = %f,%f\n", p1.getX(), p2.getX());
            System.out.printf("Y = %f, %f\n", p1.getY(), p2.getY());
            gc.restore();
	    gc.setLineWidth(1);
            gc.setStroke(Color.RED);
            //gc.strokeRect(x-5, y-5, 10, 10);
            gc.strokeRect(x-lw/2, p2.getY()-50, lw+width, 50);
            
            /*            
	    gc.setStroke(Color.BLACK);
	    gc.strokeRect(x-width/2, y-height/2, width, height);
	    */
        }
    
    void drawArc3(GraphicsContext gc) 
    {
        double x = 100;
        double y = 100;
        
        double r = 150; //circle radius
        double ad = 30; //angle in degrees
        double ar = Math.toRadians(ad);
        
        double lw = 8;
        double h = 100;
        
        gc.setLineWidth(lw);
        gc.setLineCap(StrokeLineCap.BUTT);
        gc.setStroke(Color.BLUE);
        double coff = r*Math.sin(ar)/2;
        double yoff = Math.max(coff, h/2);
        
        gc.strokeArc(x, y-r + yoff, r*2, r*2, 180, -ad, ArcType.OPEN);  //RH
        gc.strokeArc(x-2*r, y-r + yoff, r*2, r*2, 0, ad, ArcType.OPEN); //LH
        gc.strokeLine(x, y + yoff, x, y + yoff - h);
        
        gc.setFill(Color.RED);
        gc.fillOval(x-1, y-1, 3, 3);
        gc.fillOval(x-1, y+yoff-1, 3, 3);
        gc.fillOval(x+(r-r*Math.cos(ar))-1, y+yoff-r*Math.sin(ar)-1, 3, 3);
        gc.fillOval(x-1, y+yoff-h-1, 3, 3);

        double width = (r - (r-lw/2)*Math.cos(ar)) * 2;
        double height = h;
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(x-width/2, y-height/2, width, height);
        
    }
    
                
}
