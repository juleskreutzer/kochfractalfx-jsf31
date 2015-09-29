/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calculate;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import jsf31kochfractalfx.JSF31KochFractalFX;
import timeutil.TimeStamp;

/**
 *
 * @author Kevto
 */
public class KochManager implements Observer{
    private JSF31KochFractalFX application;
    private KochFractal koch;
    private List<Edge> edges;
    
    public KochManager(JSF31KochFractalFX application) {
        this.application = application;
        this.edges = new ArrayList<Edge>();
        this.koch = new KochFractal();
        this.koch.addObserver(this);
    }
    
    public void drawEdges() {
        application.clearKochPanel();
        
        TimeStamp t = new TimeStamp();
        t.setBegin();
        
        for(Edge e : this.edges)
            application.drawEdge(e); 
        
        t.setEnd();
        application.setTextCalc(t.toString());
        application.setTextNrEdges(String.format("%d", this.koch.getNrOfEdges()));
    }

    public void changeLevel(int currentLevel) {
        koch.setLevel(currentLevel);
        this.edges.clear();
        
        TimeStamp t = new TimeStamp();
        t.setBegin();
        koch.generateBottomEdge();
        koch.generateLeftEdge();
        koch.generateRightEdge();
        t.setEnd();
        application.setTextDraw(t.toString());
        
        drawEdges();
    }

    @Override
    public void update(Observable o, Object arg) {
        this.edges.add((Edge)arg);
    }


}
