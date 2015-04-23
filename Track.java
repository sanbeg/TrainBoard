import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Affine;
import javafx.geometry.Point2D;
import javafx.scene.transform.Rotate;

abstract public class Track extends Shape {
    public final TrackScale scale;
    protected final double gauge;
	
    protected final LocalConnection[] connections;

    protected static final Color BALLAST_COLOR = Color.IVORY;
    protected static final Color RAIL_COLOR    = Color.SILVER.darker();
    protected static final Color TIE_COLOR     = Color.BLACK;
        
    public Track(String id, double w, double h, TrackScale ts, int connections) {
        super(id, w, h);
        this.scale = ts;
        this.gauge = ts.railGauge();
        this.connections = new LocalConnection[connections];
    }

    @Override public boolean hasConnections()           { return true;        }
    @Override public LocalConnection[] getConnections() { return connections; }
    @Override public double connectionSize()            { return gauge * 2;   }

    protected void drawIndicators(GraphicsContext gc, Color color) {
        gc.setFill(color.interpolate(Color.TRANSPARENT, 0.6));
        for (LocalConnection c : connections) {
            gc.fillArc(c.x-gauge/2, c.y-gauge/2, gauge, gauge, 180-c.angle, 180, ArcType.CHORD);
        }
    }

    public static class LocalConnection {
	public final double x;
	public final double y;
	public final double angle;

	public LocalConnection(double x, double y, double angle) 
	{
	    this.x = x;
	    this.y = y;
	    this.angle = angle;
	}
	
    }

    public static class Straight extends Track
    {
        public Straight(String id, TrackScale scale, Length length) {
            super(id, scale.ballastWidth(), length.getPixels(), scale, 2);
	    double h = length.getPixels();
            
            connections[0] = new LocalConnection(0, -h/2, 0);
            connections[1] = new LocalConnection(0, +h/2, 180);
        }
        
        public void draw(GraphicsContext gc, Color color) {
            //ballast
	    gc.setFill(BALLAST_COLOR);
	    gc.fillRect(-getWidth()/2, -getHeight()/2, getWidth(), getHeight());

            //ties
            gc.setStroke(TIE_COLOR);
            gc.setLineCap(StrokeLineCap.BUTT);
            gc.setLineWidth(scale.tieWidth());
            double tieX = scale.tieLength()/2.0;
		
            for (int i=0; i<10; ++i) {
                double h = getHeight();
                double y = -h/2 + h*0.1*i + h*0.05;
                gc.strokeLine(-tieX, y, tieX, y);
            }

            //rails
            gc.setStroke(RAIL_COLOR);
            gc.setLineWidth(scale.railWidth());
                
            double g2 = gauge/2;
            gc.strokeLine(-g2, -getHeight()/2, -g2, getHeight()/2);
            gc.strokeLine(+g2, -getHeight()/2, +g2, getHeight()/2);

            drawIndicators(gc, color);
        }
        
    }

    public static class Road extends Track
    {
        private final double roadWidth;
        
        public Road(String id, TrackScale scale, Length w, Length l) {
            super(id, l.getPixels(), l.getPixels(), scale, 2);
            roadWidth = w.getPixels();

            double h = l.getPixels();
            connections[0] = new LocalConnection(0, -h/2, 0);
            connections[1] = new LocalConnection(0, +h/2, 180);
        }
        
