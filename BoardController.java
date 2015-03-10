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

    private final BoardModel model = new BoardModel();
    

    public BoardController(Stage stage, File file) {
        this.stage = stage;
        this.file = file;
    }

    private File file = null;
    private static final String TITLE_PREFIX = "Drawing Operations Test";

    private void updateFile(File file, FileChooser chooser) {
        this.file = file;
        saveItem.setDisable(file==null);
        stage.setTitle(String.format("%s - %s", TITLE_PREFIX, file.getName()));
        
        File parent = file.getParentFile();
        if (parent != null) {
            chooser.setInitialDirectory(parent);
        }
    }
    
    public void initialize() {
        stage.setTitle(TITLE_PREFIX);

        final Canvas canvas = new Canvas(800, 400);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        canvasPane.getChildren().add(canvas);

        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {            
                if (t.getClickCount() >1 && t.isStillSincePress()) {
		    BoardModel.Point point = model.findPointAt(t.getX(), t.getY());
		    if (point == null) {
			//clicked empty spot, add shape
			model.drawShape(gc, t.getX(), t.getY());
		    } else {
			//clicked occupied spot, remove a shape
			model.eraseShape(gc, point);
		    }
		    
		     
                }  
            });

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, (MouseEvent t) -> {
                if (t.getClickCount() == 1) {
                    model.liftShape(gc, t.getX(), t.getY(), Color.BLUE);
                }
            });
        
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, (MouseEvent t) -> {
                model.releaseShape(gc);
            });
        
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent e) -> {
                model.moveShape(gc, e.getX(), e.getY(), Color.BLUE);
            });

       closeItem.setOnAction((javafx.event.ActionEvent e) -> {stage.close();});

       final FileChooser fileChooser = new FileChooser();
       fileChooser.getExtensionFilters().addAll(
                                                new FileChooser.ExtensionFilter("XML Files", "*.xml"),
                                                new FileChooser.ExtensionFilter("All Files", "*.*")
                                                );
       
       if (file != null) {
           updateFile(file, fileChooser);
           SavedBoard sb = JAXB.unmarshal(file, SavedBoard.class);
           if (sb.tracks != null) {
               model.addAllPlaces(sb.tracks);
               model.redraw(gc);
           }
       }
       

       newItem.setOnAction((ActionEvent e) -> {
               resetBoard(gc, canvas);
               file = null;
               saveItem.setDisable(true);
               stage.setTitle(TITLE_PREFIX);
           });
       
       openItem.setOnAction((ActionEvent e) -> {
               fileChooser.setTitle("Open Layout");
               File file = fileChooser.showOpenDialog(stage);
               if (file != null) {
                   updateFile(file, fileChooser);
                   resetBoard(gc, canvas);
                   SavedBoard sb = JAXB.unmarshal(file, SavedBoard.class);
                   if (sb.tracks != null) {
                       model.addAllPlaces(sb.tracks);
                       model.redraw(gc);
                   }
               }
           });

       saveItem.setOnAction((ActionEvent ev) -> {
               if (file != null) {
		   SavedBoard savedBoard = new SavedBoard();
		   savedBoard.setAll(model.shapes);
                   JAXB.marshal(savedBoard, file);
               }
           });
               

       saveAsItem.setOnAction((ActionEvent ev) -> {
               fileChooser.setTitle("Save Layout");
               File file = fileChooser.showSaveDialog(stage);
               if (file != null) {
                   updateFile(file, fileChooser);
		   SavedBoard savedBoard = new SavedBoard();
		   savedBoard.setAll(model.shapes);
                   JAXB.marshal(savedBoard, file);
               }
           });
       

    }

    public static class SavedPlace
    {
	public double x;
	public double y;
    }
    
    private static class SavedBoard 
    {
        public List<SavedPlace> tracks = new java.util.ArrayList<>();
	public void setAll(List<BoardModel.Point> points) 
	{
	    for (BoardModel.Point place: points) {
		SavedPlace sp = new SavedPlace();
		sp.x = place.x;
		sp.y = place.y;
		tracks.add(sp);
	    }
        }
    }

    private void resetBoard(GraphicsContext gc, Canvas canvas) 
    {
        model.shapes.clear();
        
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
    


}
