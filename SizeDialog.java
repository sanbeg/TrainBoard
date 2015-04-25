import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.function.ToDoubleFunction;

public class SizeDialog {

    private final Dialog<ButtonType> dialog = new Dialog<>();

    ToDoubleFunction<TextField> f = tf->Double.parseDouble(tf.getText());

    private final TextField widthfield = new TextField();
    private final TextField heightfield = new TextField();

    public SizeDialog() {
	dialog.setTitle("New Board");
	dialog.getDialogPane().getButtonTypes().addAll(
						       ButtonType.OK,
						       ButtonType.CANCEL
						       );

	widthfield.setPromptText("Width");
	heightfield.setPromptText("Height");

	GridPane grid = new GridPane();
	grid.setHgap(10);
	grid.setVgap(10);
	//grid.setPadding(new Insets(20, 150, 10, 10));
                    
	grid.add(new Label("Width:"), 0, 0);
	grid.add(widthfield, 1, 0);
	grid.add(new Label("Height:"), 0, 1);
	grid.add(heightfield, 1, 1);
	dialog.getDialogPane().setContent(grid);
    }

    public Length width;
    public Length height;
    
    public boolean prompt() 
    {
	boolean rv;
	
	if (! dialog
	    .showAndWait()
	    .filter(r -> r == ButtonType.OK)
	    .isPresent()){
	    return false;
	}
	
	String err = null;
				
	if (widthfield.getText().length() == 0 
	    || 
	    heightfield.getText().length() == 0) {
	    err = "Width and height are required";
	}
	else {
	    try {
		double w = f.applyAsDouble(widthfield);
		double h = f.applyAsDouble(heightfield);

		if (w>0 && h>0) {
		    width  = new Length(w);
		    height = new Length(h);
		} else {
		    err = "Width and height must be > 0";
		}
	    }
	    catch (NumberFormatException nfe) {
		err = "Invalid number: " 
		    + nfe.getMessage();
	    }
	}
				
				
	if (err != null) {
	    Alert alert 
		= new Alert(Alert.AlertType.ERROR);
	    alert.setHeaderText("Error reading size");
	    alert.setContentText(err);
	    alert.showAndWait();
	    rv = false;
	} else {
	    rv = true;
	}
		    
	return rv;
	
    }
}

