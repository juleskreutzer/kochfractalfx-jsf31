/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import calculate.Edge;
import calculate.KochFractal;
import java.util.Observable;
import java.util.Observer;

/**
 *
 * @author Kevto
 */
public class Program implements Observer {
    
    Program() {}

    @Override
    public void update(Observable o, Object arg) {
        Edge e = (Edge) arg;
        System.out.println(String.format("%s -- %s -- %s -- %s", e.Y1, e.X1, e.X2, e.Y2));
    }
    
    public static void main(String[] args) {
        Program program = new Program();
        KochFractal koch = new KochFractal();
        koch.setLevel(2);
        koch.addObserver(program);
        koch.generateBottomEdge();
        koch.generateLeftEdge();
        koch.generateRightEdge();
    }
}
