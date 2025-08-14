package com.example.demo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.Random;

import java.util.List;

public class MainController {


    @FXML private TextField xField, yField, tempField, humField, windField, randomCitiesNumberField, coolingRateField, initTempField;
     @FXML private  TextArea  CostFiled,AccuracyFiled;
    @FXML private  TextArea  CostFiled1;
@FXML private AnchorPane Page2;

    @FXML private TableView<City> table;
    @FXML private TableColumn<City, Integer> idCol;
    @FXML private TableColumn<City, Double> xCol, yCol, tCol, hCol, wCol;
    @FXML private TableColumn<City, String> sCol;
    @FXML private Pane startPage;
    @FXML private Canvas canvas, canvas1;

    private final ObservableList<City> cities = FXCollections.observableArrayList();


    private final Perceptron p = new Perceptron();

    private int nextId = 0;

    @FXML
    public void initialize() {

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        xCol.setCellValueFactory(new PropertyValueFactory<>("x"));
        yCol.setCellValueFactory(new PropertyValueFactory<>("y"));
        tCol.setCellValueFactory(new PropertyValueFactory<>("temperature"));
        hCol.setCellValueFactory(new PropertyValueFactory<>("humidity"));
        wCol.setCellValueFactory(new PropertyValueFactory<>("windSpeed"));
        sCol.setCellValueFactory(new PropertyValueFactory<>("safeLabel"));
        table.setItems(cities);
        table.refresh();
        try {
            // Data Train and  separation

            WeatherDatasetLoader.load("src/main/resources/weather_data.csv");
            double[][] X = WeatherDatasetLoader.features;
            int[] y       = WeatherDatasetLoader.labels;
            int n = X.length;


            java.util.List<Integer> idx = new java.util.ArrayList<>();
            for (int i = 0; i < n; i++) idx.add(i);
            java.util.Collections.shuffle(idx, new java.util.Random(42));


            int trainSize = (int) Math.round(n * 0.7);


            double[][] Xtr = new double[trainSize][];
            int[]      ytr = new int[trainSize];
            double[][] Xte = new double[n - trainSize][];
            int[]      yte = new int[n - trainSize];

            for (int i = 0; i < trainSize; i++) {
                int id = idx.get(i);
                Xtr[i] = X[id];
                ytr[i] = y[id];
            }
            for (int i = trainSize; i < n; i++) {
                int id = idx.get(i);
                Xte[i - trainSize] = X[id];
                yte[i - trainSize] = y[id];
            }


            p.train(Xtr, ytr, 1000);


            double acc = calcAccuracy(p, Xte, yte);
            System.out.printf("Test Accuracy = %.2f%%%n", acc);

            AccuracyFiled.setText(String.format("%.2f", acc));


        } catch (Exception ex) {
            showError("Training Error", ex.getMessage());
        }

        redraw();
        drawAfter(false);
    }

    @FXML
    private void onAddRandomCities(){
        try{
            int citiesNum = Integer.parseInt(randomCitiesNumberField.getText());
            if(citiesNum <= 0) throw new Exception();
            City[] randCities = new City[citiesNum];

            int minXY = 0;
            int maxXY = 100;

            Random rand = new Random();

            for (int i = 0; i < citiesNum; ) {
                int x = rand.nextInt(maxXY - minXY + 1) + minXY;
                int y = rand.nextInt(maxXY - minXY + 1) + minXY;

                boolean tooClose = false;
                for (City existing : cities) {
                    double distance = Math.hypot(existing.getX() - x, existing.getY() - y);
                    if (distance < 15) {
                        tooClose = true;
                        break;
                    }
                }

                if (!tooClose) {
                    double temp = 5.0 + 45.0 * rand.nextDouble();
                    double hum = 100 * rand.nextDouble();
                    double wind = 100 * rand.nextDouble();


                    temp = Math.round(temp * 100.0) / 100.0;
                    hum = Math.round(hum * 100.0) / 100.0;
                    wind = Math.round(wind * 100.0) / 100.0;

                    randCities[i] = new City(nextId++, x, y, temp, hum, wind);
                    cities.add(randCities[i]);
                    i++;
                }
            }




//            for(int i=0;i<citiesNum;i++){
//                int x = rand.nextInt(maxXY - minXY + 1) + minXY;
//                int y = rand.nextInt(maxXY - minXY + 1) + minXY;
//
//                double temp = 5.0 + 45.0 * rand.nextDouble(); // Random temp for each city between 5 and 50 degrees
//                double hum = 100 * rand.nextDouble(); // Random humidity double between 0 and 100 for each city
//                double wind = 100 * rand.nextDouble(); // Random wind double between 0 and 100 for each city
//
//                temp = Math.round(temp * 100.0) / 100.0;
//                hum = Math.round(hum * 100.0) / 100.0;
//                wind = Math.round(wind * 100.0) / 100.0;
//
//                randCities[i] = new City(nextId++,x,y,temp,hum,wind);
//                cities.add(randCities[i]);
//            }
            table.refresh();
            redraw();
            randomCitiesNumberField.clear();

        }catch(Exception e){
            showError("Input error", "Please enter a valid number");
        }
    }

