import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Dialog;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.event.ActionEvent;

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

    private void loadFile(File file, Canvas canvas, Canvas floatingCanvas) {
	SavedBoard sb = JAXB.unmarshal(file, SavedBoard.class);
	if (sb.width > 0 && sb.height > 0) {
	    width = new Length(sb.width);
	    height = new Length(sb.height);
            
	    canvas.setWidth(width.getPixels());
	    floatingCanvas.setWidth(width.getPixels());
	    canvas.setHeight(height.getPixels());
	    floatingCanvas.setHeight(height.getPixels());
	} else {
	    System.err.println("missing size in save file");
	}
		    
	if (sb.tracks != null) {
	    addAllPlaces(sb.tracks);
	}
    }

    public void initialize() {
        stage.setTitle(TITLE_PREFIX);

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
                model.releaseShape(gc);
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
       
	newItem.setOnAction((ActionEvent e) -> {
                {
                    Dialog<ButtonType> dialog = new Dialog<>();
                    dialog.setTitle("New Board");
                    
                    dialog.getDialogPane().getButtonTypes().addAll(
                        ButtonType.OK,
                        ButtonType.CANCEL
                        );


                    GridPane grid = new GridPane();
                    grid.setHgap(10);
                    grid.setVgap(10);
                    //grid.setPadding(new Insets(20, 150, 10, 10));
                    
                    TextField widthfield = new TextField();
                    widthfield.setPromptText("Width");
                    TextField heightfield = new TextField();
                    heightfield.setPromptText("Height");
                    
                    grid.add(new Label("Width:"), 0, 0);
                    grid.add(widthfield, 1, 0);
                    grid.add(new Label("Height:"), 0, 1);
                    grid.add(heightfield, 1, 1);
                    dialog.getDialogPane().setContent(grid);
                    
                    dialog.showAndWait()
                        .filter(r -> r == ButtonType.OK)
                        .filter(r -> widthfield.getText().length() > 0)
                        .filter(r -> heightfield.getText().length() > 0)
                        .ifPresent(bt -> System.out.printf("w=%s, h=%s\n", 
                                                           widthfield.getText()));
                    
                }
                
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
    }

}
