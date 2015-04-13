import javafx.geometry.Point2D;
import javafx.scene.transform.Rotate;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Point 
{
    public double x;
    public double y;
    public double angle = 0;
        
    public final Shape shape;
    public boolean obscured = false;
        
    public final GlobalConnection[] connections;

    public Point(double x, double y, Shape s) {
        this.x = x;
        this.y = y;
        this.shape = s;

        if (s.hasConnections()) {
            Track.LocalConnection [] lc = s.getConnections();
            connections = new GlobalConnection[lc.length];
            for (int i=0; i<lc.length; ++i) {
                connections[i] = new GlobalConnection(lc[i], x, y);
            }
        } else {
            connections = null;
        }

    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
	
    public void addXy(double x, double y) {
	this.x += x;
	this.y += y;
    }
    
    public double getWidth() {
        Point2D right = new Rotate(angle)
            .transform(shape.getWidth()/2, shape.getHeight()/2);
        Point2D left = new Rotate(angle)
            .transform(-shape.getWidth()/2, shape.getHeight()/2);
        double w = Math.max(Math.abs(left.getX()),Math.abs(right.getX())) * 2;
        //System.out.println("W=" + w);
        return w;
            
    }
    public double getHeight() {
        Point2D top = new Rotate(angle)
            .transform(shape.getWidth()/2, shape.getHeight()/2);
        Point2D bottom = new Rotate(angle)
            .transform(shape.getWidth()/2, -shape.getHeight()/2);
        double h = Math.max(Math.abs(top.getY()),Math.abs(bottom.getY())) * 2;
        return h;
    }
        
    public boolean covers(double x, double y) {
        double w2 = getWidth()/2;
        double h2 = getHeight()/2;
        return x < this.x+w2 
            && x > this.x-w2 
            && y < this.y+h2 
            && y > this.y-h2;
    }
    public boolean overlaps(Point other) {
        double width = (getWidth() + other.getWidth())/2;
        double height = (getHeight() + other.getHeight())/2;
        return Math.abs(x - other.x) < width 
            && Math.abs(y - other.y) < height;
    }

    public boolean obscures(Point other) {
        double width = (getWidth() + other.getWidth() + 2)/2;
        double height = (getHeight() + other.getHeight() + 2)/2;
        return Math.abs(x - other.x) < width 
            && Math.abs(y - other.y) < height;
    }

    public void draw(GraphicsContext gc, Color color) 
	{
	    //Affine transform = gc.getTransform();
	    gc.save();
	    gc.translate(x, y);
            gc.rotate(angle);
            //todo - make bounding box
	    shape.draw(gc, color);
	    //gc.setTransform(transform);
	    gc.restore();
	}
    public void erase(GraphicsContext gc) 
	{
	    gc.save();
	    gc.translate(x, y);
            gc.rotate(angle);
	    shape.erase(gc);
	    gc.restore();
	}
 
}
