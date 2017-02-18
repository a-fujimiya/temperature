package com.example.atyk.temperature;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import com.example.atyk.temperature.adapter.TemperatureListAdapter;
import com.example.atyk.temperature.model.TemperatureLog;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created on 2017/02/18.
 */

public class TemperatureListActivity extends AppCompatActivity {
  private static final String TAG = "TemperatureListActivity";
  private Realm realm;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.temperature_list);

    realm = Realm.getDefaultInstance();
    final RealmResults<TemperatureLog> temperatureLogs =
        realm.where(TemperatureLog.class).findAllSorted("dateTime", Sort.ASCENDING);
    final TemperatureListAdapter adapter =
        new TemperatureListAdapter(this, R.layout.list_item, temperatureLogs);
    final ListView listView = (ListView) findViewById(R.id.list);
    listView.setAdapter(adapter);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (realm != null) {
      realm.close();
      realm = null;
      Log.v(TAG, "realm closed normally");
    }
  }

  static Intent getStartIntent(Context context) {
    return new Intent(context, TemperatureListActivity.class);
  }
}