        public void draw(GraphicsContext gc, Color color) 
            {
                //ballast
                gc.setFill(BALLAST_COLOR);
                gc.fillRect(-scale.ballastWidth()/2, -getHeight()/2, scale.ballastWidth(), getHeight());

                double tieX = scale.tieLength()/2;
                double roadX = scale.ballastWidth()/2;
                
                gc.setLineCap(StrokeLineCap.BUTT);
            
                //road
                gc.setFill(Color.BLACK);
                gc.fillRoundRect(-getHeight()/2, -roadWidth/2, getHeight(), roadWidth, 10, 10);
                gc.setStroke(Color.YELLOW);
                gc.setLineWidth(1.0);
                gc.strokeLine(-getHeight()/2, 0, -roadX, 0);
                gc.strokeLine(+roadX, 0, getHeight()/2, 0);
                
                //ties
                gc.setStroke(TIE_COLOR);
                gc.setLineWidth(scale.tieWidth());
                
                for (int i=0; i<10; ++i) {
                    double h = getHeight();
                    double y = -h/2 + h*0.1*i + h*0.05;
                    gc.strokeLine(-tieX, y, tieX, y);
                }

                //rails
                gc.setStroke(RAIL_COLOR);
                gc.setLineWidth(scale.railWidth());
                
                //double gauge = w*0.4;
                double gauge = scale.railGauge()/2;
                
                gc.strokeLine(-gauge, -getHeight()/2, -gauge, getHeight()/2);
                gc.strokeLine(+gauge, -getHeight()/2, +gauge, getHeight()/2);

                drawIndicators(gc, color);
            }
        
    }

 
    public static class Cross extends Track {
        private final double trackWidth;
        private final double angle;
        
        public Cross(String id, TrackScale ts, Length length, double angle) {
            super(id, length.getPixels(), length.getPixels(), ts, 4);
            this.trackWidth = ts.ballastWidth();
            this.angle = angle;

            double h = length.getPixels();
            Point2D p1 = new Rotate(angle).transform(0, -h/2);
            Point2D p2 = new Rotate(180+angle).transform(0, -h/2);
	    connections[0] = new LocalConnection(0, -h/2, 0);
	    connections[1] = new LocalConnection(p1.getX(), p1.getY(), angle);
	    connections[2] = new LocalConnection(0, +h/2, 180);
	    connections[3] = new LocalConnection(p2.getX(), p2.getY(), 180+angle);
        }

        public void draw(GraphicsContext gc, Color color) {
            Affine vert = gc.getTransform();
            Affine horiz = new Affine(vert);
            horiz.appendRotation(angle);
            
            //ballast
	    gc.setFill(BALLAST_COLOR);
	    gc.fillRect(-trackWidth/2, -getHeight()/2, trackWidth, getHeight());

            gc.setTransform(horiz);
	    gc.fillRect(-trackWidth/2, -getHeight()/2, trackWidth, getHeight());
            
            // center
            double tieX = trackWidth*0.45;
            double arc2 = 4;
            gc.setFill(Color.BLACK);
            gc.setTransform(vert);

            gc.fillRoundRect(-tieX, -tieX*2, tieX*2, tieX*4, arc2, arc2);
            gc.setTransform(horiz);
            gc.fillRoundRect(-tieX, -tieX*2, tieX*2, tieX*4, arc2, arc2);
            
            // ties - TODO

            //rails
            gc.setStroke(RAIL_COLOR);
            gc.setLineWidth(scale.railWidth());
                
            //double gauge = trackWidth*0.4;
            double gauge = scale.railGauge()/2;
            
            gc.setTransform(vert);
            gc.strokeLine(-gauge, -getHeight()/2, -gauge, getHeight()/2);
            gc.strokeLine(+gauge, -getHeight()/2, +gauge, getHeight()/2);

            gc.setStroke(RAIL_COLOR);
            gc.setTransform(horiz);
            gc.strokeLine(-gauge, -getHeight()/2, -gauge, getHeight()/2);
            gc.strokeLine(+gauge, -getHeight()/2, +gauge, getHeight()/2);

            gc.setStroke(TIE_COLOR);
            gc.setLineCap(StrokeLineCap.BUTT);
            double rw = scale.railWidth() / 1.5;
            gc.setLineWidth(scale.railWidth() / 2);
            
            gc.strokeLine(-gauge+rw, -tieX*2, -gauge+rw, +tieX*2);
            gc.strokeLine(+gauge-rw, -tieX*2, +gauge-rw, +tieX*2);

            gc.setTransform(vert);

            gc.strokeLine(-gauge+rw, -tieX*2, -gauge+rw, +tieX*2);
            gc.strokeLine(+gauge-rw, -tieX*2, +gauge-rw, +tieX*2);


            drawIndicators(gc, color);
            /*
            gc.setFill(color.interpolate(Color.TRANSPARENT, 0.6));
            gc.setTransform(vert);
            gc.fillOval(-gauge, -getHeight()/2, 2*gauge, 2*gauge);
            gc.fillOval(-gauge, +getHeight()/2-2*gauge, 2*gauge, 2*gauge);
            gc.setTransform(horiz);
            gc.fillOval(-gauge, -getHeight()/2, 2*gauge, 2*gauge);
            gc.fillOval(-gauge, +getHeight()/2-2*gauge, 2*gauge, 2*gauge);
            */
        }
        
    }

