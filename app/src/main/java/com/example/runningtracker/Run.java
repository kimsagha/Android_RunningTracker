package com.example.runningtracker;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "runs")
public class Run implements Serializable {

    @NonNull
    @PrimaryKey(autoGenerate = true)
    private int id; // auto generated

    @NonNull
    private double speed; // kilometers/hour

    @NonNull
    private double distance; // kilometers

    @NonNull
    private String time; // hh:mm:ss

    @NonNull
    private String date; // yyyy-mm-dd

    private String name; // optional

    private Integer rating; // 1-5, optional

    private Integer temperature; // degrees Celsius, optional

    // constructor
    public Run(
            @NonNull int id, @NonNull double speed, @NonNull double distance, @NonNull String time,
            @NonNull String date, String name, Integer temperature, Integer rating) {
        this.id = id;
        this.speed = speed;
        this.distance = distance;
        this.time = time;
        this.date = date;
        this.name = name;
        this.temperature = temperature;
        this.rating = rating;
    }

    // getters and setters for run values
    public int getId() {
        return id;
    }

    public double getSpeed() {
        return speed;
    }

    public double getDistance() {
        return distance;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTemperature() {
        return temperature;
    }

    public void setTemperature(Integer temperature) {
        this.temperature = temperature;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

}
