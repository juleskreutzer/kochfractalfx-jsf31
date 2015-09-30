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
    private List<Thread> threads;
    private int count;
    
    public KochManager(JSF31KochFractalFX application) {
        this.application = application;
        this.edges = new ArrayList<>();
        this.koch = new KochFractal();
        this.koch.addObserver(this);
        this.koch1 = new KochFractal();
        this.koch1.addObserver(this);
        this.koch2 = new KochFractal();
        this.koch2.addObserver(this);
        this.koch3 = new KochFractal();
        this.koch3.addObserver(this);
        this.threads = new ArrayList<>();
        this.count = 0;
    }
    
    public void drawEdges() {
        if(count >= 3) {
            application.clearKochPanel();

            TimeStamp t = new TimeStamp();
            t.setBegin();

            for(Edge e : edges) {
                application.drawEdge(e); 
            }

            t.setEnd();
            application.setTextDraw(t.toString());
            application.setTextNrEdges(String.format("%d", this.koch.getNrOfEdges()));
        }
    }

    public void changeLevel(int currentLevel) {
        clearAllThreads();
        resetCount();
        koch.setLevel(currentLevel);
        koch1.setLevel(currentLevel);
        koch2.setLevel(currentLevel);
        koch3.setLevel(currentLevel);
        this.edges.clear();
        
        Thread end = new Thread(new Runnable(){
            @Override public void run() {
                synchronized(getCurrentInstance()) {
                    try {
                        final TimeStamp t = new TimeStamp();
                        t.setBegin();
                        getCurrentInstance().wait();
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
            }
        });
        threads.add(end);
        
        Thread t1 = new Thread(new GenerateRunnable(koch1, this, GeneratePart.BOTTOM));
        threads.add(t1);
        Thread t2 = new Thread(new GenerateRunnable(koch1, this, GeneratePart.LEFT));
        threads.add(t2);
        Thread t3 = new Thread(new GenerateRunnable(koch1, this, GeneratePart.RIGHT));
        threads.add(t3);

        
        end.start();
        t1.start();
        t2.start();
        t3.start();
    }

    @Override
    public void update(Observable o, Object arg) {
        synchronized(this) {
            this.edges.add((Edge)arg);
        }
    }
    
    /**
     * Thread safe getting the manager for anonymous runnables.
     * @return KochManager current instance that we're working with.
     */
    public synchronized KochManager getCurrentInstance() {
        return this;
    }
    
    /**
     * Thread safe upping the count.
     */
    public synchronized int countUp() {
        this.count++;
        return count;
    }
    
    
    /**
     * Thread safe resetting the count.
     */
    public synchronized void resetCount() {
        this.count = 0;
    }
    
    
    /**
     * Clearing all the bald headed threads.
     */
    public synchronized void clearAllThreads() {
        for(Thread t: threads)
            t.interrupt();
        threads.clear();
    }
    
    
    /**
     * New runnable to generate the edges.
     */
    public static class GenerateRunnable implements Runnable {
        final KochFractal koch;
        final KochManager manager;
        final GeneratePart part;
        
        public GenerateRunnable(KochFractal koch, KochManager manager, GeneratePart part) {
            this.koch = koch;
            this.manager = manager;
            this.part = part;
        }
        
        @Override public void run() {
            try {
                if(Thread.interrupted())
                    throw new InterruptedException("Interrupted before executing...");
                
                switch(part) {
                    case BOTTOM:
                        koch.generateBottomEdge();
                        break;
                    case LEFT:
                        koch.generateLeftEdge();
                        break;
                    case RIGHT:
                        koch.generateRightEdge();
                        break;
                    default:
                        throw new Exception("How did this happen?!");
                }
                
                if(Thread.interrupted())
                    throw new InterruptedException("Interrupted after executing...");
                
                synchronized(manager) {
                    if(manager.countUp() >= 3)
                        manager.notify();
                }
            } catch (InterruptedException ex) {
                System.err.println("[INFO][THREAD-" + Thread.currentThread().getId() + "] " + ex.getMessage());
            } catch (Exception ex) {
                System.err.println("[INFO][THREAD-" + Thread.currentThread().getId() + "] " + ex.getMessage());
            }
            
            System.out.println("[INFO][THREAD-" + Thread.currentThread().getId() + "] Runnable has been executed and done!");
        }
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
