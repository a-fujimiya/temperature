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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.example.atyk.temperature.model.TemperatureLog;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = "TemperatureSensor";
  private static final String STATE_LAST_TEMPERATURE = "STATE_LAST_TEMPERATURE";
  private SensorManager manager;
  private Sensor temperatureSensor;
  private Realm realm;
  private RealmChangeListener realmChangeListener =
      new RealmChangeListener<RealmResults<TemperatureLog>>() {
        @Override public void onChange(RealmResults<TemperatureLog> elements) {
          TemperatureLog temperatureLog = elements.sort("dateTime", Sort.DESCENDING).first();
          final float temperature = temperatureLog.getTemperature();
          Log.v(TAG, "callback temperature: " + temperature);
          updateView(temperature);
        }
      };

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
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    Realm.init(this);
    manager = (SensorManager) getSystemService(SENSOR_SERVICE);
    temperatureSensor = manager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
    realm = Realm.getDefaultInstance();
    updateView(0.0f);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    manager = null;
    temperatureSensor = null;
    if (realm != null) {
      realm.close();
      realm = null;
    }
  }

  @Override protected void onPause() {
    super.onPause();
    if (temperatureSensor != null) {
      manager.unregisterListener(listener);
    } else {
      unregisterReceiver(receiver);
    }
    realm.removeChangeListener(realmChangeListener);
  }

  @Override protected void onResume() {
    super.onResume();
    if (temperatureSensor != null) {
      manager.registerListener(listener, temperatureSensor, SensorManager.SENSOR_DELAY_UI);
    } else {
      final IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
      registerReceiver(receiver, filter);
    }
    RealmResults<TemperatureLog> realmResults = realm.where(TemperatureLog.class).findAll();
    realmResults.addChangeListener(realmChangeListener);
  }

  @Override protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    final TemperatureLog temperatureLog =
        realm.where(TemperatureLog.class).findAllSorted("dateTime", Sort.DESCENDING).first();
    outState.putFloat(STATE_LAST_TEMPERATURE, temperatureLog.getTemperature());
  }

  @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    final float temperature = savedInstanceState.getFloat(STATE_LAST_TEMPERATURE);
    updateView(temperature);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_show_data) {
      startActivity(TemperatureListActivity.getStartIntent(this));
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private void updateTemperature(int temperature) {
    Log.v(TAG, "temperature int value: " + temperature);
    updateTemperature(temperature / 10.0F);
  }

  private void updateTemperature(float temperature) {
    final RealmResults<TemperatureLog> results = realm.where(TemperatureLog.class).findAll();
    if (!results.isEmpty()) {
      final float last = results.sort("dateTime", Sort.DESCENDING).first().getTemperature();
      if (Float.compare(last, temperature) == 0) return;
    }
    realm.beginTransaction();
    final TemperatureLog temperatureLog = realm.createObject(TemperatureLog.class);
    final long dateTime = System.currentTimeMillis();
    temperatureLog.setDateTime(dateTime);
    temperatureLog.setTemperature(temperature);
    realm.commitTransaction();
  }

  private void updateView(float temperature) {
    TextView view = (TextView) findViewById(R.id.temperature_text);
    view.setText(
        String.format(Locale.getDefault(), getString(R.string.temperature_format), temperature));
  }
}
