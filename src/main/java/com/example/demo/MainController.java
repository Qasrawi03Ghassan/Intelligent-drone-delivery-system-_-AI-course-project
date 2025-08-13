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

import java.util.List;

public class MainController {


    @FXML private TextField xField, yField, tempField, humField, windField;
     @FXML private  TextArea  CostFiled,AccuracyFiled;
    @FXML private  TextArea  CostFiled1;
@FXML private AnchorPane Page2;

    @FXML private TableView<City> table;
    @FXML private TableColumn<City, Integer> idCol;
    @FXML private TableColumn<City, Double> xCol, yCol, tCol, hCol, wCol;
    @FXML private TableColumn<City, String> sCol;
    @FXML private Pane startPage;
    @FXML private Canvas canvas;

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


        } catch (Exception ex) {
            showError("Training Error", ex.getMessage());
        }

        redraw();
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
            showError("Input Error", "make sure enter all fields.");
        }
    }

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
    }


    private void redraw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        g.strokeLine(30, canvas.getHeight()-30, canvas.getWidth()-10, canvas.getHeight()-30);
        g.strokeLine(30, canvas.getHeight()-30, 30, 10);

        for (City c : cities) {
            double px = 30 + c.x * 3;
            double py = (canvas.getHeight()-30) - c.y * 3;

            if (c.safeToFly == 1) g.setFill(Color.RED);
            else if (c.safeToFly == 0) g.setFill(Color.GREEN);
            else g.setFill(Color.GRAY);

            g.fillOval(px-4, py-4, 8, 8);
        }
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

@FXML
    private void onOptimizeRoute() {
        if (cities.size() < 2) return;


        for (City c: cities)
            c.safeToFly = p.predict(new double[]{c.temperature, c.humidity, c.windSpeed});

        int iterMax = 20000;
        double T0 = 150.0;
        double alpha = 0.995;
        double penalty = 50.0; boolean closeLoop = true;

        var res = SimulatedAnnealing.optimize(cities, iterMax, T0, alpha, penalty, closeLoop, new java.util.Random());

        CostFiled1.setText(String.format("%.2f",res.bestCost));

    }



}