    public static class Curve extends Track {
	private final double radius;
	private final double angle;

	private static double mkWidth(TrackScale scale, double d, double ad) {
            double ar = Math.toRadians(ad);
            double bow = d * Math.cos(ar/2);
	    double lw = scale.ballastWidth();
            return d - bow + lw;
	}	
	private static double mkHeight(TrackScale scale, double d, double ad) {
            double ar = Math.toRadians(ad);
	    double lw = scale.ballastWidth();
            return (d+lw) * Math.sin(ar/2);
	}

        public Curve(String id, TrackScale scale, Length radius, double angle) {
	    super(id, 
                    mkWidth(scale, radius.getPixels()*2, angle), 
                    mkHeight(scale, radius.getPixels()*2, angle),
                    scale,
                    2
		  );

	    this.radius = radius.getPixels();
	    this.angle = angle;
	    double r = radius.getPixels();
	    
            Point2D p1 = new Rotate(+angle/2, r, 0).transform(0,0);
            Point2D p2 = new Rotate(-angle/2, r, 0).transform(0,0);
	    connections[0] = new LocalConnection(p1.getX(), p1.getY(), angle/2);
	    connections[1] = new LocalConnection(p2.getX(), p2.getY(), 180-angle/2);
	}

        public void draw(GraphicsContext gc, Color color) {
            //ballast
	    gc.setStroke(BALLAST_COLOR);
	    double lw = scale.ballastWidth();
            gc.setLineWidth(lw);
            gc.setLineCap(StrokeLineCap.BUTT);
	    
	    double x=0, y=0, r=radius, ad=angle;
            double d = 2 * r;
            
            gc.strokeArc(x, y-r, d, d, 180-ad/2, ad, ArcType.OPEN);

	    double gauge = scale.railGauge()/2;
	    gc.setLineWidth(scale.railWidth());
	    gc.setStroke(RAIL_COLOR);
	    d = 2*(r+gauge);
	    gc.strokeArc(-gauge, -(r+gauge), d, d, 180-ad/2, ad, ArcType.OPEN); //left
	    d = 2*(r-gauge);
	    gc.strokeArc(+gauge, -(r-gauge), d, d, 180-ad/2, ad, ArcType.OPEN); //right

            drawIndicators(gc, color);
	}
	
    }

    public static class Turnout extends Track {
        public static enum Hand {
            LEFT, RIGHT, ALL;

            public boolean right() 
            {
                return this==RIGHT || this==ALL;
            }
            public boolean left() 
            {
                return this==LEFT || this==ALL;
            }
            
        }

        private final double length;
	private final double radius;
	private final double angle;
        private final double yoff;
        private final Hand hand;
        
        
	private static double mkWidth(TrackScale scale, double d, double ad) {
            double r = d/2;
	    double lw = scale.ballastWidth();
            double ar = Math.toRadians(ad);
            
            return (r - (r-lw/2)*Math.cos(ar)) * 2;
	}	
	private static double mkHeight(TrackScale scale, double d, double ad) {
            double ar = Math.toRadians(ad);
	    double lw = scale.ballastWidth();
            return (d+lw) * Math.sin(ar/2);
	}

