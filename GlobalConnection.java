public class GlobalConnection {
    public double x;
    public double y;
    public Shape.LocalConnection connection;

    public GlobalConnection(Shape.LocalConnection lc, double x, double y)
    {
	set(lc, x, y);
	
    }

    public void set(Shape.LocalConnection lc, double x, double y) 
    {
	    connection = lc;
	    this.x = x + lc.x;
	    this.y = y + lc.y;
    }
    public void moveTo(double x, double y) 
    {
	    this.x = x + connection.x;
	    this.y = y + connection.y;
    }
	
}
    