    @FXML
    private void onAddCity() {
        try {
            double x = Double.parseDouble(xField.getText().trim());
            double y = Double.parseDouble(yField.getText().trim());
            double t = Double.parseDouble(tempField.getText().trim());
            double h = Double.parseDouble(humField.getText().trim());
            double w = Double.parseDouble(windField.getText().trim());

            City c = new City(nextId++, x, y, t, h, w);
            cities.add(c);
            table.refresh();
            redraw();
            clearInputs(false);
        } catch (Exception e) {
            showError("Input Error", "make sure to enter all fields.");
        }
    }

    boolean generateRoundRoute = false;
    @FXML
    private void onPredictSafety() {

        for(City c:cities)
        {
            int pred =p.predict(new double[]{c.temperature, c.humidity, c.windSpeed});
            c.safeToFly = pred;
        }
        double penalty =50;
        double cost = Cost.totalCost(cities, penalty, closeLoop);
        setCostValue(cost);

        generateRoundRoute = true;

        table.refresh();
        redraw();

    }


    private static double calcAccuracy(Perceptron p, double[][] X, int[] y) {
        int correct = 0;
        for (int i = 0; i < X.length; i++) {
            if (p.predict(X[i]) == y[i]) correct++;
        }
        return X.length == 0 ? 0.0 : (correct * 100.0) / X.length;
    }



    @FXML
    private void onClear() {
        cities.clear();
        nextId = 0;
        redraw();
        drawAfter(false);

        CostFiled.clear();
        CostFiled1.clear();
    }

    private void drawRandomRoute(GraphicsContext g, List<double[]> points, boolean closeLoop) {
        if (points.size() < 2) return;

        // Create a list of indices instead of points
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) indices.add(i);

        // Shuffle indices to create a random route
        java.util.Collections.shuffle(indices);

        g.setStroke(Color.GRAY);
        g.setLineWidth(2);
        double radius = 12; // point radius

        // Draw lines between cities along the random route
        for (int i = 0; i < indices.size() - 1; i++) {
            double[] from = points.get(indices.get(i));
            double[] to = points.get(indices.get(i + 1));

            double dx = to[0] - from[0];
            double dy = to[1] - from[1];
            double length = Math.sqrt(dx*dx + dy*dy);
            double offsetX = dx / length * radius;
            double offsetY = dy / length * radius;

            g.strokeLine(from[0] + offsetX, from[1] + offsetY,
                    to[0] - offsetX, to[1] - offsetY);
        }