        public Turnout(String id, TrackScale scale, Hand hand, Length length, Length radius, double angle) {
	    super(id, 
                  mkWidth(scale, radius.getPixels()*2, angle), 
                  mkHeight(scale, radius.getPixels()*2, angle),
                  scale,
                  (hand == Hand.ALL) ? 4 : 3
		  );
            this.length = length.getPixels();
	    this.radius = radius.getPixels();
	    this.angle = angle;
            this.hand = hand;
            
            double ar = Math.toRadians(angle);
            double h = getHeight();
            double coff = this.radius * Math.sin(ar)/2;
            yoff = Math.max(coff, h/2);

            int nc=0;
            
            connections[nc++] = new LocalConnection(0, yoff, 180);
            connections[nc++] = new LocalConnection(0, yoff-h, 0);
            double r = this.radius;
            
            if (hand.right()) connections[nc++] = new LocalConnection(r-r*Math.cos(ar), yoff-r*Math.sin(ar), this.angle);
            if (hand.left()) connections[nc++] = new LocalConnection(-(r-r*Math.cos(ar)), yoff-r*Math.sin(ar), -this.angle);
	}

 
        public void draw(GraphicsContext gc, Color color) {
            //ballast
	    gc.setStroke(BALLAST_COLOR);
	    double lw = scale.ballastWidth();
            gc.setLineWidth(lw);
            gc.setLineCap(StrokeLineCap.BUTT);

            double h = getHeight();

            double d = radius * 2;
            if (hand.right()) gc.strokeArc(0,         -radius + yoff, d, d, 180, -angle, ArcType.OPEN);
            if (hand.left()) gc.strokeArc(-2*radius, -radius + yoff, d, d,   0, +angle, ArcType.OPEN);
            gc.strokeLine(0, yoff, 0, yoff - h);

            //rails
            gc.setStroke(RAIL_COLOR);
            gc.setLineWidth(scale.railWidth());
                
            //straight rails
            double g2 = gauge/2;
            gc.strokeLine(-g2, -getHeight()/2, -g2, getHeight()/2);
            gc.strokeLine(+g2, -getHeight()/2, +g2, getHeight()/2);

            double r = radius;
            if (hand.right()) {
                //RH rails
                d = 2*(r+g2);
                gc.strokeArc(-g2, -d/2 + yoff, d, d, 180, -angle, ArcType.OPEN);  //RH
                d = 2*(r-g2);
                gc.strokeArc(+g2, -d/2 + yoff, d, d, 180, -angle, ArcType.OPEN);  //RH
            }
            if (hand.left()) {
                //LH rails
                d = 2*(r+g2);
                gc.strokeArc(+g2-d, -d/2 + yoff, d, d, 0, +angle, ArcType.OPEN);  //RH
                d = 2*(r-g2);
                gc.strokeArc(-g2-d, -d/2 + yoff, d, d, 0, +angle, ArcType.OPEN);  //RH
            }

            // rail gap
            double gap = scale.railWidth() / 1.5;
            gc.setStroke(BALLAST_COLOR);
            gc.setLineWidth(scale.railWidth() / 2);
            gc.strokeLine(-g2+gap, -getHeight()/2, -g2+gap, getHeight()/2);
            gc.strokeLine(+g2-gap, -getHeight()/2, +g2-gap, getHeight()/2);
            
            if (hand.right()) {
                d = 2*(r+g2);
                gc.strokeArc(-g2+gap, -d/2 + yoff, d, d, 180, -angle, ArcType.OPEN);  //RH
                d = 2*(r-g2);
                gc.strokeArc(+g2-gap, -d/2 + yoff, d, d, 180, -angle, ArcType.OPEN);  //RH
            }
            if (hand.left()) {
                //LH rails
                d = 2*(r+g2);
                gc.strokeArc(+g2-d-gap, -d/2 + yoff, d, d, 0, +angle, ArcType.OPEN);  //RH
                d = 2*(r-g2);
                gc.strokeArc(-g2-d+gap, -d/2 + yoff, d, d, 0, +angle, ArcType.OPEN);  //RH
            }
            

            drawIndicators(gc, color);
        }
    }
    
}
    
