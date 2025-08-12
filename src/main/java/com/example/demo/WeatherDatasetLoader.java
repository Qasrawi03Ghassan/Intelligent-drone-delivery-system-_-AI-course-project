package com.example.demo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class WeatherDatasetLoader {
    public static double[][] features;
    public static int[] labels;

    public static void load(String filename) throws Exception {
        ArrayList<double[]> Xlist = new ArrayList<>();
        ArrayList<Integer> ylist = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line = br.readLine();
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            double temp = Double.parseDouble(parts[0]);
            double hum = Double.parseDouble(parts[1]);
            double wind = Double.parseDouble(parts[2]);
            int label = Integer.parseInt(parts[3]);

            Xlist.add(new double[]{temp, hum, wind});
            ylist.add(label);
        }
        br.close();

        features = Xlist.toArray(new double[0][0]);
        labels = ylist.stream().mapToInt(i -> i).toArray();
    }
}
