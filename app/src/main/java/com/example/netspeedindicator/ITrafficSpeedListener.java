package com.example.netspeedindicator;

public interface ITrafficSpeedListener {
    void onTrafficSpeedMeasured(double upStream, double downStream);
}