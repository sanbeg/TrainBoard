import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;
import javafx.scene.transform.Rotate;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public class BoardModel 
{
    public GraphicsContext floatingContext;
    
    public final List<Point> shapes = new java.util.ArrayList<>();

    public final Map<String,Shape> shapesMap = new HashMap<>();
        {
            shapesMap.put("middot", new Shape.MidDot("middot", 30, 30, 4));
            shapesMap.put("solid", new Shape.SolidSquare("solid", 30, 30));
            shapesMap.put("tall", new Shape.SolidSquare("tall", 30, 60));
            shapesMap.put("straight", new Shape.Straight("straight", 16, 96));
            shapesMap.put("cross", new Shape.Cross("cross", 16, 64));
            shapesMap.put("road", new Shape.Road("road", 16, 64));
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
   

    public void liftShape(GraphicsContext gc, double x, double y) 
    {
        Point old = findPointAt(x, y);
        if (old != null) {
	    old.draw(gc, Color.BLUE);
            heldPoint = old;
        }
    }
    
    public void snapShape(GraphicsContext gc, Point held) {
        /*
         * If moved to overlap, push out of the way.
         * should handle multiple overlap, avoid putting 2 in same place.
         */

        Point ov = null;
        double minDist = Double.MAX_VALUE;
        double minCpDist = Double.MAX_VALUE;
        GlobalConnection heldCp = null;
        GlobalConnection nearCp = null;
	
        for (Point p : shapes) {
            if (p != held && p.overlaps(held)) {
		if (p.connections != null && held.connections != null) {
		    for (GlobalConnection hc : held.connections) {
			hc.moveTo(held.x, held.y, held.angle);
			for (GlobalConnection pc : p.connections) {
			    pc.moveTo(p.x, p.y, p.angle);
			    double dx = pc.x - hc.x;
			    double dy = pc.y - hc.y;
			    
			    double dist = Math.sqrt(dx*dx + dy*dy);
			    //System.out.printf("dist = %.2f (%.2f, %.2f)\n", dist, dx, dy);
			    
			    if (dist < minCpDist) {
				//System.out.printf("dist = %.2f -> %.2f\n", minCpDist, dist);
				minCpDist = dist;
				heldCp = hc;
				nearCp = pc;
				ov = p;
			    }
			    
			}
		    }
		} else {
		    double dx = p.x-held.x;
		    double dy = p.y-held.y;
                
		    double dist = Math.sqrt(dx*dx + dy*dy);
		    if (dist < minDist) {
			minDist = dist;
			ov = p;
		    }
		    p.draw(gc, Color.GREEN);
		}
            }
        }

	if (heldCp != null) {
	    held.erase(gc);
            ov.draw(gc, Color.GREEN);
            ov.obscured = false;

	    held.x += nearCp.x - heldCp.x;
	    held.y += nearCp.y - heldCp.y;
	    
            double angle = ov.angle + nearCp.connection.angle - heldCp.connection.angle + 180;
            Point2D p2d = new Rotate(angle - held.angle, nearCp.x, nearCp.y)
                .transform(held.x,held.y);
            
            held.angle = angle % 360;
            held.x = p2d.getX();
            held.y = p2d.getY();
            redrawAround(gc, held, Color.GREEN);
	}
        else

	if (ov != null) {
            held.erase(gc);
            ov.draw(gc, Color.GREEN);
            ov.obscured = false;

            Point2D newPoint;
	    newPoint = new Rotate(-ov.angle, ov.x, ov.y).transform(held.x, held.y);
            held.angle = ov.angle;
            held.x = newPoint.getX();
            held.y = newPoint.getY();

            double xd = Math.abs(held.x - ov.x);
            double yd = Math.abs(held.y - ov.y);
	    double width = (held.shape.getWidth()+ov.shape.getWidth())/2;
	    double height = (held.shape.getHeight()+ov.shape.getHeight())/2;
	    
            if (xd < yd) {
                held.x = ov.x;
                held.y = (held.y > ov.y) ? ov.y+height : ov.y-height;
            }
            else {
                held.y = ov.y;
                held.x = (held.x > ov.x) ? ov.x+width : ov.x-width;
            }

	    newPoint = new Rotate(ov.angle, ov.x, ov.y).transform(held.x, held.y);
            held.angle = ov.angle;
            held.x = newPoint.getX();
            held.y = newPoint.getY();
            
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
    

    public void moveShape(GraphicsContext gc, double x, double y) {
        if (heldPoint != null) {
            Point old = heldPoint;
	    old.erase(gc);
            redrawAround(gc, old, Color.GREEN);

            for (Point p : shapes) {
                if (p == old) continue;
		
                //TODO - only ovelap closest shape
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
	    old.draw(gc, Color.BLUE);
        }
        
    }

    public void rotateShape(GraphicsContext gc, Point point, double angle) {
        point.erase(gc);
        point.angle += angle;
        point.draw(gc, Color.GREEN);
    }
    

}

    