package com.example.myweather;

public class WeatherData {
    private String province;
    private String city;
    private String updateTime;
    private String temperature;
    private String humidity;

    public WeatherData(String province, String city, String updateTime, String temperature, String humidity) {
        this.province = province;
        this.city = city;
        this.updateTime = updateTime;
        this.temperature = temperature;
        this.humidity = humidity;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }
}
