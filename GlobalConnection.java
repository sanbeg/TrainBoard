public class GlobalConnection {
    public double x;
    public double y;
    public final Shape.LocalConnection connection;
    public GlobalConnection peer = null;

    public GlobalConnection (Shape.LocalConnection lc,
                             double x,
                             double y
        )
	{
	    connection = lc;
	    this.x = x;
	    this.y = y;
	}
    double getX(Point p) 
	{
	    return p.x + connection.x;
	}
    double getY(Point p)
	{
	    return p.y + connection.y;
	}
	

}
    
