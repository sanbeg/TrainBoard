import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.event.ActionEvent;

import java.util.List;
import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAttribute;

public class BoardController {
    
    private final Stage stage;
    
    public Pane canvasPane;

    public MenuItem newItem;
    public MenuItem openItem;
    public MenuItem saveItem;
    public MenuItem saveAsItem;
    public MenuItem closeItem;

    public BoardController(Stage stage) {
        this.stage = stage;
    }
    
    public void initialize() {
        final Canvas canvas = new Canvas(800, 400);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        canvasPane.getChildren().add(canvas);

        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {            
                if (t.getClickCount() >1 && t.isStillSincePress()) {
                    drawShape(gc, t);
                }  
            });

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, (MouseEvent t) -> {
                if (t.getClickCount() == 1) {
                    liftShape(gc, t, Color.BLUE);
                    press.x = t.getX();
                    press.y = t.getY();
                }
            });
        
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, (MouseEvent t) -> {
                releaseShape(gc);
            });
        
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent e) -> {
                moveShape(gc, e, Color.BLUE);
            });

       closeItem.setOnAction((javafx.event.ActionEvent e) -> {stage.close();});

       final FileChooser fileChooser = new FileChooser();
       fileChooser.getExtensionFilters().addAll(
                                                new FileChooser.ExtensionFilter("XML Files", "*.xml"),
                                                new FileChooser.ExtensionFilter("All Files", "*.*")
                                                );
       
       newItem.setOnAction((ActionEvent e) -> {
               resetBoard(gc, canvas);
           });
       
       openItem.setOnAction((ActionEvent e) -> {
               fileChooser.setTitle("Open Layout");
               File file = fileChooser.showOpenDialog(stage);
               if (file != null) {
                   resetBoard(gc, canvas);
                   System.out.println(file.getName());
                   SavedBoard sb = JAXB.unmarshal(file, SavedBoard.class);
                   shapes.addAll(sb.tracks);
                   redraw(gc);
               }
           });

       saveAsItem.setOnAction((ActionEvent ev) -> {
               fileChooser.setTitle("Save Layout");
               File file = fileChooser.showSaveDialog(stage);
               if (file != null) {
                   System.out.println(file.getName());
                   JAXB.marshal(savedBoard, file);
                   
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
        public Point() 
        {
            this.x = this.y = -1;
        }
        
        public boolean covers(double x, double y) {
            double w2 = WIDTH/2;
            return (x < this.x+w2 && x > this.x-w2 && y < this.y+w2 && y > this.y-w2);
        }
        public boolean overlaps(Point other) {
            return Math.abs(x - other.x) < WIDTH && Math.abs(y - other.y) < WIDTH;
        }
                
    }
    
    private final List<Point> shapes = new java.util.ArrayList<>();
    private Point press = new Point(-1, -1);

    private static class SavedBoard 
    {
        public List<Point> tracks;
        public SavedBoard(List<Point> points) 
        {
            tracks = points;
        }
        public SavedBoard() 
        {
        }
        
    }

    private final SavedBoard savedBoard = new SavedBoard(shapes);
    
    private void resetBoard(GraphicsContext gc, Canvas canvas) 
    {
        press.x = press.y = -1;
        shapes.clear();
        
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
    

    private void redraw(GraphicsContext gc) 
    {
        gc.setFill(Color.GREEN);
        
        for (Point p : shapes) {
            gc.fillRoundRect(p.x-WIDTH/2, p.y-WIDTH/2, WIDTH, WIDTH, ARC, ARC);
        }

    }
    
                
    private void drawShape(GraphicsContext gc, MouseEvent t) {
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(5);

        double x = t.getX();
        double y = t.getY();
        
        Point old = findPointAt(x, y);

        gc.setFill(Color.GREEN);

        if (old == null) {
            //clicked empty spot, add shape
            Point p = new Point(x,y);
            snapShape(gc, p);
            gc.fillRoundRect(p.x-WIDTH/2, p.y-WIDTH/2, WIDTH, WIDTH, ARC, ARC);
            shapes.add(p);
        }
        else {
            //clicked occupied spot, remove a shape
            gc.clearRect(old.x-WIDTH/2, old.y-WIDTH/2, WIDTH, WIDTH);
            shapes.remove(old);

            for (Point p : shapes) {
                gc.fillRoundRect(p.x-WIDTH/2, p.y-WIDTH/2, WIDTH, WIDTH, ARC, ARC);
            }
        }
    }

    private Point heldPoint = null;
    
    private Point findPointAt(double x, double y) {
        Point old = null;
        double w2 = WIDTH/2;
        
        for (Point p : shapes) {
            if (p.covers(x, y)) {
                old = p;
                break;
            }
        }
        return old;
    }
   

    private void liftShape(GraphicsContext gc, MouseEvent t, Color color) 
    {
        Point old = findPointAt(t.getX(), t.getY());
        if (old != null) {
            gc.setFill(color);
            gc.fillRoundRect(old.x-WIDTH/2, old.y-WIDTH/2, WIDTH, WIDTH, ARC, ARC);
            heldPoint = old;
        }
    }
    
    private void snapShape(GraphicsContext gc, Point old) {
        /*
         * If moved to overlap, push out of the way.
         * should handle multiple overlap, avoid putting 2 in same place.
         */

        gc.setFill(Color.GREEN);
        Point ov = null;
        boolean clear=false;
        
        for (Point p : shapes) {
            if (p != old && p.overlaps(old)) {
                ov = p;
                //TODO - keep best overlap (by min dist moved, etc)
                //break;
                if (!clear) {
                    gc.clearRect(old.x-WIDTH/2, old.y-WIDTH/2, WIDTH, WIDTH);
                    clear = true;
                }
                
                gc.fillRoundRect(ov.x-WIDTH/2, ov.y-WIDTH/2, WIDTH, WIDTH, ARC, ARC);
            }
        }
        if (ov != null) {
            double xd = Math.abs(old.x - ov.x);
            double yd = Math.abs(old.y - ov.y);

            if (xd < yd) {
                old.x = ov.x;
                old.y = (old.y > ov.y) ? ov.y+WIDTH : ov.y-WIDTH;
            }
            else {
                old.y = ov.y;
                old.x = (old.x > ov.x) ? ov.x+WIDTH : ov.x-WIDTH;
            }
        }

    }
    

    private void releaseShape(GraphicsContext gc) 
    {
        if (heldPoint != null) {
            Point old = heldPoint;
            snapShape(gc, old);

            gc.setFill(Color.GREEN);
            gc.fillRoundRect(old.x-WIDTH/2, old.y-WIDTH/2, WIDTH, WIDTH, ARC, ARC);

            heldPoint = null;
        }
    }

    private void moveShape(GraphicsContext gc, MouseEvent t, Color color) {
        if (heldPoint != null) {
            Point old = heldPoint;
            gc.clearRect(old.x-WIDTH/2, old.y-WIDTH/2, WIDTH, WIDTH);
            gc.setFill(Color.GREEN);
            
            for (Point p : shapes) {
                if (p == old) continue;
                if (p.overlaps(old)) {
                    gc.setFill(Color.RED);
                }
                else {
                    gc.setFill(Color.GREEN);
                }

                gc.fillRoundRect(p.x-WIDTH/2, p.y-WIDTH/2, WIDTH, WIDTH, ARC, ARC);

            }
            gc.setFill(color);
            old.x = t.getX();
            old.y = t.getY();
            gc.fillRoundRect(old.x-WIDTH/2, old.y-WIDTH/2, WIDTH, WIDTH, ARC, ARC);
        }
        
    }
    


}
