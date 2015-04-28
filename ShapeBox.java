import java.util.List;

public class ShapeBox {
    private final List <Shape> shapes = new java.util.ArrayList<>();
    
    {
	TrackScale nscale = TrackScale.N;
	shapes.add(new Track.Straight("straight", nscale, new Length(2.5)));
	shapes.add(new Track.Straight("straight5", nscale, new Length(5)));

	shapes.add(new Track.Curve("curve", nscale, new Length(9.5), 30.0));
	shapes.add(new Track.Curve("half-curve", nscale, new Length(9.5), 15.0));
	shapes.add(new Track.Curve("curve-11", nscale, new Length(11), 30.0));
	shapes.add(new Track.Curve("curve-19", nscale, new Length(19), 15.0));

	shapes.add(new Track.Cross("x90", nscale, new Length(2.0), 90));
	shapes.add(new Track.Cross("x45", nscale, new Length(2.0), 45));
	shapes.add(new Track.Cross("x15", nscale, new Length(5.0), 15));
	shapes.add(new Track.Road("road", nscale, new Length(1.0), new Length(2.0)));

	shapes.add(new Track.Turnout("right",
				     nscale,
				     Track.Turnout.Hand.RIGHT,
				     new Length(5.0),
				     new Length(19),
				     15.0));
	shapes.add(new Track.Turnout("left",
				     nscale, 
				     Track.Turnout.Hand.LEFT,
				     new Length(5.0),
				     new Length(19),
				     15.0));

	shapes.add(new Track.Turnout("turn",
				     nscale, 
				     Track.Turnout.Hand.ALL, 
				     new Length(5.0), 
				     new Length(19), 
				     15.0));
	    
	shapes.add(new Shape.MidDot("middot", 30, 30, 4));
	shapes.add(new Shape.SolidSquare("solid", 30, 30));
	shapes.add(new Shape.MidDot("tall", 30, 60, 6));


    }

    public Iterable<Shape> getShapes() {
	return shapes;
    }
    
}
