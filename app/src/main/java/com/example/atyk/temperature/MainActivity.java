package com.example.atyk.temperature;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = "TemperatureSensor";
  private SensorManager manager;
  private Sensor temperatureSensor;
  private final BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {
      final String action = intent.getAction();
      Log.v(TAG, "receive action: " + action);
      if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
        final int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        updateTemperature(temperature);
      }
    }
  };
  private final SensorEventListener listener = new SensorEventListener() {
    @Override public void onSensorChanged(SensorEvent event) {
      Log.v(TAG, "listener called: " + event.sensor.getName());
      final float temperature = event.values[0];
      updateTemperature(temperature);
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {
      Log.v(TAG, "onAccuracyChanged called: " + accuracy);
    }
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    manager = (SensorManager) getSystemService(SENSOR_SERVICE);
    temperatureSensor = manager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    manager = null;
  }

  @Override protected void onPause() {
    super.onPause();
    if (temperatureSensor != null) {
      manager.unregisterListener(listener);
    } else {
      unregisterReceiver(receiver);
    }
  }

  @Override protected void onResume() {
    super.onResume();
    if (temperatureSensor != null) {
      manager.registerListener(listener, temperatureSensor, SensorManager.SENSOR_DELAY_UI);
    } else {
      final IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
      registerReceiver(receiver, filter);
    }
  }

  private void updateTemperature(int temperature) {
    Log.v(TAG, "temperature int value: " + temperature);
    updateTemperature(temperature / 10.0F);
  }

  private void updateTemperature(float temperature) {
    TextView view = (TextView) findViewById(R.id.temperature_text);
    view.setText(
        String.format(Locale.getDefault(), getString(R.string.temperature_format), temperature));
  }
}