        // Optional: close the loop
        if (closeLoop) {
            double[] last = points.get(indices.get(indices.size() - 1));
            double[] first = points.get(indices.get(0));

            double dx = first[0] - last[0];
            double dy = first[1] - last[1];
            double length = Math.sqrt(dx*dx + dy*dy);
            double offsetX = dx / length * radius;
            double offsetY = dy / length * radius;

            g.strokeLine(last[0] + offsetX, last[1] + offsetY,
                    first[0] - offsetX, first[1] - offsetY);
        }
    }

    private void drawBestRoute(GraphicsContext g, List<double[]> points, boolean closeLoop) {
        if (points.size() < 2) return;

        // Create a list of indices instead of points
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) indices.add(i);

        ArrayList<Integer> bestRoute = new ArrayList<>();
        for(int i=0;i<points.size();i++){
            bestRoute.add(optRes.bestOrder[i]);
        }
        indices = bestRoute;

        g.setStroke(Color.YELLOW);
        g.setLineWidth(2);
        double radius = 12; // point radius

        // Draw lines between cities along the random route
        for (int i = 0; i < indices.size() - 1; i++) {
            double[] from = points.get(indices.get(i));
            double[] to = points.get(indices.get(i + 1));

            double dx = to[0] - from[0];
            double dy = to[1] - from[1];
            double length = Math.sqrt(dx*dx + dy*dy);
            double offsetX = dx / length * radius;
            double offsetY = dy / length * radius;

            g.strokeLine(from[0] + offsetX, from[1] + offsetY,
                    to[0] - offsetX, to[1] - offsetY);
        }

        // Optional: close the loop
        if (closeLoop) {
            double[] last = points.get(indices.get(indices.size() - 1));
            double[] first = points.get(indices.get(0));

            double dx = first[0] - last[0];
            double dy = first[1] - last[1];
            double length = Math.sqrt(dx*dx + dy*dy);
            double offsetX = dx / length * radius;
            double offsetY = dy / length * radius;

            g.strokeLine(last[0] + offsetX, last[1] + offsetY,
                    first[0] - offsetX, first[1] - offsetY);
        }
    }



    private void drawAfter(boolean isOptimize) {
        GraphicsContext g = canvas1.getGraphicsContext2D();
        g.clearRect(0, 0, canvas1.getWidth(), canvas1.getHeight());

        g.setStroke(Color.WHITE);

        g.strokeLine(30, canvas1.getHeight() - 30, canvas1.getWidth() - 10, canvas1.getHeight() - 30);
        g.strokeLine(30, canvas1.getHeight() - 30, 30, 10);

        g.strokeLine(30, canvas.getHeight() - 30, canvas.getWidth() - 10, canvas.getHeight() - 30);
        g.strokeLine(30, canvas.getHeight() - 30, 30, 10);

        List<double[]> points = new java.util.ArrayList<>();

        for (City c : cities) {
            double px = 30 + c.x * 3;
            double py = (canvas.getHeight() - 30) - c.y * 3;

            points.add(new double[]{px, py});

            if (c.safeToFly == 1) g.setFill(Color.RED);
            else if (c.safeToFly == 0) g.setFill(Color.GREEN);
            else g.setFill(Color.GRAY);

            //g.fillOval(px-4, py-4, 8, 8);

            double radius = 12;
            g.fillOval(px - radius, py - radius, radius * 2, radius * 2);

            g.setFill(Color.WHITE);
            g.setFont(Font.font("Arial", FontWeight.BOLD, 12));

            String label = String.valueOf(c.id);

            double textWidth = label.length() * 6;
            double textHeight = 4;
            g.fillText(label, px - textWidth / 2, py + textHeight);
        }
        if(isOptimize)drawBestRoute(g,points,true);

    }



    private void redraw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        g.setStroke(Color.WHITE);

        g.strokeLine(30, canvas.getHeight()-30, canvas.getWidth()-10, canvas.getHeight()-30);
        g.strokeLine(30, canvas.getHeight()-30, 30, 10);

        List<double[]> points = new java.util.ArrayList<>();

        for (City c : cities) {
            double px = 30 + c.x * 3;
            double py = (canvas.getHeight()-30) - c.y * 3;

            points.add(new double[]{px, py});

            if (c.safeToFly == 1) g.setFill(Color.RED);
            else if (c.safeToFly == 0) g.setFill(Color.GREEN);
            else g.setFill(Color.GRAY);

            //g.fillOval(px-4, py-4, 8, 8);

            double radius = 12;
            g.fillOval(px-radius,py-radius,radius*2,radius*2);

            g.setFill(Color.WHITE);
            g.setFont(Font.font("Arial", FontWeight.BOLD,12));

            String label = String.valueOf(c.id);

            double textWidth = label.length() * 6;
            double textHeight = 4;
            g.fillText(label, px - textWidth / 2, py + textHeight);
        }
        if(generateRoundRoute)drawRandomRoute(g, points, true); // Use true to generate a closed route
        generateRoundRoute = false;
        g.setStroke(Color.WHITE);
    }

    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    private void clearInputs(boolean all) {
        if (all) {
            xField.clear();
            yField.clear();
            tempField.clear();
            humField.clear();
            windField.clear();


            initTempField.clear();
            coolingRateField.clear();

        } else {
            tempField.clear(); humField.clear(); windField.clear();
        }
    }
    boolean closeLoop = true;

    public void setCostValue(double cost) {
        CostFiled.setText(String.format("%.2f", cost));
    }


    public void startPageButton()
        {
        startPage.setVisible(false);
            Page2.setVisible(true);
       }

    SimulatedAnnealing.Result optRes;
    boolean optimize = false;
@FXML
    private void onOptimizeRoute() {
        if (cities.size() < 2) return;

        optimize = true;

        for (City c: cities)
            c.safeToFly = p.predict(new double[]{c.temperature, c.humidity, c.windSpeed});

        int iterMax = 20000;
        double T0;
        double alpha;
        double penalty = 50.0; boolean closeLoop = true;

        try{
            T0 = Double.parseDouble(initTempField.getText());
            alpha = Double.parseDouble(coolingRateField.getText());

            var res = SimulatedAnnealing.optimize(cities, iterMax, T0, alpha, penalty, closeLoop, new java.util.Random());
            CostFiled1.setText(String.format("%.2f",res.bestCost));

            System.out.println();
            System.out.println();

            optRes = res;

            System.out.print("Initial route: ");
            for(int i=0;i<res.initialOrder.length;i++){
                System.out.print(res.initialOrder[i]+" ");
            }
            System.out.println();

            System.out.print("Initial cost: "+ res.initialCost);

            System.out.print("\nBest route: ");
            for(int i=0;i<res.bestOrder.length;i++){
                System.out.print(res.bestOrder[i]+" ");
            }

            System.out.println();
            System.out.print("Best cost: "+ res.bestCost);

        }catch(Exception e){
            showError("Input error","Please enter a valid input for simulated annealing parameters first!");
        };

        drawAfter(true);


//        var res = SimulatedAnnealing.optimize(cities, iterMax, T0, alpha, penalty, closeLoop, new java.util.Random());
//
//        CostFiled1.setText(String.format("%.2f",res.bestCost));

    }



}
