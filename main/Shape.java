import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class Shape {
    public final String id;
    private final double width;
    private final double height;

    public String getId()     { return id;     }
    public double getWidth()  { return width;  }
    public double getHeight() { return height; }
        
    public Shape(String id, double w, double h) {
        this.id = id;
        this.width = w;
        this.height = h;
    }

    abstract public void draw(GraphicsContext gc, Color color);

    public void erase(GraphicsContext gc) {
        //add 1 for rotated shape
        gc.clearRect(
            -getWidth()/2-1, 
            -getHeight()/2-1,
            getWidth()+2, 
            getHeight()+2
        );
    }

    public boolean hasConnections()                 { return false; }
    public Track.LocalConnection[] getConnections() { return null;  }
    public double connectionSize()                  { return 0;     }
        
    public static class SolidSquare extends Shape 
    {
	private final double arc = 10;
        
        public SolidSquare(String id, double w, double h) {
            super(id, w, h);
        }
        
	public void draw(GraphicsContext gc, Color color) {
	    gc.setFill(color);
	    gc.fillRoundRect(
			     -getWidth()/2,
			     -getHeight()/2,
			     getWidth(),
			     getHeight(),
			     arc, arc);
	}
    }

    public static class MidDot extends SolidSquare {
	private final double diameter;

        public MidDot(String id, double w, double h, double d) {
            super(id, w, h);
            this.diameter = d;
        }
        
	public void draw(GraphicsContext gc, Color color) 
	{
            super.draw(gc, Color.LIGHTGREY);
	    gc.setFill(color);
	    gc.fillOval(-diameter/2, -diameter/2, diameter, diameter);
	}
    }

    
}

