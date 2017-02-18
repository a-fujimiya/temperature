package com.example.atyk.temperature.model;

import io.realm.RealmObject;

/**
 * Created on 2017/02/16.
 */

public class TemperatureLog extends RealmObject {
  private long dateTime;
  private float temperature;

  public long getDateTime() {
    return dateTime;
  }

  public void setDateTime(long dateTime) {
    this.dateTime = dateTime;
  }

  public float getTemperature() {
    return temperature;
  }

  public void setTemperature(float temperature) {
    this.temperature = temperature;
  }
}
