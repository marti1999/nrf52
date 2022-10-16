package com.example.test;

public class Entry {
    // string variable for
    // storing employee name.
    private String temperature;

    // string variable for storing
    // employee contact number
    private String wind;

    // string variable for storing
    // employee address.
    private String precipitation;

    // an empty constructor is
    // required when using
    // Firebase Realtime Database.
    public Entry() {

    }

    public Entry(String temperature, String wind, String precipitation) {
        this.temperature = temperature;
        this.wind = wind;
        this.precipitation = precipitation;
    }

    // created getter and setter methods
    // for all our variables.
    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getWind() {
        return wind;
    }

    public void setWind(String wind) {
        this.wind = wind;
    }

    public String getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(String precipitation) {
        this.precipitation = precipitation;
    }

    @Override
    public String toString() {
        return "Entry{" +
                "temperature='" + temperature + '\'' +
                ", wind='" + wind + '\'' +
                ", precipitation='" + precipitation + '\'' +
                '}';
    }
}
