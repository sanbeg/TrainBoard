import javafx.geometry.Point2D;
import javafx.scene.transform.Rotate;

public class GlobalConnection {
    public double x;
    public double y;
    public Track.LocalConnection connection;

    public GlobalConnection(Track.LocalConnection lc, double x, double y)
    {
	set(lc, x, y);
	
    }
    private  void set(Track.LocalConnection lc, double x, double y) 
    {
	    connection = lc;
	    this.x = x + lc.x;
	    this.y = y + lc.y;
    }
    public void moveTo(double x, double y, double angle) 
    {
        Point2D p2d = new Rotate(angle).transform(connection.x, connection.y);
        this.x = x + p2d.getX();
        this.y = y + p2d.getY();
    }
	
}
    
