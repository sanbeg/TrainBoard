public class Length {
    public static double ppi = 24; //FIXME!!!
    //private static final double ppi = 96;
    
    public static enum Unit {
	IN(1.0), MM(25.4);
	
	public double perInch;
	
	Unit(double pi) {
	    perInch = pi;
	}
    }


    private final double inches;
    
    public Length(double length) {
	inches = length;
    }

    public Length(double length, Unit unit) {
	inches = length / unit.perInch;
    }

    public double getInches() {
	return inches;
    }
    public double getPixels() {
	return inches * ppi;
    }
    
}
