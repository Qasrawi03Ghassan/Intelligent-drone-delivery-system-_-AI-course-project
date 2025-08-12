package com.example.demo;

public class Perceptron {

    private double[] w = new double[3];
    private double b = 0.0;
    private double lr = 0.01;

    public void train(double[][] x, int[] y, int epochs) {
        for (int e = 0; e < epochs; e++) {
            for (int i = 0; i < x.length; i++) {
                int yhat = predict(x[i]);
                int err = y[i] - yhat;
                for (int j = 0; j < 3; j++) {
                    w[j] += lr * err * x[i][j];
                }
                b += lr * err;
            }
        }
    }

    public int predict(double[] x) {
        double z = b;
        for (int j = 0; j < 3; j++) z += w[j] * x[j];
        return z >= 0 ? 1 : 0;
    }
}
