import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public interface Shape {
    double getWidth();
    double getHeight();
        
    void draw(GraphicsContext gc, Color color);
    void erase(GraphicsContext gc);

    String getId();

default boolean hasConnections() 
    {
	return false;
    }
    
default LocalConnection[] getConnections() 
    {
	return null;
    }
    

    public static class LocalConnection {
	public final double x;
	public final double y;
	public final double angle;

	public LocalConnection(double x, double y, double angle) 
	{
	    this.x = x;
	    this.y = y;
	    this.angle = angle;
	}
	
    }

    public static class SolidSquare implements Shape 
    {
	private final double width;
        private final double height;
        
	private final double arc = 10;
        private final String id;
        
        public SolidSquare(String id, double w, double h) {
            this.id = id;
            this.width = w;
            this.height = h;
        }
        

	public double getWidth() 
	{
	    return width;
	}
	public double getHeight() {
            return height;
        }

        public String getId() {
            return id;
        }
        
	public void draw(GraphicsContext gc, Color color) 
	{
	    gc.setFill(color);
	    gc.fillRoundRect(
			     -getWidth()/2,
			     -getHeight()/2,
			     getWidth(),
			     getHeight(),
			     arc, arc);
	}
	public void erase(GraphicsContext gc) 
	{
	    //add 1 for rotated shape
            gc.clearRect(
			 -getWidth()/2-1, 
			 -getHeight()/2-1,
			 getWidth()+2, 
			 getHeight()+2
			 );
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

    public static class Straight extends SolidSquare
    {
	private final double gauge;
	private final LocalConnection[] connections;
	
	@Override public boolean hasConnections() {
	    return true;
	}
	@Override public LocalConnection [] getConnections() {
	    return connections;
	}

        public Straight(String id, Length gauge, Length length) {
            super(id, gauge.getPixels()*1.2, length.getPixels());
	    double h = length.getPixels();
	    this.gauge = gauge.getPixels();
	    
	    connections = new LocalConnection[] {
		new LocalConnection(0, -h/2, 0),
		new LocalConnection(0, +h/2, 180)
	    };
	    
	    
        }
        
        public void draw(GraphicsContext gc, Color color) 
            {
                //ballast
                super.draw(gc, Color.IVORY);

                //ties
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(3.0);
                //double tieX = getWidth()*0.45;
		double tieX = getWidth() / 2 - 2;
                
                for (int i=0; i<10; ++i) {
                    double h = getHeight();
                    double y = -h/2 + h*0.1*i + h*0.05;
                    gc.strokeLine(-tieX, y, tieX, y);
                }

                //rails
                gc.setStroke(Color.SILVER.darker());
                gc.setLineWidth(1.0);
                
                double g2 = gauge/2;
                gc.strokeLine(-g2, -getHeight()/2, -g2, getHeight()/2);
                gc.strokeLine(+g2, -getHeight()/2, +g2, getHeight()/2);

                //indicator
/*
                gc.setFill(color);
                double diameter = 4.0;
                gc.fillOval(-diameter/2, -diameter/2, diameter, diameter);
*/
                //gc.setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.4));
                gc.setFill(color.interpolate(Color.TRANSPARENT, 0.6));
                
                gc.fillOval(-g2, -getHeight()/2, 2*g2, 2*g2);
                gc.fillOval(-g2, +getHeight()/2-2*g2, 2*g2, 2*g2);
            }
        
    }

    public static class Road extends SolidSquare
    {
        private final double length; //road length
        private final double w;
        
        private final static double arc = 10;
        
        public Road(String id, double w, double h) {
            super(id, h, h);
            length = w;
            this.w = w;
        }
        
        public void draw(GraphicsContext gc, Color color) 
            {
                //ballast
                gc.setFill(Color.IVORY);
                gc.fillRoundRect(
                    -w/2, -getHeight()/2,
                    w, getHeight(),
                    arc, arc);

                double tieX = w*0.45;

                //road
                gc.setFill(Color.BLACK);
                gc.fillRect(
                    -getHeight()/2, -length/2,
                    getHeight(), length
                    );
                gc.setStroke(Color.YELLOW);
                gc.setLineWidth(1.0);
                gc.strokeLine(-getHeight()/2, 0, -tieX, 0);
                gc.strokeLine(+tieX, 0, getHeight()/2, 0);
                
                //ties
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(3.0);
                
                for (int i=0; i<10; ++i) {
                    double h = getHeight();
                    double y = -h/2 + h*0.1*i + h*0.05;
                    gc.strokeLine(-tieX, y, tieX, y);
                }

                //rails
                gc.setStroke(Color.SILVER.darker());
                gc.setLineWidth(1.0);
                
                double gauge = w*0.4;
                gc.strokeLine(-gauge, -getHeight()/2, -gauge, getHeight()/2);
                gc.strokeLine(+gauge, -getHeight()/2, +gauge, getHeight()/2);

                gc.setFill(color.interpolate(Color.TRANSPARENT, 0.6));
                
                gc.fillOval(-gauge, -getHeight()/2, 2*gauge, 2*gauge);
                gc.fillOval(-gauge, +getHeight()/2-2*gauge, 2*gauge, 2*gauge);
            }
        
    }

 
    public static class Cross extends SolidSquare
    {
        private final double trackWidth;
        private final double arc = 10;

	private final LocalConnection[] connections 
	    = new LocalConnection[4];
	
	@Override public boolean hasConnections() {
	    return true;
	}
	@Override public LocalConnection [] getConnections() {
	    return connections;
	}

        public Cross(String id, double w, double h) {
            super(id, h, h);
            trackWidth = w;

	    connections[0] = new LocalConnection(0, -h/2, 0);
	    connections[1] = new LocalConnection(+h/2, 0, 90);
	    connections[2] = new LocalConnection(0, +h/2, 180);
	    connections[3] = new LocalConnection(-h/2, 0, 270);
        }

        public void draw(GraphicsContext gc, Color color) {
            //ballast
	    gc.setFill(Color.IVORY);
	    gc.fillRoundRect(
                -trackWidth/2,
                -getHeight()/2,
                trackWidth,
                getHeight(),
                arc, arc);

	    gc.fillRoundRect(
                -getHeight()/2,
                -trackWidth/2,
                getHeight(),
                trackWidth,
                arc, arc);
            // center
            double tieX = trackWidth*0.45;
            double arc2 = 4;
            gc.setFill(Color.BLACK);
            gc.fillRoundRect(-tieX*2, -tieX, tieX*4, tieX*2, arc2, arc2);
            gc.fillRoundRect(-tieX, -tieX*2, tieX*2, tieX*4, arc2, arc2);
            
            // ties - TODO

            //rails
            gc.setStroke(Color.SILVER.darker());
            gc.setLineWidth(1.0);
                
            double gauge = trackWidth*0.4;
            gc.strokeLine(-gauge, -getHeight()/2, -gauge, getHeight()/2);
            gc.strokeLine(+gauge, -getHeight()/2, +gauge, getHeight()/2);

            gc.strokeLine(-getWidth()/2, -gauge, getWidth()/2, -gauge);
            gc.strokeLine(-getWidth()/2, +gauge, getWidth()/2, +gauge);
           
            gc.setFill(color.interpolate(Color.TRANSPARENT, 0.6));
            gc.fillOval(-gauge, -getHeight()/2, 2*gauge, 2*gauge);
            gc.fillOval(-gauge, +getHeight()/2-2*gauge, 2*gauge, 2*gauge);

        }
        
    }
}

