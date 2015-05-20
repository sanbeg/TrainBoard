import java.util.List;
import java.util.Optional;
import javafx.scene.control.TreeItem;

public class ShapeBox {
    private final List <Shape> shapes = new java.util.ArrayList<>();
    
    {
	TrackScale nscale = TrackScale.N;
	shapes.add(new Track.Straight("straight", nscale, new Length(2.5)));
	shapes.add(new Track.Straight("straight5", nscale, new Length(5)));

	shapes.add(new Track.Curve("curve", nscale, new Length(9.5), 30.0));
	shapes.add(new Track.Curve("half-curve", nscale, new Length(9.5), 15.0));
	shapes.add(new Track.Curve("curve-975", nscale, new Length(9.75), 30.0));
	shapes.add(new Track.Curve("curve-half-975", nscale, new Length(9.75), 15.0));

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
        //R1 = 194.6, R2=228.2, R3=492.6, R4=526.2
 
        shapes.add(new Track.Turnout("trix-r1-24-rh",
                                     nscale,
                                     Track.Turnout.Hand.RIGHT,
                                     new Length(104.2, Length.Unit.MM),
                                     new Length(194.6, Length.Unit.MM),
                                     24.0));
        shapes.add(new Track.Curve("trix-r1-6", nscale, new Length(194.6, Length.Unit.MM), 6.0));

        shapes.add(new Track.Straight("ho-straight-9", TrackScale.HO, new Length(9)));
        shapes.add(new Track.Curve("ho-curve-18", TrackScale.HO, new Length(18), 30.0));
        
	shapes.add(new Shape.MidDot("middot", 30, 30, 4));
	shapes.add(new Shape.SolidSquare("solid", 30, 30));
	shapes.add(new Shape.MidDot("tall", 30, 60, 6));


    }

    public Iterable<Shape> getShapes() {
	return shapes;
    }
    
    public class TreeTrack {
        public final Optional<Shape> shape;
        public final String string;
        
        public TreeTrack(Shape shape) {
            this.shape = Optional.of(shape);
            this.string = shape.getId();
        }
        public TreeTrack(String string) {
            this.shape = Optional.empty();
            this.string = string;
        }
        public String toString() {
            return string;
        }
    }
                

    public TreeItem<TreeTrack> getTree() {
        TreeItem<TreeTrack> root   = new TreeItem<>(new TreeTrack("tracks"));
        TreeItem<TreeTrack> rootN  = new TreeItem<>(new TreeTrack("N"));
        TreeItem<TreeTrack> rootHO = new TreeItem<>(new TreeTrack("HO"));

        root.getChildren().add(rootN);
        root.getChildren().add(rootHO);
        root.setExpanded(true);
        
        for (Shape shape : shapes) {
            if (shape instanceof Track) {
                switch(((Track)shape).scale) {
                  case N:
                    rootN.getChildren().add(new TreeItem<>(new TreeTrack(shape)));
                    break;
                  case HO:
                    rootHO.getChildren().add(new TreeItem<>(new TreeTrack(shape)));
                    break;
                }
            }
        }

        return root;
    }
    
}
