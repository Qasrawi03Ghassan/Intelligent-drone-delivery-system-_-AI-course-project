package com.example.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class City {

    public final int id;
    public double x, y;
    public double temperature, humidity, windSpeed;
    public int safeToFly;

    public City(int id, double x, double y,double temperature, double humidity, double windSpeed) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.safeToFly = -1;
    }
    public int getId(){ return id; }
    public double getX(){ return x; }
    public double getY(){ return y; }
    public double getTemperature(){ return temperature; }
    public double getHumidity(){ return humidity; }
    public double getWindSpeed(){ return windSpeed; }
    public String getSafeLabel() {
        return safeToFly == -1 ? "—" : (safeToFly == 0 ? "Safe" : "Unsafe");
    }


}
