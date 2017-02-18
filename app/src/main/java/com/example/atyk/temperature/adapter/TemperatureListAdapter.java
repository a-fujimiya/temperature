package com.example.atyk.temperature.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.atyk.temperature.R;
import com.example.atyk.temperature.model.TemperatureLog;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created on 2017/02/18.
 */

public class TemperatureListAdapter extends ArrayAdapter<TemperatureLog> {
  private static final String TAG = "TemperatureListAdapter";
  private final LayoutInflater inflater;

  public TemperatureListAdapter(Context context, int resource, List<TemperatureLog> objects) {
    super(context, resource, objects);
    inflater = LayoutInflater.from(context);
  }

  @NonNull @Override
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    final View view;
    final ViewHolder holder;
    if (convertView != null) {
      view = convertView;
      holder = (ViewHolder) view.getTag();
    } else {
      view = inflater.inflate(R.layout.list_item, parent, false);
      holder = new ViewHolder();
      holder.dateTimeText = (TextView) view.findViewById(R.id.data_date);
      holder.temperatureText = (TextView) view.findViewById(R.id.data_temperature);
      view.setTag(holder);
    }
    final TemperatureLog temperatureLog = getItem(position);
    if (temperatureLog != null) {
      final long dateTime = temperatureLog.getDateTime();
      final float temperature = temperatureLog.getTemperature();
      holder.dateTimeText.setText(convertDateString(dateTime));
      holder.temperatureText.setText(String.valueOf(temperature));
    } else {
      Log.v(TAG, "temperatureLog is null: " + position);
    }
    return view;
  }

  private String convertDateString(long dateTime) {
    final DateFormat df = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss", Locale.getDefault());
    final Date date = new Date(dateTime);
    return df.format(date);
  }

  private static class ViewHolder {
    TextView dateTimeText;
    TextView temperatureText;
  }
}
