package com.example.demo;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SimulatedAnnealing {
    public  static class Result {
        public final int[] initialOrder, bestOrder;
        public final double initialCost, bestCost;
        Result(int[] iOrd, double iCost, int[] bOrd, double bCost)
        {
            this.initialOrder=iOrd; this.initialCost=iCost; this.bestOrder=bOrd; this.bestCost=bCost;
        }
    }


    private static double calcTemp(int i, double T0, double alpha) {
        return T0 * Math.pow(alpha, i);
    }


    private static double routeCost(List<City> cities, int[] order, double penalty, boolean closeLoop) {
        double sum = 0;
        for (int k = 0; k < order.length - 1; k++) {
            sum += Cost.distance(cities.get(order[k]), cities.get(order[k+1]));
        }
        if (closeLoop && order.length > 1) {
            sum += Cost.distance(cities.get(order[order.length-1]), cities.get(order[0]));
        }
        int unsafe = 0;
        for (int idx : order) if (cities.get(idx).safeToFly == 1) unsafe++;
        return sum + penalty * unsafe;
    }


    private static void swapTwo(int[] order, Random rnd) {
        if (order.length < 2) return;
        int i = rnd.nextInt(order.length), j = rnd.nextInt(order.length);
        while (j == i) j = rnd.nextInt(order.length);
        int t = order[i]; order[i] = order[j]; order[j] = t;
    }


    public static Result optimize(List<City> cities, int iterMax, double T0, double alpha, double penalty, boolean closeLoop, Random rnd) {

        int n = cities.size();
        int[] xcurr = new int[n];
        for (int i = 0; i < n; i++) xcurr[i] = i;

        double currCost = routeCost(cities, xcurr, penalty, closeLoop);
        int[] xbest = Arrays.copyOf(xcurr, n);
        double bestCost = currCost;

        int[] initOrder = Arrays.copyOf(xcurr, n);
        double initCost  = currCost;

        for (int i = 1; i <= iterMax; i++) {
            double Tc = calcTemp(i, T0, alpha);


            int[] xnext = Arrays.copyOf(xcurr, n);
            swapTwo(xnext, rnd);


            double nextCost = routeCost(cities, xnext, penalty, closeLoop);
            double dE = nextCost - currCost;


            if (dE < 0) {
                xcurr = xnext; currCost = nextCost;
                if (currCost < bestCost) { xbest = Arrays.copyOf(xcurr, n); bestCost = currCost; }
            } else {

                double acceptProb = Math.exp(-dE / Tc);
                if (acceptProb > rnd.nextDouble()) {
                    xcurr = xnext; currCost = nextCost;
                }
            }
            if (Tc < 1e-9) break;
        }
        return new Result(initOrder, initCost, xbest, bestCost);
    }
}
