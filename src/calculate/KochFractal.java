/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calculate;

import java.util.Observable;
import javafx.scene.paint.Color;

/**
 *
 * @author Peter Boots
 */
public class KochFractal extends Observable {

    private int level = 1;      // The current level of the fractal
    private int nrOfEdges = 3;  // The number of edges in the current level of the fractal
    private float hue;          // Hue value of color for next edge
    private boolean cancelled;  // Flag to indicate that calculation has been cancelled 

    private void drawKochEdge(double ax, double ay, double bx, double by, int n) throws InterruptedException {
        if(Thread.interrupted())
            throw new InterruptedException("Interrupting during the drawKochEdge (execution)...");
        
        if (!cancelled) {
            if (n == 1) {
                setHue(getHue() + 1.0f / nrOfEdges);
                Edge e = new Edge(ax, ay, bx, by, Color.hsb(getHue()*360.0, 1.0, 1.0));     
                this.setChanged();
                this.notifyObservers(e);            
            } else {
                double angle = Math.PI / 3.0 + Math.atan2(by - ay, bx - ax);
                double distabdiv3 = Math.sqrt((bx - ax) * (bx - ax) + (by - ay) * (by - ay)) / 3;
                double cx = Math.cos(angle) * distabdiv3 + (bx - ax) / 3 + ax;
                double cy = Math.sin(angle) * distabdiv3 + (by - ay) / 3 + ay;
                final double midabx = (bx - ax) / 3 + ax;
                final double midaby = (by - ay) / 3 + ay;
                drawKochEdge(ax, ay, midabx, midaby, n - 1);
                drawKochEdge(midabx, midaby, cx, cy, n - 1);
                drawKochEdge(cx, cy, (midabx + bx) / 2, (midaby + by) / 2, n - 1);
                drawKochEdge((midabx + bx) / 2, (midaby + by) / 2, bx, by, n - 1);
            }
        }
    }

    public void generateLeftEdge() throws InterruptedException {
        synchronized(this) {
            setHue(0f);
            cancelled = false;
            drawKochEdge(0.5, 0.0, (1 - Math.sqrt(3.0) / 2.0) / 2, 0.75, level);
        }
    }

    public void generateBottomEdge() throws InterruptedException {
        synchronized(this) {
            setHue(1f / 3f);
            cancelled = false;
            drawKochEdge((1 - Math.sqrt(3.0) / 2.0) / 2, 0.75, (1 + Math.sqrt(3.0) / 2.0) / 2, 0.75, level);
        }
    }

    public void generateRightEdge() throws InterruptedException {
        synchronized(this) {
            setHue(2f / 3f);
            cancelled = false;
            drawKochEdge((1 + Math.sqrt(3.0) / 2.0) / 2, 0.75, 0.5, 0.0, level);
        }
    }
    
    public void cancel() {
        cancelled = true;
    }

    public void setLevel(int lvl) {
        level = lvl;
        nrOfEdges = (int) (3 * Math.pow(4, level - 1));
    }

    public int getLevel() {
        return level;
    }

    public int getNrOfEdges() {
        return nrOfEdges;
    }
    
    private void setHue(float hue) {
        this.hue = hue;
    }
    
    private synchronized float getHue() {
        return this.hue;
    }
}
