//package dustin.examples;

import java.lang.reflect.Field;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.stage.Stage;

/**
 * Simple JavaFX 2 application that prints out values of standardly available
 * Color fields.
 * 
 * @author Dustin
 */
public class JavaFxColorDemo extends Application
{
   /** Width of label for colorn name. */
   private final static int COLOR_NAME_WIDTH = 150;
   /** Width of rectangle that displays color. */
   private final static int COLOR_RECT_WIDTH = 50;
   /** Height of rectangle that displays color. */
   private final static int COLOR_RECT_HEIGHT = 25;

    private final static String WEB_TEXT = "#fefefe";
    
   private final TextField webField = TextFieldBuilder.create().text(WEB_TEXT).build();
   private final Rectangle customColorRectangle = RectangleBuilder.create()
      .width(COLOR_RECT_WIDTH).height(COLOR_RECT_HEIGHT)
       .fill(Color.web(WEB_TEXT)).stroke(Color.BLACK).build();

   /**
    * Build a pane containing details about the instance of Color provided.
    * 
    * @param color Instance of Color about which generated Pane should describe.
    * @return Pane representing information on provided Color instance.
    */
   private Pane buildColorBox(final Color color, final String colorName)
   {
      final HBox colorBox = new HBox();
      final Label colorNameLabel = new Label(colorName);
      colorNameLabel.setMinWidth(COLOR_NAME_WIDTH);
      colorBox.getChildren().add(colorNameLabel);
      final Rectangle colorRectangle = new Rectangle(COLOR_RECT_WIDTH, COLOR_RECT_HEIGHT);
      colorRectangle.setFill(color);
      colorRectangle.setStroke(Color.BLACK);
      colorBox.getChildren().add(colorRectangle);
      //add opacticy?
      String rgbString = String.format("#%X%X%X", 
                                       (int)(color.getRed()*256),
                                       (int)(color.getGreen()*256),
                                       (int)(color.getBlue()*256));
      final Label rgbLabel = new Label(rgbString);
      
      colorBox.getChildren().add(rgbLabel);
      return colorBox;
   }

   /**
    * Build pane with ability to specify own RGB values and see color.
    * 
    * @return Pane with ability to specify colors.
    */
   private Pane buildCustomColorPane()
   {
      final HBox customBox = new HBox();
      final Button button = new Button("Display Color");
      button.setPrefWidth(COLOR_NAME_WIDTH);
      button.setOnMouseClicked(new EventHandler<MouseEvent>()
      {
         @Override
         public void handle(MouseEvent t)
         {
             Color customColor = Color.web(webField.getText());
             customColorRectangle.setFill(customColor);
         }
      });
      customBox.getChildren().add(button);
      customBox.getChildren().add(this.customColorRectangle);
      customBox.getChildren().add(this.webField);
      return customBox;
   }

   /**
    * Build the main pane indicating JavaFX 2's pre-defined Color instances.
    * 
    * @return Pane containing JavaFX 2's pre-defined Color instances.
    */
   private Pane buildColorsPane()
   {
      final VBox colorsPane = new VBox();
      final Field[] fields = Color.class.getFields(); // only want public
      for (final Field field : fields)
      {
         if (field.getType() == Color.class)
         {
            try
            {
               final Color color = (Color) field.get(null);
               final String colorName = field.getName();
               colorsPane.getChildren().add(buildColorBox(color, colorName));
            }
            catch (IllegalAccessException illegalAccessEx)
            {
               System.err.println(
                  "Securty Manager does not allow access of field '"
                  + field.getName() + "'.");
            }
         }
      }
      colorsPane.getChildren().add(buildCustomColorPane());
      return colorsPane;
   }

   /**
    * Start method overridden from parent Application class.
    * 
    * @param stage Primary stage.
    * @throws Exception JavaFX application exception.
    */
   @Override
   public void start(final Stage stage) throws Exception
   {
      final Group rootGroup = new Group();
      final Scene scene = new Scene(rootGroup, 700, 725, Color.WHITE);
      final ScrollPane scrollPane = new ScrollPane();
      scrollPane.setPrefWidth(scene.getWidth());
      scrollPane.setPrefHeight(scene.getHeight());
      scrollPane.setContent(buildColorsPane());
      rootGroup.getChildren().add(scrollPane);
      stage.setScene(scene);
      stage.setTitle("JavaFX Standard Colors Demonstration");
      stage.show();
   }

   /**
    * Main function for running JavaFX application.
    * 
    * @param arguments Command-line arguments; none expected.
    */
   public static void main(final String[] arguments)
   {
      Application.launch(arguments);
   }
}
