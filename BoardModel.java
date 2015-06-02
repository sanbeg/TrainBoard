import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;
import javafx.scene.transform.Rotate;

import java.util.List;
import java.util.Set;

public class BoardModel 
{
    public final List<Point> shapes = new java.util.ArrayList<>();
    private GraphicsContext floatingContext=null;
    private boolean dirty = false;
    
    private static final Color POINT_COLOR_NORMAL = Color.GREEN;
    private static final Color POINT_COLOR_HELD = Color.BLUE;
    private static final Color POINT_COLOR_CLIP = Color.RED;
    private static final Color POINT_COLOR_OBSCURE = Color.YELLOW;

    private boolean showInactiveJoiners = true;
    
    public Color pointColorNormal() {
	return showInactiveJoiners?
	    POINT_COLOR_NORMAL:
	    Color.TRANSPARENT;
    }
    
    public void showInactiveJoiners(GraphicsContext gc, boolean val) {
	showInactiveJoiners = val;
	redraw(gc);
    }
    
    public void colorCodeCurves(GraphicsContext gc, boolean val) {
	boolean old = Track.colorCodeCurves;
	Track.colorCodeCurves = val;
	if (old != val) {
	    redraw(gc);
	}
    }

    public void drawTies(GraphicsContext gc, boolean val) {
	boolean old = Track.drawTies;
	Track.drawTies = val;
	if (old != val) {
	    redraw(gc);
	}
    }
    

    public void setFloatingContext(GraphicsContext gc) {
	assert floatingContext == null;
	floatingContext = java.util.Objects.requireNonNull(gc);
    }
    
    private GraphicsContext getfgc(GraphicsContext fallback) {
	return (floatingContext==null) ? fallback : floatingContext;
    }

    private GraphicsContext getGc(Point point, GraphicsContext fallback) {
        if (floatingContext==null) return fallback;
        else if (heldPoints.contains(point)) return floatingContext;
        else return fallback;
    }

    public boolean isDirty() {
        return dirty;
    }
    
    public void makeClean() {
        dirty = false;
    }

    public void addPoint(Point p) 
    {
	shapes.add(p);
    }
    
    public void redraw(GraphicsContext gc) 
    {
        for (Point p : shapes) {
	    p.draw(getGc(p, gc), pointColorNormal());
        }
    }
    
                
    public void addShape(GraphicsContext gc, double x, double y, Shape shape) {
	Point p = new Point(x,y, shape);
	snapShape(gc, p);
	p.draw(gc, pointColorNormal());
	shapes.add(p);
        dirty = true;
    }
    
    public void eraseShape(GraphicsContext gc, Point old) 
    {
	old.erase(gc);
	shapes.remove(old);
        dirty = true;
        
	for (Point p : shapes) {
	    p.draw(getGc(p, gc), pointColorNormal());
	}
    }

