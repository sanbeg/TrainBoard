import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
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
    public ToolBar trackBar;
    
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
    
    //click points
    private double cx = -1;
    private double cy = -1;

    private BoardModel.Point cmPoint = null;

    private ContextMenu makeContextMenu(final GraphicsContext gc) 
    {
        final ContextMenu contextMenu = new ContextMenu();
        final MenuItem cmDeleteItem = new MenuItem("Delete");
        final MenuItem cmRotateItem = new MenuItem("Rotate");
        
        contextMenu.getItems().add(cmRotateItem);
        contextMenu.getItems().add(cmDeleteItem);
        contextMenu.setAutoHide(true);
	
        cmDeleteItem.setOnAction((javafx.event.ActionEvent e) -> {
                if (cmPoint != null) model.eraseShape(gc, cmPoint);
            });
        cmRotateItem.setOnAction((javafx.event.ActionEvent e) -> {
                if (cmPoint != null) model.rotateShape(gc, cmPoint, 45.0);
            });
        
	return contextMenu;
    }
    
    public void initialize() {
        stage.setTitle(TITLE_PREFIX);

        final Canvas canvas = new Canvas(800, 400);
        //final Canvas floatingCanvas = new Canvas(800, 400);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        //GraphicsContext fgc = floatingCanvas.getGraphicsContext2D();

        canvasPane.getChildren().add(canvas);
        //canvasPane.getChildren().add(floatingCanvas);

        final ContextMenu contextMenu = makeContextMenu(gc);
	final ToggleGroup trackGroup = new ToggleGroup();
        

        canvasPane.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {            
                if (t.getClickCount() > 1 && t.isStillSincePress()) {
		    BoardModel.Point point = model.findPointAt(t.getX(), t.getY());
		    if (point == null) {
			//clicked empty spot, add shape
                        Toggle button = trackGroup.getSelectedToggle();
                        if (button != null) {
                            Shape shape = (Shape)button.getUserData();
                            model.addShape(gc, t.getX(), t.getY(), shape);
                        }
                        
		    } else {
			//clicked occupied spot, remove a shape
			model.eraseShape(gc, point);
		    }
                }  
            });

        canvasPane.addEventHandler(MouseEvent.MOUSE_PRESSED, (MouseEvent t) -> {
                if (t.getClickCount() != 1) return;
                
                switch(t.getButton()) {
                  case PRIMARY:
                    contextMenu.hide();
                    cx = t.getX();
                    cy = t.getY();
                    model.liftShape(gc, cx, cy);
                    break;
                  case SECONDARY:
                    cmPoint = model.findPointAt(t.getX(), t.getY());
                    if (cmPoint != null) {
                        contextMenu.show(canvasPane, t.getScreenX(), t.getScreenY());
                    } else {
                        contextMenu.hide();
                    }
                    break;
                }
            });
        
        canvasPane.addEventHandler(MouseEvent.MOUSE_RELEASED, (MouseEvent t) -> {
                model.releaseShape(gc);
            });
        
        canvasPane.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent e) -> {
                double dx = e.getX();
                double dy = e.getY();
                model.moveShape(gc, dx-cx, dy-cy);
                cx = dx;
                cy = dy;
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
  
       trackBar.getItems().clear();
       addButton(trackBar, trackGroup, "solid");
       addButton(trackBar, trackGroup, "middot");
       addButton(trackBar, trackGroup, "tall");
       addButton(trackBar, trackGroup, "straight");
       addButton(trackBar, trackGroup, "cross");
    }

    private void addButton(ToolBar bar, ToggleGroup group, String label) {
        Shape shape = model.shapesMap.get(label);
        Canvas bc = new Canvas(shape.getWidth(), shape.getHeight());
        GraphicsContext gc = bc.getGraphicsContext2D();
        gc.translate(shape.getWidth()/2, shape.getHeight()/2);
        shape.draw(gc, Color.GREEN);
        
        //ToggleButton button = new ToggleButton(label, bc);
        ToggleButton button = new ToggleButton(null, bc);
        button.setToggleGroup(group);
        button.setUserData(shape);
        bar.getItems().add(button);
    }
    

    public static class SavedPlace
    {
	public double x;
	public double y;
	public double angle = 0;
        public String shape;
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
		sp.angle = place.angle;
                sp.shape = place.shape.getId();
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
