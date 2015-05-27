import javafx.scene.control.Alert;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.event.ActionEvent;

//image export
import javafx.scene.image.WritableImage;
import javafx.scene.SnapshotParameters;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

import java.io.File;
import java.io.IOException;

import javafx.util.Pair;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class BoardController {
    
    private final Stage stage;
    
    public Pane canvasPane;
    public ToolBar trackBar;
    
    public MenuItem newItem;
    public MenuItem openItem;
    public MenuItem saveItem;
    public MenuItem saveAsItem;
    public MenuItem exportItem;
    public MenuItem closeItem;

    public MenuItem moveLeftItem;
    public MenuItem moveRightItem;
    public MenuItem moveUpItem;
    public MenuItem moveDownItem;
    public MenuItem moveCenterItem;
    
    private final BoardModel model = new BoardModel();
    private final ShapeBox shapeBox = new ShapeBox();
    
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

    private Point cmPoint = null;

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
    
    private Length width;
    private Length height;


    private void resizeBoard(Canvas canvas, Canvas floatingCanvas) {
	canvas.setWidth(width.getPixels());
	floatingCanvas.setWidth(width.getPixels());
	canvas.setHeight(height.getPixels());
	floatingCanvas.setHeight(height.getPixels());
    }
    
    private void loadFile(File file, Canvas canvas, Canvas floatingCanvas) {
	SavedBoard sb = JAXB.unmarshal(file, SavedBoard.class);
	if (sb.width > 0 && sb.height > 0) {
	    width = new Length(sb.width);
	    height = new Length(sb.height);
            //Length.ppi = sb.dpi;
            
	    resizeBoard(canvas, floatingCanvas);
	} else {
	    System.err.println("missing size in save file");
	}
		    
	if (sb.tracks != null) {
	    addAllPlaces(sb.tracks);
	}
    }
    
    private void confirmClose(javafx.event.Event event) {
        if (model.isDirty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Close");
            alert.setHeaderText("Discard changes?");
            alert.showAndWait()
                .filter(response -> response == ButtonType.CANCEL)
                .ifPresent(response -> event.consume());
        }
    }
    

    public void initialize() {
        stage.setTitle(TITLE_PREFIX);
        stage.setOnCloseRequest(this::confirmClose);
        
	{
	    Screen screen = Screen.getPrimary();
	    System.out.println("DPI = " + screen.getDpi());
	    stage.setHeight(screen.getVisualBounds().getHeight() * 0.9);
	    stage.setWidth(screen.getVisualBounds().getWidth() * 0.9);
	}
	
	width = new Length(4*12, Length.Unit.IN);
	height = new Length(2*12, Length.Unit.IN);
	
        final Canvas canvas = new Canvas(width.getPixels(), height.getPixels());
        final Canvas floatingCanvas = new Canvas(width.getPixels(), height.getPixels());
        GraphicsContext gc = canvas.getGraphicsContext2D();
        model.setFloatingContext(floatingCanvas.getGraphicsContext2D());

        canvasPane.getChildren().add(canvas);
        canvasPane.getChildren().add(floatingCanvas);

        //System.out.println(canvasPane.getHeight() + " = " + canvas.getHeight());
        
        final ContextMenu contextMenu = makeContextMenu(gc);
	final ToggleGroup trackGroup = new ToggleGroup();

        canvasPane.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
                if (t.getClickCount() > 1 && t.isStillSincePress()) {
		    Point point = 
			model.findPointAt(t.getX(), t.getY());
		    if (point == null) {
			//clicked empty spot, add shape
                        Toggle button = trackGroup.getSelectedToggle();
                        if (button != null) {
                            Shape shape = (Shape)button.getUserData();
                            model.addShape(gc, t.getX(), t.getY(), shape);
                        }
                        else {
                            previewShape.ifPresent(s -> model.addShape(gc, t.getX(), t.getY(), s));
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
        
        canvasPane.addEventHandler(MouseEvent.MOUSE_RELEASED, (MouseEvent t)->{
                if (! t.isControlDown()) {
                    model.releaseShape(gc);
                }
            });
        
        canvasPane.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent e)->{
                double dx = e.getX();
                double dy = e.getY();
                model.moveShape(gc, dx-cx, dy-cy);
                cx = dx;
                cy = dy;
            });
        
        closeItem.setOnAction((javafx.event.ActionEvent e) -> {stage.close();});

        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters()
	    .addAll(
		    new FileChooser.ExtensionFilter("XML Files", "*.xml"),
		    new FileChooser.ExtensionFilter("All Files", "*.*")
		    );
       
	if (file != null) {
	    updateFile(file, fileChooser);
	    loadFile(file, canvas, floatingCanvas);
	    model.redraw(gc);
	}
       
	final SizeDialog sizeDialog = new SizeDialog();
	
	newItem.setOnAction((ActionEvent e) -> {
                confirmClose(e);
                if (e.isConsumed()) return;
 
		if (sizeDialog.prompt()) {
		    width = sizeDialog.width;
		    height = sizeDialog.height;
		
                    resizeBoard(canvas, floatingCanvas);		
                    resetBoard(gc, canvas);
                    file = null;
                    saveItem.setDisable(true);
                    stage.setTitle(TITLE_PREFIX);
		}
	    });

	openItem.setOnAction((ActionEvent e) -> {
                confirmClose(e);
                if (e.isConsumed()) return;
                
		fileChooser.setTitle("Open Layout");
		File file = fileChooser.showOpenDialog(stage);
		if (file != null) {
		    updateFile(file, fileChooser);
		    resetBoard(gc, canvas);
		    loadFile(file, canvas, floatingCanvas);
		    model.redraw(gc);
		}
	    });

	saveItem.setOnAction((ActionEvent ev) -> {
		if (file != null) {
		    SavedBoard savedBoard = new SavedBoard();
                    savedBoard.width = width.getInches();
                    savedBoard.height = height.getInches();
                        
		    savedBoard.setAll(model.shapes);
                    //savedBoard.setShapes(model.shapesMap);
		    JAXB.marshal(savedBoard, file);
                    model.makeClean();
		}
	    });
               

        saveAsItem.setOnAction((ActionEvent ev) -> {
                fileChooser.setTitle("Save Layout");
                File file = fileChooser.showSaveDialog(stage);
                if (file != null) {
                    updateFile(file, fileChooser);
                    SavedBoard savedBoard = new SavedBoard();
                    savedBoard.width = width.getInches();
                    savedBoard.height = height.getInches();
                    savedBoard.setAll(model.shapes);
                    //savedBoard.setShapes(model.shapesMap);
                    JAXB.marshal(savedBoard, file);
                    model.makeClean();
                }
            });
  
	
        final FileChooser imageFileChooser = new FileChooser();
        imageFileChooser.getExtensionFilters()
	    .addAll(
		    new FileChooser.ExtensionFilter("PNG Files", "*.png"),
		    new FileChooser.ExtensionFilter("All Files", "*.*")
		    );
        imageFileChooser.setTitle("Export Image");

        exportItem.setOnAction((ActionEvent ev) -> {
                File file = imageFileChooser.showSaveDialog(stage);
                if (file != null) {
                    WritableImage image;
                    
                    image = canvasPane.snapshot(null, null);
                    /*
                       // this will draw a transparent snapshot
                    SnapshotParameters sp = new SnapshotParameters();
                    sp.setFill(Color.TRANSPARENT);
                    image = canvas.snapshot(sp, image);
                    */

                    try {
                        ImageIO.write(SwingFXUtils.fromFXImage(image, null),
                                      "png",
                                      file);
                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            });
        
        moveLeftItem.setOnAction((ActionEvent ev) -> model.goLeft(gc));
        moveRightItem.setOnAction((ActionEvent ev) -> model.goRight(gc, width.getPixels()));
        moveUpItem.setOnAction((ActionEvent ev) -> model.goUp(gc));
        moveDownItem.setOnAction((ActionEvent ev) -> model.goDown(gc, height.getPixels()));
        moveCenterItem.setOnAction((ActionEvent ev) -> model.goCenter(gc, width.getPixels(), height.getPixels()));
        
        trackBar.getItems().clear();
	for (Shape shape : shapeBox.getShapes()) {
	    addButton(trackBar, trackGroup, shape);
	}

        addTrackTree();
    }

    private Optional<Shape> previewShape = Optional.empty();
    
    public Canvas treePreview;
    public TreeView<ShapeBox.TreeTrack> shapeTree;
    
    private void addTrackTree() {

        final GraphicsContext gc = treePreview.getGraphicsContext2D();
        shapeTree.setRoot(shapeBox.getTree());
        
        gc.translate(treePreview.getWidth()/2, treePreview.getHeight()/2);

        shapeTree.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue == null) { 
                    //nothing selected and clicked outside of tree
                }
                else if (newValue.isLeaf()) {
                    newValue.getValue().shape.ifPresent(shape -> {
                            previewShape.ifPresent(old -> old.erase(gc));
                            shape.draw(gc, Color.TRANSPARENT);
                            previewShape = Optional.of(shape);
                        });
                } else {
                    //newValue.setExpanded(true);
                }
            });

    }
    
    private void addButton(ToolBar bar, ToggleGroup group, Shape shape) {
        Canvas bc = new Canvas(shape.getWidth(), shape.getHeight());
        GraphicsContext gc = bc.getGraphicsContext2D();
        gc.translate(shape.getWidth()/2, shape.getHeight()/2);
        shape.draw(gc, Color.TRANSPARENT);
        
        //ToggleButton button = new ToggleButton(label, bc);
        ToggleButton button = new ToggleButton(null, bc);
        button.setToggleGroup(group);
        button.setUserData(shape);
        bar.getItems().add(button);
    }
    
    public void addAllPlaces(List<SavedPlace> savedPlaces) 
    {
	Map<String,Shape> shapesMap = new HashMap<>();
	for (Shape s : shapeBox.getShapes()) {
	    shapesMap.put(s.getId(), s);
	}
	
	for (SavedPlace sp : savedPlaces) {
	    Shape s = shapesMap.get(sp.shape);
            if (s == null) s = shapesMap.get("solid");
	    Point p = new Point(sp.x, sp.y, s);
	    p.angle = sp.angle;
	    model.addPoint(p);
	}
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
        @XmlAttribute public final double dpi = Length.ppi;
        
        public List<SavedPlace> tracks = new java.util.ArrayList<>();

/*
        @XmlElementWrapper @XmlElement(name="shape")
        public List<Shape> shapes = new java.util.ArrayList<>();
*/
        @XmlAttribute public double width;
        @XmlAttribute public double height;
        
	public void setAll(List<Point> points) 
	{
	    for (Point place: points) {
		SavedPlace sp = new SavedPlace();
		sp.x = place.getX();
		sp.y = place.getY();
		sp.angle = place.angle;
                sp.shape = place.shape.getId();
		tracks.add(sp);
	    }
        }
/*
        public void setShapes(java.util.Map<String, Shape> shapesMap) 
        {
            for (Shape s : shapesMap.values()) {
                shapes.add(s);
            }
        }
*/      
    }

    private void resetBoard(GraphicsContext gc, Canvas canvas) 
    {
        model.shapes.clear();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        model.makeClean();
    }

}
