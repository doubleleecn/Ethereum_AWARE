package com.boli.ethereum_aware;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.aware.Aware;
import com.aware.Aware_Preferences;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,MainFragment.OnSensorPickListener,IntervalFragment.OnNumberPickerListener {

    private int Acc_sensor=0;
    private int GPS_sensor=0;
    private int Min_interval=0;
    private int Sec_interval=2;
    private String defaul1="00";
    private String defaul2="0002";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        MainFragment fragment = new MainFragment();
        android.support.v4.app.FragmentTransaction fragmentTransaction=
                getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container,fragment);
        fragmentTransaction.commit();

        getData();
        Log.d("TAG", "min/sec: "+Min_interval+"sec"+Sec_interval);
        Log.d("TAG", "A/GPS: "+Acc_sensor+"GPS sensor"+GPS_sensor);

        //Initialise AWARE
        Intent aware = new Intent(this, Aware.class);
        startService(aware);

        Start_Accelerometer(Acc_sensor,Min_interval,Sec_interval);
        Start_GPS(GPS_sensor,Min_interval,Sec_interval);



        //start service to send data to blockchain
        Intent startIntent = new Intent(this, SendData.class);
        String Acc =Integer.toString(Acc_sensor);
        String Gps =Integer.toString(GPS_sensor);
        String Interval =String.valueOf(Min_interval*60 +Sec_interval);
       // Log.d("MAINActivity", "Acc: "+Acc);
       // Log.d("MAINActivity", "Gps: "+Gps);
         Log.d("TAG", "MAINActivity Interval: "+Interval);
        startIntent.putExtra("ACC_SENSORS", Acc);
        startIntent.putExtra("GPS_SENSORS", Gps);
        startIntent.putExtra("Interval",Interval);
        startService(startIntent);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_sensor) {
            // Handle the camera action
            MainFragment main_fragment = new MainFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction=
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container,main_fragment);
            fragmentTransaction.commit();


        } else if (id == R.id.nav_interval) {
            // Create new fragment and transaction
            IntervalFragment interval_fragment = new IntervalFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction=
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container,interval_fragment);
            fragmentTransaction.commit();


        } else if (id == R.id.nav_user_info) {
            User_Info User_Info_fragment = new User_Info();
            android.support.v4.app.FragmentTransaction fragmentTransaction=
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container,User_Info_fragment);
            fragmentTransaction.commit();

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onNumberPickerSelected(String result) {
        saveData("Interval",result);
        getData();
        Start_Accelerometer(Acc_sensor,Min_interval,Sec_interval);
        Start_GPS(GPS_sensor,Min_interval,Sec_interval);

    }

    @Override
    public void onSensorSelected(String result) {
        saveData("Sensors",result);
        getData();
        Start_Accelerometer(Acc_sensor,Min_interval,Sec_interval);
        Start_GPS(GPS_sensor,Min_interval,Sec_interval);

    }

    //get the data saved in activity
    public void getData()
    {
        SharedPreferences mPerferences = getSharedPreferences("SETTING_DATA", 0);
        //SharedPreferences mPerferences = PreferenceManager.getDefaultSharedPreferences(this);//get default preference
        //get activity's private preference
        SharedPreferences m_private;
        m_private = this.getPreferences(Context.MODE_PRIVATE);
        String counter_sensor=mPerferences.getString("Sensors", defaul1);//default value is 0
        String counter_interval=mPerferences.getString("Interval", defaul2);//default value is0
//        Log.d("counter_interval", "getdata: "+counter_interval);
//        Log.d("counter_sensor", "getdata: "+counter_sensor);
        if(counter_interval!="0002")
        {
            Min_interval = Character.getNumericValue(counter_interval.charAt(0)) * 10 + Character.getNumericValue(counter_interval.charAt(1));
            Sec_interval = Character.getNumericValue(counter_interval.charAt(2)) * 10 + Character.getNumericValue(counter_interval.charAt(3));
        }
        if(counter_sensor!="00")
        {
            Acc_sensor = Character.getNumericValue(counter_sensor.charAt(0));
            GPS_sensor = Character.getNumericValue(counter_sensor.charAt(1));
        }

    }


    public void saveData(String ID,String data)
    {
        //record required data
        SharedPreferences mPerferences = getSharedPreferences("SETTING_DATA", 0);
        //SharedPreferences mPerferences=PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences m_private=this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor=mPerferences.edit();
        mEditor.putString(ID, data);
        mEditor.commit();
    }

    private int Start_Accelerometer(int acc_sensor, int min_interval, int sec_interval) {
        if(acc_sensor!=0){
            //Activate Accelerometer
            Aware.setSetting(this, Aware_Preferences.STATUS_ACCELEROMETER, true);
            //Set sampling frequency
           // int interval = (min_interval*60+sec_interval)*1000;
            Aware.setSetting(this, Aware_Preferences.FREQUENCY_ACCELEROMETER, 200000);
            //Apply settings
            Aware.startSensor(this, Aware_Preferences.STATUS_ACCELEROMETER);
            Log.d("TAG", "Accelerometer Sensor start ");
            return 1;
        }else{
            //Deactivate Accelerometer
            Aware.setSetting(this, Aware_Preferences.STATUS_ACCELEROMETER, false);
            //Apply settings
            Aware.stopSensor(this, Aware_Preferences.STATUS_ACCELEROMETER);
            Log.d("TAG", "Accelerometer Sensor stop ");
            return 0;
        }
    }

    private int Start_GPS(int gps_sensor, int min_interval, int sec_interval) {
        if(gps_sensor!=0){
            //Activate GPS
            //Set sampling frequency
            //int interval = (min_interval*60+sec_interval)*1000;
            Aware.setSetting(getApplication(), Aware_Preferences.STATUS_LOCATION_GPS, true);
            Aware.setSetting(getApplication(), Aware_Preferences.STATUS_LOCATION_NETWORK, true);
            Aware.setSetting(getApplication(), Aware_Preferences.FREQUENCY_LOCATION_GPS, 0);
            Aware.setSetting(getApplication(), Aware_Preferences.FREQUENCY_LOCATION_NETWORK, 0);
            Aware.setSetting(getApplication(), Aware_Preferences.MIN_LOCATION_GPS_ACCURACY, 0);
            Aware.setSetting(getApplication(), Aware_Preferences.MIN_LOCATION_NETWORK_ACCURACY, 0);
            Aware.setSetting(getApplication(), Aware_Preferences.LOCATION_EXPIRATION_TIME,300);
            Aware.startSensor(this, Aware_Preferences.STATUS_LOCATION_GPS);

            Log.d("TAG", "GPS Sensor start ");
            return 1;
        }else{
            //Deactivate GPS
            Aware.setSetting(this, Aware_Preferences.STATUS_LOCATION_GPS, false);
            //Apply settings
            Aware.stopSensor(this, Aware_Preferences.STATUS_LOCATION_GPS);
            Log.d("TAG", "GPS Sensor stop ");
            return 0;
        }
    }
}
