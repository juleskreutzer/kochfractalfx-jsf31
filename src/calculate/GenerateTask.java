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

/**
 *
 * @author Kevto
 */
public class GenerateTask implements Callable<List<Edge>>, Observer {
        KochFractal koch;
        final KochManager.GeneratePart part;
        final CyclicBarrier barrier;
        List<Edge> edges;
        
        public GenerateTask(int level, KochManager.GeneratePart part, CyclicBarrier barrier) {
            this.koch = new KochFractal();
            this.koch.setLevel(level);
            this.koch.addObserver(this);
            this.edges = new ArrayList<>();
            this.part = part;
            this.barrier = barrier;
        }
        
        @Override public List<Edge> call() {
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
                
                barrier.await();
                
                if(Thread.interrupted())
                    throw new InterruptedException("Interrupted after executing...");
            } catch (InterruptedException ex) {
                System.err.println("[INFO][THREAD-" + Thread.currentThread().getId() + "] " + ex.getMessage());
            } catch (Exception ex) {
                System.err.println("[INFO][THREAD-" + Thread.currentThread().getId() + "] " + ex.getMessage());
            }
            
            System.out.println("[INFO][THREAD-" + Thread.currentThread().getId() + "] Runnable has been executed and done!");
            return edges;
        }

        @Override
        public void update(Observable o, Object arg) {
            synchronized(this) {
                edges.add((Edge)arg);
            }
        }
    }
