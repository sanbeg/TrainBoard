import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertNotNull;
import junit.framework.TestCase;

class TestShape implements Shape 
{
    public String getId() {
        return "test";
    }
    public double getWidth() {
        return 10;
    }
    public double getHeight() {
        return 100;
    }
    public Color color;
    public int nDraw = 0;
    public int nErase = 0;
    public GraphicsContext drawGc = null;
    public GraphicsContext eraseGc = null;
    
    public void draw(GraphicsContext gc, Color color) {
        ++nDraw;
        this.color = color;
	drawGc = gc;
    }
    
    public void erase(GraphicsContext gc){
        ++nErase;
	eraseGc = gc;
    }
    
}


public class BoardModelTest extends TestCase
{
    final Canvas canvas = new Canvas(250,250);
    final GraphicsContext gc = canvas.getGraphicsContext2D();
    final GraphicsContext fgc = new Canvas(250,250).getGraphicsContext2D();

    public void testAdd() {
        BoardModel bm = new BoardModel();
        TestShape ts = new TestShape();
        bm.addShape(gc, 100, 100, ts);
        assertNull("left is empty", bm.findPointAt(80,100));
        assertNull("right is empty", bm.findPointAt(120,100));
        assertEquals("found at center", ts, bm.findPointAt(100,100).shape);
        assertEquals("found above", ts, bm.findPointAt(100,120).shape);
        assertEquals("found below", ts, bm.findPointAt(100,80).shape);
        assertEquals("it's green", Color.GREEN, ts.color);
    }

    public void testLift() {
        BoardModel bm = new BoardModel();
        TestShape ts = new TestShape();
        bm.addShape(gc, 100, 100, ts);
        assertEquals("Called draw", 1, ts.nDraw);
        bm.liftShape(gc, 100, 100);
        assertEquals("Called draw again", 2, ts.nDraw);
        assertEquals("Used Color", ts.color, Color.BLUE);
        bm.releaseShape(gc);
        assertEquals("Called draw again", 3, ts.nDraw);
        assertEquals("Changed Color", ts.color, Color.GREEN);
    }

    public void testLiftToFloat() {
        BoardModel bm = new BoardModel();
	bm.setFloatingContext(fgc);
	
        TestShape ts = new TestShape();
        bm.addShape(gc, 100, 100, ts);
        assertEquals("Called draw", 1, ts.nDraw);
        bm.liftShape(gc, 100, 100);
        assertEquals("Called draw again", 2, ts.nDraw);
        assertEquals("Used Color", ts.color, Color.BLUE);
	assertEquals("Erased on fixed", ts.eraseGc, gc);
	assertEquals("Drew on float", ts.drawGc, fgc);
	
        bm.releaseShape(gc);
	assertEquals("Erased on float", ts.eraseGc, fgc);
        assertEquals("Called draw again", 3, ts.nDraw);
        assertEquals("Changed Color", ts.color, Color.GREEN);
	assertEquals("Drew on fixed", ts.drawGc, gc);
    }

    public void testMove() {
        BoardModel bm = new BoardModel();
        TestShape ts = new TestShape();
        bm.addShape(gc, 100, 100, ts);
        bm.liftShape(gc, 100, 100);
        assertEquals("called erase", 1, ts.nErase);

        bm.moveShape(gc, 100, 0);

        assertNull("old place is empty", bm.findPointAt(100,100));
        assertNull("left is empty", bm.findPointAt(180,100));
        assertNull("right is empty", bm.findPointAt(220,100));
        assertEquals("found at center", ts, bm.findPointAt(200, 100).shape);
        assertEquals("called erase", 2, ts.nErase);
    }

    public void testMoveOnFloat() {
        BoardModel bm = new BoardModel();
	bm.setFloatingContext(fgc);
	
        TestShape ts = new TestShape();
        bm.addShape(gc, 100, 100, ts);
        bm.liftShape(gc, 100, 100);
        assertEquals("called erase", 1, ts.nErase);

        bm.moveShape(gc, 100, 0);

        assertNull("old place is empty", bm.findPointAt(100,100));
        assertNull("left is empty", bm.findPointAt(180,100));
        assertNull("right is empty", bm.findPointAt(220,100));
        assertEquals("found at center", ts, bm.findPointAt(200, 100).shape);
        assertEquals("called erase", 2, ts.nErase);
	assertEquals("Erased on float", fgc, ts.eraseGc);
    }
    
    public void testRotate() {
        BoardModel bm = new BoardModel();
        TestShape ts = new TestShape();
        bm.addShape(gc, 100, 100, ts);
        assertEquals("got angle", 0, bm.findPointAt(100,100).angle, 0.01);
        bm.rotateShape(gc, bm.findPointAt(100,100), 90);
        assertEquals("got angle", 90, bm.findPointAt(100,100).angle, 0.01);

        assertNull("above is empty", bm.findPointAt(100,80));
        assertNull("below is empty", bm.findPointAt(100,120));
        assertEquals("found at center", ts, bm.findPointAt(100,100).shape);
        assertEquals("found left", ts, bm.findPointAt(80,100).shape);
        assertEquals("found right", ts, bm.findPointAt(120,100).shape);
    }

    public void testAddSnap() {
        BoardModel bm = new BoardModel();
        TestShape ts = new TestShape();
   
        bm.addShape(gc, 0, 0, ts);
        bm.addShape(gc, 5, 75, ts);
        assertEquals("X aligned", 0, bm.findPointAt(0,75).x, 0.001);
        assertEquals("Y aligned", 100, bm.findPointAt(0,75).y, 0.001);
    }

    public void testMoveSnap() {
        BoardModel bm = new BoardModel();
        TestShape ts = new TestShape();
   
        bm.addShape(gc, 0, 0, ts);
        bm.addShape(gc, 5, 200, ts);

        assertEquals("X is same", 5, bm.findPointAt(5,200).x, 0.001);
        assertEquals("Y is same", 200, bm.findPointAt(5,200).y, 0.001);

        bm.liftShape(gc, 5, 200);
        bm.moveShape(gc, 0, -101);
	bm.releaseShape(gc);
	
        assertEquals("X aligned", 0, bm.findPointAt(1,75).x, 0.001);
        assertEquals("Y aligned", 100, bm.findPointAt(1,75).y, 0.001);
    }

    public void testRotatedSnap() 
    {
        BoardModel bm = new BoardModel();
        TestShape ts = new TestShape();
        bm.addShape(gc, 100, 100, ts);
	Point p1 = bm.findPointAt(100,100);
        bm.rotateShape(gc, p1, 90);
	// x = 50..150, y=95..105)

	assertNull(bm.findPointAt(151,100));
	assertNotNull(bm.findPointAt(149, 100));
	assertNull(bm.findPointAt(151, 100));

	bm.addShape(gc, 154, 102, ts);
	Point p2 = bm.findPointAt(155,100);
	assertEquals("Angle matches", p1.angle, p2.angle, 0.01);
        assertEquals("Y aligned", p1.y, p2.y, 0.001);
	assertEquals("X is snapped", p1.x+100, p2.x, 0.001);
    }
    
}