    private final Set<Point> heldPoints = new java.util.HashSet<>();
    
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
            old.erase(gc);
            redrawAround(gc, old, pointColorNormal());
            old.draw(getfgc(gc), POINT_COLOR_HELD);
            heldPoints.add(old);
        }
    }
    
    private void snapShape(GraphicsContext gc, Point held) {
        /*
         * If moved to overlap, push out of the way.
         * should handle multiple overlap, avoid putting 2 in same place.
         */

        Point ov = null;
        double minDist = Double.MAX_VALUE;
        double minCpDist = Double.MAX_VALUE;
        GlobalConnection heldCp = null;
        GlobalConnection nearCp = null;
	
	if (held.floating) return;

        for (Point p : shapes) {
	    if (p.floating) continue;
	    
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
			    
			    if (dist < minCpDist 
                                //check connection size only on move?
                                && dist < p.shape.connectionSize()) {
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
		    p.draw(gc, pointColorNormal());
		}
            }
        }

	//need to figure out which layer it was on
	GraphicsContext heldGc = heldPoints.contains(held) ?
	    getfgc(gc) : gc;
	
	if (heldCp != null) {
	    held.erase(heldGc);
            ov.draw(gc, pointColorNormal());
            ov.obscured = false;

	    held.x += nearCp.x - heldCp.x;
	    held.y += nearCp.y - heldCp.y;
	    
            double angle = ov.angle + nearCp.connection.angle - heldCp.connection.angle + 180;
            Point2D p2d = new Rotate(angle - held.angle, nearCp.x, nearCp.y)
                .transform(held.x,held.y);
            
            held.angle = angle % 360;
            held.x = p2d.getX();
            held.y = p2d.getY();
            redrawAround(gc, held, pointColorNormal());
	}
        else

	if (ov != null) {
            held.erase(heldGc);
            ov.draw(gc, pointColorNormal());
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

	    newPoint = new Rotate(ov.angle, ov.x, ov.y)
		.transform(held.x, held.y);
            held.angle = ov.angle;
            held.x = newPoint.getX();
            held.y = newPoint.getY();
        }
    }
    

    public void releaseShape(GraphicsContext gc) 
    {
        for (Point old : heldPoints) {
            snapShape(gc, old); //how to snap multiple shapes?
	    if (floatingContext != null) {
		old.erase(floatingContext);
	    }

            old.draw(gc, pointColorNormal());
        }
        heldPoints.clear();
    }
    
    private void redrawAround(GraphicsContext gc, Point point, Color color) {
        for (Point p : shapes) {
            if (p == point) continue;
            //TODO - test correct redraws -w- multi-select,
            //maybe redrawaround on floating context, too.
            if (heldPoints.contains(p)) continue;

            if (point.obscures(p)) {
                p.draw(gc, color);
            }
        }
    }
    

    public void moveShape(GraphicsContext gc, double x, double y) {
        for (Point old : heldPoints) {
	    if (floatingContext == null) {
		old.erase(gc);
		redrawAround(gc, old, pointColorNormal());
	    } else {
		old.erase(floatingContext);
	    }

            for (Point p : shapes) {
                if (heldPoints.contains(p)) continue;
		
                //TODO - only ovelap closest shape
		if (old.floating && p.obscures(old)) {
		    p.obscured = true;
		}
                else if (p.overlaps(old)) {
                    p.draw(gc, POINT_COLOR_CLIP);
                    p.obscured = true;
                }
                else if (p.obscures(old)) {
                    p.draw(gc, POINT_COLOR_OBSCURE);
                    p.obscured = true;
                }
                else if (p.obscured) {
		    //redraw to reset indicator color
		    //can leave traces in round corners or rotated edges
		    p.erase(gc);
                    redrawAround(gc, p, pointColorNormal());
                    p.draw(gc, pointColorNormal());
                    p.obscured = false;
                    //System.out.printf("Redraw %.1f,%.1f\n", p.x, p.y);
                }
            }
        }
        for (Point old : heldPoints) {
            old.x += x;
            old.y += y;
	    old.draw(getfgc(gc), POINT_COLOR_HELD);
            dirty = true;
        }
        
    }

    public void rotateShape(GraphicsContext gc, Point point, double angle) {
        //should support multi-rotate?
        GraphicsContext pgc = getGc(point, gc);
        
        point.erase(pgc);
        point.angle += angle;
        point.draw(pgc, pointColorNormal());
        dirty = true;
    }
    
    //fixme - erase/redraw each point on right gc.
    public void goLeft(GraphicsContext gc) {
        double left = Double.MAX_VALUE;
        for (Point p : shapes) {
            left = Math.min(left, p.x - p.getWidth()/2);
        }
        for (Point p : shapes) {
            p.erase(getGc(p, gc));
            p.x -= left;
        }
        redraw(gc);
        dirty = true;
    }

    public void goRight(GraphicsContext gc, double bound) {
        double right = Double.MIN_VALUE;
        for (Point p : shapes) {
            right = Math.max(right, p.x + p.getWidth()/2);
        }
        for (Point p : shapes) {
            p.erase(getGc(p, gc));
            p.x += (bound - right);
        }
        redraw(gc);
        dirty = true;
    }

    public void goUp(GraphicsContext gc) {
        double top = Double.MAX_VALUE;
        for (Point p : shapes) {
            top = Math.min(top, p.y - p.getHeight()/2);
        }
        for (Point p : shapes) {
            p.erase(getGc(p, gc));
            p.y -= top;
        }
        redraw(gc);
        dirty = true;
    }

    public void goDown(GraphicsContext gc, double bound) {
        double bottom = Double.MIN_VALUE;
        for (Point p : shapes) {
            bottom = Math.max(bottom, p.y + p.getHeight()/2);
        }
        for (Point p : shapes) {
            p.erase(getGc(p, gc));
            p.y += (bound - bottom);
        }
        redraw(gc);
        dirty = true;
    }

    public void goCenter(GraphicsContext gc, double xbound, double ybound) {
        double left = Double.MAX_VALUE;
        double right = Double.MIN_VALUE;
        double top = Double.MAX_VALUE;
        double bottom = Double.MIN_VALUE;

        for (Point p : shapes) {
            left   = Math.min(left,   p.x - p.getWidth()/2);
            right  = Math.max(right,  p.x + p.getWidth()/2);
            top    = Math.min(top,    p.y - p.getHeight()/2);
            bottom = Math.max(bottom, p.y + p.getHeight()/2);
        }

        double xdelta = (left + right - xbound)  / 2;
        double ydelta = (top + bottom - ybound) / 2;
        

        for (Point p : shapes) {
            p.erase(getGc(p, gc));
            p.x -= xdelta;
            p.y -= ydelta;
        }
        redraw(gc);
        dirty = true;
    }
        
}

    
