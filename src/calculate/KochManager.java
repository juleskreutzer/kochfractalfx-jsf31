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
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javafx.application.Platform;
import jsf31kochfractalfx.JSF31KochFractalFX;
import timeutil.TimeStamp;

/**
 *
 * @author Kevto
 */
public class KochManager implements Observer{
    private JSF31KochFractalFX application;
    private KochFractal koch;
    private KochFractal koch1;
    private KochFractal koch2;
    private KochFractal koch3;
    private List<Edge> edges;
    private ExecutorService eService;
    private CyclicBarrier barrier;
    
    public KochManager(JSF31KochFractalFX application) {
        this.application = application;
        this.edges = new ArrayList<>();
        this.koch = new KochFractal();
        this.koch.addObserver(this);

        this.eService = Executors.newFixedThreadPool(3);
        this.barrier = new CyclicBarrier(3);
    }
    
    public void drawEdges() {
        application.clearKochPanel();

        TimeStamp t = new TimeStamp();
        t.setBegin();
        
        synchronized(this) {
            for(Edge e : edges) {
                application.drawEdge(e); 
            }
        }

        t.setEnd();
        application.setTextDraw(t.toString());
        application.setTextNrEdges(String.format("%d", this.koch.getNrOfEdges()));
    }

    public void changeLevel(int currentLevel) {
        koch.setLevel(currentLevel);
        this.edges.clear();
        
        final Future<List<Edge>> bottom = eService.submit(new GenerateTask(koch.getLevel(), GeneratePart.BOTTOM, barrier));
        final Future<List<Edge>> left = eService.submit(new GenerateTask(koch.getLevel(), GeneratePart.LEFT, barrier));
        final Future<List<Edge>> right = eService.submit(new GenerateTask(koch.getLevel(), GeneratePart.RIGHT, barrier));
        
        Thread end = new Thread(new Runnable(){
            @Override public void run() {
                synchronized(this) {
                    try {
                        final TimeStamp t = new TimeStamp();
                        t.setBegin();
                        synchronized(getInstance()) {
                            edges.addAll(bottom.get());
                            Thread.currentThread().sleep(2400);
                            edges.addAll(left.get());
                            Thread.currentThread().sleep(2400);
                            edges.addAll(right.get());
                        }
                        t.setEnd();
                        
                        Platform.runLater(new Runnable() { 
                            @Override public void run() {
                                application.setTextCalc(t.toString());
                            }
                        });
                        
                        application.requestDrawEdges();
                    } catch (Exception ex) {
                        System.err.println("[INFO][THREAD-" + Thread.currentThread().getId() + "] " + ex.getMessage());
                    }
                    System.out.println("[INFO][THREAD-" + Thread.currentThread().getId() + "-WAIT] Runnable has been executed and done!");
                }
                System.out.println("EDGES COUNT: " + edges.size());
            }
        });
        end.start();
        
    }

    @Override
    public void update(Observable o, Object arg) {
        synchronized(this) {
            this.edges.add((Edge)arg);
        }
    }
    
    
    public ExecutorService getThreadPool() {
        return eService;
    }
    
    public KochManager getInstance() {
        return this;
    }

    
    /**
     * Static enumeration for Generate.
     */
    public static enum GeneratePart {
        BOTTOM(0),
        LEFT(1),
        RIGHT(2);
        
        private int val;

        GeneratePart(int val) {
            this.val = val;
        }

        public int val() {
            return val;
        }
    }
}
