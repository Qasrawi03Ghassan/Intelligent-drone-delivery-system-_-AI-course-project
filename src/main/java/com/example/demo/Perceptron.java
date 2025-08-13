package com.example.demo;
import java.util.Random;

public class Perceptron {

    private double[] w = new double[3];
    private double b = 1.0;
    private double lr = 0.1;

    private  double thresould=0.2;



    public void train(double[][] x, int[] y, int epochs) {
      Random random = new Random();
        for (int i = 0; i < 3; i++) {
        w[i] = random.nextDouble() - 0.5; ;
        }
        for (int e = 0; e < epochs; e++) {

            for (int i = 0; i < x.length; i++) {
                int yhat = predict(x[i]);
                int err = y[i] - yhat;
                for (int j = 0; j < 3; j++) {
                    w[j] += lr * err * x[i][j];
                }
                thresould += lr * err;
            }
        }
    }

    public int predict(double[] x) {
        double z = thresould;
        for (int j = 0; j < 3; j++)
            z += w[j] * x[j];
        return z >= 0 ? 1 : 0;
    }


}
