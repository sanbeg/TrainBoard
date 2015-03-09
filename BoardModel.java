import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

public class BoardModel 
{
    private static final double WIDTH = 30;
    private static final double ARC   = WIDTH/3;
    
    public static class Point 
    {
        public double x;
        public double y;
        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
        public Point() 
        {
            this.x = this.y = -1;
        }
        
        public boolean covers(double x, double y) {
            double w2 = WIDTH/2;
            return (x < this.x+w2 && x > this.x-w2 && y < this.y+w2 && y > this.y-w2);
        }
        public boolean overlaps(Point other) {
            return Math.abs(x - other.x) < WIDTH && Math.abs(y - other.y) < WIDTH;
        }
                
	public void draw(GraphicsContext gc, Color color) 
	{
	    gc.setFill(color);
	    gc.fillRoundRect(x-WIDTH/2, y-WIDTH/2, WIDTH, WIDTH, ARC, ARC);
	}
	public void erase(GraphicsContext gc) 
	{
            gc.clearRect(x-WIDTH/2, y-WIDTH/2, WIDTH, WIDTH);
	}
	
    }
    
    public final List<Point> shapes = new java.util.ArrayList<>();

    
    public void redraw(GraphicsContext gc) 
    {
        for (Point p : shapes) {
	    p.draw(gc, Color.GREEN);
        }
    }
    
                
    public void drawShape(GraphicsContext gc, double x, double y) {
        Point old = findPointAt(x, y);

        if (old == null) {
            //clicked empty spot, add shape
            Point p = new Point(x,y);
            snapShape(gc, p);
	    p.draw(gc, Color.GREEN);
	    shapes.add(p);
        }
        else {
            //clicked occupied spot, remove a shape
	    old.erase(gc);
            shapes.remove(old);

            for (Point p : shapes) {
		p.draw(gc, Color.GREEN);
            }
        }
    }

    private Point heldPoint = null;
    
    public Point findPointAt(double x, double y) {
        Point old = null;
        double w2 = WIDTH/2;
        
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

            if (xd < yd) {
                old.x = ov.x;
                old.y = (old.y > ov.y) ? ov.y+WIDTH : ov.y-WIDTH;
            }
            else {
                old.y = ov.y;
                old.x = (old.x > ov.x) ? ov.x+WIDTH : ov.x-WIDTH;
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

    