import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class BoardModel 
{
    
    public static class Point 
    {
        public double x;
        public double y;
	public final Shape shape;
	
        public Point(double x, double y, Shape s) {
            this.x = x;
            this.y = y;
            this.shape = s;
        }
        
        public boolean covers(double x, double y) {
            double w2 = shape.getWidth()/2;
            double h2 = shape.getHeight()/2;
            return x < this.x+w2 
		&& x > this.x-w2 
		&& y < this.y+h2 
		&& y > this.y-h2;
        }
        public boolean overlaps(Point other) {
	    double width = (shape.getWidth() + other.shape.getWidth())/2;
	    double height = (shape.getHeight() + other.shape.getHeight())/2;
            return Math.abs(x - other.x) < width 
		&& Math.abs(y - other.y) < height;
        }

	public void draw(GraphicsContext gc, Color color) 
	{
	    //Affine transform = gc.getTransform();
	    gc.save();
	    gc.translate(x, y);
	    shape.draw(gc, color);
	    //gc.setTransform(transform);
	    gc.restore();
	}
	public void erase(GraphicsContext gc) 
	{
	    gc.save();
	    gc.translate(x, y);
	    shape.erase(gc);
	    gc.restore();
	}

    }
    public interface Shape {
        double getWidth();
        double getHeight();
        
        void draw(GraphicsContext gc, Color color);
        void erase(GraphicsContext gc);

        String getId();
    }

    public static class SolidSquare implements Shape 
    {
	private final double width = 30;
	private final double arc = width/3;
        
	public double getWidth() 
	{
	    return width;
	}
	public double getHeight() {
            return width;
        }

        public String getId() {
            return "solid";
        }
        
	public void draw(GraphicsContext gc, Color color) 
	{
	    gc.setFill(color);
	    gc.fillRoundRect(-getWidth()/2, -getHeight()/2, getWidth(), getHeight(), arc, arc);
	}
	public void erase(GraphicsContext gc) 
	{
            gc.clearRect(-getWidth()/2, -getHeight()/2, getWidth(), getHeight());
	}
	
    }

    public static class MidDot extends SolidSquare {
	private final double diameter = 4;

        public String getId() {
            return "middot";
        }
        
	public void draw(GraphicsContext gc, Color color) 
	{
            super.draw(gc, Color.LIGHTGREY);
	    gc.setFill(color);
	    gc.fillOval(-diameter/2, -diameter/2, diameter, diameter);
	}
    }

    public class Tall extends SolidSquare {
        public String getId() {
            return "tall";
        }
        public double getHeight() {
            return getWidth() * 2;
        }
    }
    
    public final List<Point> shapes = new java.util.ArrayList<>();

    private final Map<String,Shape> ShapesMap = new HashMap<>();
        {
            ShapesMap.put("middot", new MidDot());
            ShapesMap.put("solid", new SolidSquare());
            ShapesMap.put("tall", new Tall());
        }
    

    public void addAllPlaces(List<BoardController.SavedPlace> savedPlaces) 
    {
	for (BoardController.SavedPlace sp: savedPlaces) {
            Shape s = ShapesMap.get(sp.shape);
            if (s == null) s = new SolidSquare();
	    Point p = new Point(sp.x, sp.y, s);
	    shapes.add(p);
	}
    }
    
    public void redraw(GraphicsContext gc) 
    {
        for (Point p : shapes) {
	    p.draw(gc, Color.GREEN);
        }
    }
    
                
    public void drawShape(GraphicsContext gc, double x, double y) {
	Point p = new Point(x,y, new MidDot());
	snapShape(gc, p);
	p.draw(gc, Color.GREEN);
	shapes.add(p);
    }
    
    public void eraseShape(GraphicsContext gc, Point old) 
    {
	old.erase(gc);
	shapes.remove(old);

	for (Point p : shapes) {
	    p.draw(gc, Color.GREEN);
	}
    }

    private Point heldPoint = null;
    
    public Point findPointAt(double x, double y) {
        Point old = null;
        
        for (Point p : shapes) {
            if (p.covers(x, y)) {
                old = p;
                break;
            }
        }
        return old;
    }
   

    public void liftShape(GraphicsContext gc, double x, double y, Color color) 
    {
        Point old = findPointAt(x, y);
        if (old != null) {
	    old.draw(gc, color);
            heldPoint = old;
        }
    }
    
    public void snapShape(GraphicsContext gc, Point old) {
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
		    old.erase(gc);
                    clear = true;
                }
                ov.draw(gc, Color.GREEN);
            }
        }
        if (ov != null) {
            double xd = Math.abs(old.x - ov.x);
            double yd = Math.abs(old.y - ov.y);
	    double width = (old.shape.getWidth()+ov.shape.getWidth())/2;
	    double height = (old.shape.getHeight()+ov.shape.getHeight())/2;
	    
            if (xd < yd) {
                old.x = ov.x;
                old.y = (old.y > ov.y) ? ov.y+height : ov.y-height;
            }
            else {
                old.y = ov.y;
                old.x = (old.x > ov.x) ? ov.x+width : ov.x-width;
            }
        }

    }
    

    public void releaseShape(GraphicsContext gc) 
    {
        if (heldPoint != null) {
            Point old = heldPoint;
            snapShape(gc, old);
            old.draw(gc, Color.GREEN);
            heldPoint = null;
        }
    }

    public void moveShape(GraphicsContext gc, double x, double y, Color color) {
        if (heldPoint != null) {
            Point old = heldPoint;
	    old.erase(gc);
            gc.setFill(Color.GREEN);
            
            for (Point p : shapes) {
                if (p == old) continue;
		Color pcolor;
		
                if (p.overlaps(old)) {
                    pcolor = Color.RED;
                }
                else {
                    pcolor = Color.GREEN;
                }
		p.draw(gc, pcolor);
            }
            old.x = x;
            old.y = y;
	    old.draw(gc, color);
        }
        
    }


}

    