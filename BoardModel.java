import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;
import javafx.scene.transform.Rotate;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class BoardModel 
{
    
    public static class Point 
    {
        public double x;
        public double y;
        public double angle = 0;
        
	public final Shape shape;
	public boolean obscured = false;
        
        public Point(double x, double y, Shape s) {
            this.x = x;
            this.y = y;
            this.shape = s;
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


    public final List<Point> shapes = new java.util.ArrayList<>();

    public final Map<String,Shape> shapesMap = new HashMap<>();
        {
            shapesMap.put("middot", new Shape.MidDot("middot", 30, 30, 4));
            shapesMap.put("solid", new Shape.SolidSquare("solid", 30, 30));
            shapesMap.put("tall", new Shape.SolidSquare("tall", 30, 60));
            shapesMap.put("straight", new Shape.Straight("straight", 16, 96));
        }

    public void addAllPlaces(List<BoardController.SavedPlace> savedPlaces) 
    {
	for (BoardController.SavedPlace sp: savedPlaces) {
            Shape s = shapesMap.get(sp.shape);
            if (s == null) s = shapesMap.get("solid");
	    Point p = new Point(sp.x, sp.y, s);
	    p.angle = sp.angle;
	    shapes.add(p);
	}
    }
    
    public void redraw(GraphicsContext gc) 
    {
        for (Point p : shapes) {
	    p.draw(gc, Color.GREEN);
        }
    }
    
                
    public void addShape(GraphicsContext gc, double x, double y, Shape shape) {
	Point p = new Point(x,y, shape);
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
        double minDist = Double.MAX_VALUE;
        
        for (Point p : shapes) {
            if (p.covers(x, y)) {
                double dx = p.x-x;
                double dy = p.y-y;
                
                double dist = Math.sqrt(dx*dx + dy*dy);
                if (dist < minDist) {
                    minDist = dist;
                    old = p;
                }
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
                ov.obscured = false;
            }
        }
        if (ov != null) {

            Point2D newPoint;
	    newPoint = new Rotate(-ov.angle, ov.x, ov.y).transform(old.x, old.y);
            old.angle = ov.angle;
            old.x = newPoint.getX();
            old.y = newPoint.getY();

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

	    newPoint = new Rotate(ov.angle, ov.x, ov.y).transform(old.x, old.y);
            old.angle = ov.angle;
            old.x = newPoint.getX();
            old.y = newPoint.getY();
            
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
    
    private void redrawAround(GraphicsContext gc, Point point, Color color) {
        for (Point p : shapes) {
            if (p == point) continue;
            
            if (point.obscures(p)) {
                p.draw(gc, color);
            }
        }
    }
    

    public void moveShape(GraphicsContext gc, double x, double y, Color color) {
        if (heldPoint != null) {
            Point old = heldPoint;
	    old.erase(gc);
            redrawAround(gc, old, Color.GREEN);
            gc.setFill(Color.GREEN);

            for (Point p : shapes) {
                if (p == old) continue;
		
                if (p.overlaps(old)) {
                    p.draw(gc, Color.RED);
                    p.obscured = true;
                }
                else if (p.obscures(old)) {
                    p.draw(gc, Color.YELLOW);
                    p.obscured = true;
                }
                else if (p.obscured) {
		    //can leave traces in round corners or rotated edges
		    p.erase(gc);
                    redrawAround(gc, p, Color.GREEN);
                    p.draw(gc, Color.GREEN);
                    p.obscured = false;
                    //System.out.printf("Redraw %.1f,%.1f\n", p.x, p.y);
                }
            }
            old.x += x;
            old.y += y;
	    old.draw(gc, color);
        }
        
    }

    public void rotateShape(GraphicsContext gc, Point point, double angle) {
        point.erase(gc);
        point.angle += angle;
        point.draw(gc, Color.GREEN);
    }
    

}

    