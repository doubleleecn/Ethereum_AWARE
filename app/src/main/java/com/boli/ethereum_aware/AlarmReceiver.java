package com.boli.ethereum_aware;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by boli on 8.8.2016.
 */
public class AlarmReceiver extends BroadcastReceiver {

    private String defaul1="00";
    private String defaul2="0130";
    private int Acc_sensor=0;
    private int GPS_sensor=0;
    private int Min_interval=0;
    private int Sec_interval=0;

    @Override
    public void onReceive(Context context, Intent intent) {

        //get preference
        SharedPreferences mPerferences = context.getSharedPreferences("SETTING_DATA", 0);
        String counter_sensor=mPerferences.getString("Sensors", defaul1);//default value is 0
        String counter_interval=mPerferences.getString("Interval", defaul2);
        Acc_sensor = Character.getNumericValue(counter_sensor.charAt(0));
        GPS_sensor = Character.getNumericValue(counter_sensor.charAt(1));
        String Acc =Integer.toString(Acc_sensor);
        String Gps =Integer.toString(GPS_sensor);
        //Log.d("onReceive", "Acc: "+Acc);
        //Log.d("onReceive", "Gps: "+Gps);

        Min_interval = Character.getNumericValue(counter_interval.charAt(0)) * 10 + Character.getNumericValue(counter_interval.charAt(1));
        Sec_interval = Character.getNumericValue(counter_interval.charAt(2)) * 10 + Character.getNumericValue(counter_interval.charAt(3));
        String Interval =String.valueOf(Min_interval*60 +Sec_interval);
        //Log.d("onReceive", "Interval: "+Interval);
        Intent i = new Intent(context, SendData.class);
        i.putExtra("ACC_SENSORS", Acc);
        i.putExtra("GPS_SENSORS", Gps);
        i.putExtra("Interval",Interval);
        context.startService(i);
    }
}
