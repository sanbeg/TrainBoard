import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

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
    
    public void draw(GraphicsContext gc, Color color) {
        ++nDraw;
        this.color = color;
    }
    
    public void erase(GraphicsContext gc){
        ++nErase;
    }
    
}


public class BoardModelTest 
{
    final Canvas canvas = new Canvas(250,250);
    final GraphicsContext gc = canvas.getGraphicsContext2D();

    @Test
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

    @Test
    public void testLift() {
        BoardModel bm = new BoardModel();
        TestShape ts = new TestShape();
        bm.addShape(gc, 100, 100, ts);
        assertEquals("Called draw", 1, ts.nDraw);
        bm.liftShape(gc, 100, 100, Color.BLUE);
        assertEquals("Called draw again", 2, ts.nDraw);
        assertEquals("Used Color", ts.color, Color.BLUE);
        bm.releaseShape(gc);
        assertEquals("Called draw again", 3, ts.nDraw);
        assertEquals("Changed Color", ts.color, Color.GREEN);
    }

    @Test
    public void testMove() {
        BoardModel bm = new BoardModel();
        TestShape ts = new TestShape();
        bm.addShape(gc, 100, 100, ts);
        bm.liftShape(gc, 100, 100, Color.BLUE);
        bm.moveShape(gc, 100, 0, Color.GREEN);

        assertNull("old place is empty", bm.findPointAt(100,100));
        assertNull("left is empty", bm.findPointAt(180,100));
        assertNull("right is empty", bm.findPointAt(220,100));
        assertEquals("found at center", ts, bm.findPointAt(200, 100).shape);
        assertEquals("called erase", 1, ts.nErase);
    }
    
    @Test
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
    
}

