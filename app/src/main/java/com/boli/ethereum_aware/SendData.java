package com.boli.ethereum_aware;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;
import com.aware.providers.Accelerometer_Provider;
import com.aware.providers.Locations_Provider;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by boli on 2.8.2016.
 */
public class SendData extends Service {

    public static final String TAG = "SendData Service";
    public static final String KEY_MD5 = "MD5";
    private JSONObject Acc_Hash_Data;
    private JSONObject GPS_Hash_Data;
    private String Acc_sensor;
    private String GPS_sensor;
    private String P2,P3,P4,P5;
    private String defaul="User Name", defaul_date="2008.01.11";
    private int defaul_status=0;
    private String Interval;
    private int P_DataStatus,P_DataType,P_Period;
    private String P_ConsumerIP,P_Date;
    private String defaul_IP="http://10.20.194.66:3000/REALDATA";
    private String httpUrl = "http://10.20.194.66:3000";


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Acc_sensor = intent.getStringExtra("ACC_SENSORS");
        GPS_sensor = intent.getStringExtra("GPS_SENSORS");
        Interval = intent.getStringExtra("Interval");

        Log.d(TAG, "onStartCommand Interval: " + Interval);
        Log.d(TAG, "onStartCommand Acc_sensor: " + Acc_sensor);
        Log.d(TAG, "onStartCommand GPS_sensor: " + GPS_sensor);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Acc_sensor != null) {
                    if (Integer.parseInt(Acc_sensor) == 1) {
                        CheckDatastatus();
                        Acc_Hash_Data = GethashAcc_data();
                        Send_JSON_DATA(Acc_Hash_Data);
                    }else{
                            Log.d(TAG,"User stopped the Acc sensor");
                        }
                }else{
                    Log.d(TAG, "instance initializer: Acc_sensor==null");
                }
                if (GPS_sensor != null) {
                    if (Integer.parseInt(GPS_sensor) == 1) {
                        CheckDatastatus();
                        GPS_Hash_Data = GethashGPS_data();
                        Send_JSON_DATA(GPS_Hash_Data);
                    }else {
                        Log.d(TAG,"User stopped the GPS sensor");
                    }
                 }else{
                Log.d(TAG, "instance initializer: GPS_sensor==null");
                 }
            }
        }).start();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int Delay = Integer.parseInt(Interval) * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + Delay;
        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() executed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private JSONObject GethashAcc_data() {
        Cursor accelerometer_data = getContentResolver().query(Accelerometer_Provider.Accelerometer_Data.CONTENT_URI, null, "accuracy > 0", null, "timestamp DESC");
        accelerometer_data.moveToFirst();
        if (!(accelerometer_data.getString(0)).equals("")) {
            StringBuffer accdatabuffer = new StringBuffer("");
            //Acc_timestamp = accelerometer_data.getLong(5);
            //Deviece_id = accelerometer_data.getString(1);
            //saveDviceID(Deviece_id);
            for (int i = 0; i < 10; i++) {
                accdatabuffer.append(accelerometer_data.getColumnName(0) + ":" + accelerometer_data.getDouble(0) + ";");
                accdatabuffer.append(accelerometer_data.getColumnName(1) + ":" + accelerometer_data.getString(1) + ";");
                accdatabuffer.append(accelerometer_data.getColumnName(2) + ":" + accelerometer_data.getDouble(2) + ";");
                accdatabuffer.append(accelerometer_data.getColumnName(3) + ":" + accelerometer_data.getString(3) + ";");
                accdatabuffer.append(accelerometer_data.getColumnName(4) + ":" + accelerometer_data.getDouble(4) + ";");
                accdatabuffer.append(accelerometer_data.getColumnName(5) + ":" + accelerometer_data.getLong(5) + ";");
                accdatabuffer.append(accelerometer_data.getColumnName(6) + ":" + accelerometer_data.getString(6) + ";");
                accdatabuffer.append(accelerometer_data.getColumnName(7) + ":" + accelerometer_data.getString(7) + ";");
                accelerometer_data.moveToNext();
            }
            accelerometer_data.close();
            BigInteger bigInteger = null;
            try {
                MessageDigest md = MessageDigest.getInstance(KEY_MD5);
                byte[] inputData = accdatabuffer.toString().getBytes();
                md.update(inputData);
                bigInteger = new BigInteger(md.digest());
            } catch (Exception e) {
                e.printStackTrace();
            }
           // Log.d("TAG", "AccMD5加密后:" + bigInteger.toString(16));

            JSONObject Acc_Data_Json = new JSONObject();
            try {
                Acc_Data_Json.put("Hashdata", bigInteger.toString(16));
                Acc_Data_Json.put("Sensor_id","ACC");
            } catch (JSONException e) {
                e.printStackTrace();
            }
           // Log.d(TAG, "GethashAcc_data Acc_Data_Json: " + Acc_Data_Json);
            return Acc_Data_Json;
        } else {
            //Log.d("TAG", "NO Accelerometer data NOW");
            JSONObject Acc_Data_Json = new JSONObject();
            try {
                Acc_Data_Json.put("Hashdata", "NO Accelerometer data NOW");
                Acc_Data_Json.put("Sensor_id","ACC");
            } catch (JSONException e) {
                e.printStackTrace();
            }
           // Log.d(TAG, "Get_hashAcc_data Acc_Data_Json: " + Acc_Data_Json);
            return Acc_Data_Json;
        }
    }

    private JSONObject GethashGPS_data() {
        Cursor Gps_data = getContentResolver().query(Locations_Provider.Locations_Data.CONTENT_URI, null, "accuracy > 0", null, "timestamp DESC");
        Gps_data.moveToFirst();
        if (!(Gps_data.getString(0)).equals("")) {
            StringBuffer gpsdatabuffer = new StringBuffer("");
            //Gps_timestamp = Gps_data.getLong(5);
            //Deviece_id = Gps_data.getString(1);
            //saveDviceID(Deviece_id);
            for (int i = 0; i < 5; i++) {
                gpsdatabuffer.append(Gps_data.getColumnName(0) + ":" + Gps_data.getDouble(0) + ";");
                gpsdatabuffer.append(Gps_data.getColumnName(1) + ":" + Gps_data.getString(1) + ";");
                gpsdatabuffer.append(Gps_data.getColumnName(2) + ":" + Gps_data.getDouble(2) + ";");
                gpsdatabuffer.append(Gps_data.getColumnName(3) + ":" + Gps_data.getString(3) + ";");
                gpsdatabuffer.append(Gps_data.getColumnName(4) + ":" + Gps_data.getDouble(4) + ";");
                gpsdatabuffer.append(Gps_data.getColumnName(5) + ":" + Gps_data.getLong(5) + ";");
                gpsdatabuffer.append(Gps_data.getColumnName(6) + ":" + Gps_data.getDouble(6) + ";");
                gpsdatabuffer.append(Gps_data.getColumnName(7) + ":" + Gps_data.getString(7) + ";");
                gpsdatabuffer.append(Gps_data.getColumnName(8) + ":" + Gps_data.getDouble(8) + ";");
                gpsdatabuffer.append(Gps_data.getColumnName(9) + ":" + Gps_data.getString(9) + ";");
                gpsdatabuffer.append(Gps_data.getColumnName(10) + ":" + Gps_data.getString(10) + ";");
                Gps_data.moveToNext();
            }
            Gps_data.close();
            BigInteger bigInteger = null;
            try {
                MessageDigest md = MessageDigest.getInstance(KEY_MD5);
                byte[] inputData = gpsdatabuffer.toString().getBytes();
                md.update(inputData);
                bigInteger = new BigInteger(md.digest());
            } catch (Exception e) {
                e.printStackTrace();
            }
            JSONObject Gps_Data_Json = new JSONObject();
            try {
                Gps_Data_Json.put("Hashdata", bigInteger.toString(16));
                Gps_Data_Json.put("Sensor_id","GPS");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return Gps_Data_Json;
        } else {
            JSONObject Gps_Data_Json = new JSONObject();
            try {
                Gps_Data_Json.put("Hashdata", "NO GPS data NOW");
                Gps_Data_Json.put("Sensor_id","GPS");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "GethashGPS_data Acc_Data_Json: " + Gps_Data_Json);
            return Gps_Data_Json;
        }
    }

    private String AddZeroBefore(String param) {
        String result= param;
        for(int i=result.length();i<64;i++)
            result= "0"+result;
        return result;
    }

    private String AddZeroAfter(String param) {
        String result= param;
        for(int i=result.length();i<64;i++)
            result= result+"0";
        return result;
    }

    private static String byteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null){
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];
        for (int j = 0; j < byteArray.length; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    //call add new data function
    private void Send_JSON_DATA(JSONObject JSON_DATA) {
        String resultData="";
        Log.d(TAG, "Start send hashed data to block chain");
        URL url = null;
        try {
            url = new URL(httpUrl);
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedException");
        }
        HttpURLConnection connection = null;
        try {
            //Create connection
            //Log.d(TAG, "Send_JSON_DATA : "+JSON_DATA);
            String methodname = "0x3e4aaec9";
            //offset of parameters
            P2="000000000000000000000000000000000000000000000000000000000000006000000000000000000000000000000000000000000000000000000000000000a000000000000000000000000000000000000000000000000000000000000000e0";

            SharedPreferences mPerferences = getApplication().getSharedPreferences("SETTING_DATA", Activity.MODE_PRIVATE);
            P3=mPerferences.getString("UserName", defaul);//如果没有获取到的话默认是"User Name"
            String P3_1 =Integer.toString(P3.length());
            byte[] para3 = P3.getBytes(StandardCharsets.UTF_8);
            P3 = byteArrayToHexStr(para3);

            P4=JSON_DATA.getString("Hashdata");
            P4 = P4.replaceAll( "-","");
            String P4_1 =Integer.toString(P4.length());
            byte[] para4 = P4.getBytes(StandardCharsets.UTF_8);
            P4 = byteArrayToHexStr(para4);

            P5=JSON_DATA.getString("Sensor_id");
            String P5_1 =Integer.toString(P5.length());
            byte[] para5 = P5.getBytes(StandardCharsets.UTF_8);
            P5 = byteArrayToHexStr(para5);

           //ADJUST PARAMETERS
            P3_1=AddZeroBefore(P3_1);
            P4_1=AddZeroBefore(P4_1);
            P5_1=AddZeroBefore(P5_1);
            P3=AddZeroAfter(P3);
            P4=AddZeroAfter(P4);
            P5=AddZeroAfter(P5);

            String params=methodname+P2+P3_1+P3+P4_1+P4+P5_1+P5;
            //Log.d(TAG, "run params: "+params);

            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("from", "0xa9f918a41fa52934720de02913842261061a5743");
            jsonObject1.put("to", "0x743e2a4881EeE26F54c4B1940c30a8f46Dfa6a49");
            jsonObject1.put("value", "0");
            jsonObject1.put("data", params);
            jsonObject1.put("gas", "300000");
            jsonObject1.put("gasPrice", "20000000000");
            JSONArray array = new JSONArray();
            array.put(jsonObject1);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("jsonrpc", "2.0");
            jsonObject.put("method", "eth_sendTransaction");
            jsonObject.put("params", array);
            jsonObject.put("id",1);


            String jsonstr = jsonObject.toString();
            //Log.d(TAG, "run jsonstr: "+jsonstr);

            byte[] req = jsonstr.getBytes(StandardCharsets.UTF_8);

            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Length",Integer.toString(req.length));
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (connection.getOutputStream());
            wr.write(req);
            wr.flush();
            wr.close();

            //Get Response
            int responseCode = connection.getResponseCode();
            if(responseCode==200){
                BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder(); // or StringBuffer if not Java 5+
                String line;
                while((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                    resultData = line;
                    //Log.d(TAG, "Response data: " + line);
                }
                JSONObject resultObj = new JSONObject(resultData);
                String TransactionHash = resultObj.getString("result");
                rd.close();
                Log.d(TAG, "ADD NEW DATA TransactionHash: " + TransactionHash);
                Thread.sleep(20000);
                //check the transaction whether success
                //Log.d(TAG, "start check ADD NEW DATA recipt");
                CheckReceipt(TransactionHash);
            }else{
                Log.d(TAG, "Send Add new data request failed");
                Message meg = new Message();
                meg.arg1=5;
                handler.sendMessage(meg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            } else {
                Log.e(TAG, "Url Null");
            }
        }
    }

    private void CheckReceipt(String hash) {
        Log.d(TAG, "start check ADD NEW DATA receipt");
        // String httpUrl = "http://10.20.223.243:3000";
        String resultData = "";
        URL url = null;
        try {
            url = new URL(httpUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection connection = null;
        try {
            //set parameters
            JSONArray array = new JSONArray();
            array.put(0, hash);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("jsonrpc", "2.0");
            jsonObject.put("method", "eth_getTransactionReceipt");
            jsonObject.put("params", array);
            jsonObject.put("id", 11);
            String jsonstr = jsonObject.toString();
            //Log.d(TAG, "run jsonstr: " + jsonstr);

            byte[] req = jsonstr.getBytes(StandardCharsets.UTF_8);
            //set request property
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Length", Integer.toString(req.length));
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            //Send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.write(req);
            wr.flush();
            wr.close();
            //Get Response
            int responseCode = connection.getResponseCode();
            //System.out.println("Response Code : " + responseCode);
            if(responseCode==200){
                BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder(); // or StringBuffer if not Java 5+
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                    resultData = line;
                   // Log.d(TAG, "Response data: " + line);
                }
                JSONObject resultObj = new JSONObject(resultData);
                String result = resultObj.getString("result");
                //Log.d(TAG, "CheckRecipt result: "+result);
                rd.close();
                //if result equal null, wait 10s and check receipt again
                //parser the transaction receipt, get the log's data
                if(result!= "null" && !result.equals("")){
                    JSONObject resultLog = new JSONObject(result);
                    String Logs = resultLog.getString("logs");
                    //Log.d(TAG, "CheckRecipt logs: "+Logs);
                    JSONArray Temlogdata = new JSONArray(Logs);
                    String LogData = Temlogdata.getString(0);
                    //Log.d(TAG, "CheckRecipt Temlogdata: "+LogData);
                    JSONObject resultLogdata = new JSONObject(LogData);
                    String topics = resultLogdata.getString("topics");
                    //Log.d(TAG, "CheckRecipt topics: "+topics);
                    JSONArray Temtopics = new JSONArray(topics);
                    String topic = Temtopics.getString(2);
                    int Topic=Integer.decode(topic);
                    //Log.d(TAG, "CheckRecipt topic: "+Topic);
                    if(Topic!=0){
                        Log.d(TAG, "Add new DATA success");
                        Message meg = new Message();
                        meg.arg1=1;
                        handler.sendMessage(meg);
                    }else{
                        Log.d(TAG, "Add new DATA failed, user NOT exist");
                        Message meg = new Message();
                        meg.arg1=2;
                        handler.sendMessage(meg);
                    }
                }else{
                    Log.d(TAG, "Transaction waiting be mined");
                    Thread.sleep(10000);
                    CheckReceipt(hash);
                }
            }else{
                Log.d(TAG, "Send CheckRecipt request Failed");
                Message meg = new Message();
                meg.arg1=4;
                handler.sendMessage(meg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void CheckDatastatus(){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String resultData = "";
                    URL url = null;
                    Log.d(TAG, "Start send CHECK USER STATUS request");
                    try {
                        url = new URL(httpUrl);
                        Log.d(TAG, "url: " + url.toString());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    HttpURLConnection connection = null;
                    try {
                        //Create connection
                        String methodname = "0x9090e633";
                        String index="0000000000000000000000000000000000000000000000000000000000000020";
                        SharedPreferences mPerferences = getApplication().getSharedPreferences("SETTING_DATA", Activity.MODE_PRIVATE);
                        String P1=mPerferences.getString("UserName", defaul);//如果没有获取到的话默认是"User Name"
                        String P1_1 =Integer.toString(P1.length());
                        byte[] para3 = P1.getBytes(StandardCharsets.UTF_8);
                        P1 = byteArrayToHexStr(para3);

                        P1_1 = AddZeroBefore(P1_1);
                        P1 = AddZeroAfter(P1);
                        String params = methodname + index +P1_1 + P1;

                        JSONObject jsonObject1 = new JSONObject();
                        jsonObject1.put("from", "0xa9f918a41fa52934720de02913842261061a5743");
                        jsonObject1.put("to", "0x743e2a4881EeE26F54c4B1940c30a8f46Dfa6a49");
                        jsonObject1.put("value", "0");
                        jsonObject1.put("data", params);
                        jsonObject1.put("gas", "300000");
                        jsonObject1.put("gasPrice", "20000000000");
                        JSONArray array = new JSONArray();
                        array.put(jsonObject1);

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("jsonrpc", "2.0");
                        jsonObject.put("method", "eth_sendTransaction");
                        jsonObject.put("params", array);
                        jsonObject.put("id", 2);


                        String jsonstr = jsonObject.toString();
                       // Log.d(TAG, "run jsonstr: " + jsonstr);

                        byte[] req = jsonstr.getBytes(StandardCharsets.UTF_8);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "application/json");
                        connection.setRequestProperty("Content-Length", Integer.toString(req.length));
                        connection.setRequestProperty("Content-Language", "en-US");
                        connection.setUseCaches(false);
                        connection.setDoOutput(true);
                        //Send request
                        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                        wr.write(req);
                        wr.flush();
                        wr.close();

                        //Get Response
                        int responseCode = connection.getResponseCode();
                        if(responseCode==200){
                            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            StringBuilder response = new StringBuilder(); // or StringBuffer if not Java 5+
                            String line;
                            while ((line = rd.readLine()) != null) {
                                response.append(line);
                                response.append('\r');
                                resultData = line;
                               // Log.d(TAG, "Response data: " + line);
                            }
                            JSONObject resultObj = new JSONObject(resultData);
                            String TransactionHash = resultObj.getString("result");
                            rd.close();
                            Log.d(TAG, "check user status TransactionHash: " + TransactionHash);
                            Thread.sleep(20000);
                            //check the transaction whether success
                            //Log.d(TAG, "start check recipt");
                            CheckSecondReceipt(TransactionHash);
                        }else{
                            Log.d(TAG, "Send check data status request failed");
                            Message meg = new Message();
                            meg.arg1=6;
                            handler.sendMessage(meg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        //return null;
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        } else {
                            Log.e(TAG, "Url Null");
                        }
                    }
                }
            }).start();

    }

    private void CheckSecondReceipt(String TransactionHash){
        Log.d(TAG, "start check CHECK USER STATUS receipt");
        String resultData = "";
        URL url = null;
        try {
            url = new URL(httpUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection connection = null;
        try {
            //set parameters
            JSONArray array = new JSONArray();
            array.put(0, TransactionHash);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("jsonrpc", "2.0");
            jsonObject.put("method", "eth_getTransactionReceipt");
            jsonObject.put("params", array);
            jsonObject.put("id", 21);
            String jsonstr = jsonObject.toString();
            //Log.d(TAG, "run jsonstr: " + jsonstr);

            byte[] req = jsonstr.getBytes(StandardCharsets.UTF_8);
            //set request property
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Length", Integer.toString(req.length));
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            //Send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.write(req);
            wr.flush();
            wr.close();
            //Get Response
            int responseCode = connection.getResponseCode();
            //System.out.println("Response Code : " + responseCode);
            if(responseCode==200){
                BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder(); // or StringBuffer if not Java 5+
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                    resultData = line;
                    //Log.d(TAG, "Response data: " + line);
                }
                JSONObject resultObj = new JSONObject(resultData);
                String result = resultObj.getString("result");
                //Log.d(TAG, "CheckRecipt result: "+result);
                rd.close();
                //if result equal null, wait 10s and check receipt again
                //parser the transaction receipt, get the log's data
                if(result!= "null" && !result.equals("")){
                    JSONObject resultLog = new JSONObject(result);
                    String Logs = resultLog.getString("logs");
                    JSONArray Temlogdata = new JSONArray(Logs);
                    String LogData = Temlogdata.getString(0);
                    JSONObject resultLogdata = new JSONObject(LogData);
                    String logdata = resultLogdata.getString("data");
                    //parser data to datastatus,datatype,consumer_ip,period
                    int DataStatus = Integer.decode(logdata.substring(66,130));
                    int DataType = Integer.decode(logdata.substring(130,194));
                    String ConsumerIP = logdata.substring(249,258);
                    int Period = Integer.decode(logdata.substring(320,322));
                    //get old--datastatus, period, date,consumer_ip, timestamp
                    getUserStatus();
                    Date dNow = new Date( );
                    SimpleDateFormat ft = new SimpleDateFormat("yyyy.MM.dd");
                    String Today =ft.format(dNow);
                    //if old_datastatus did not change and old_period+time>today's date
                    if(DataStatus==P_DataStatus&&differentDaysByMillisecond(P_Date,Today)<P_Period) {
                        //send real sensor data to consumer server
                        Log.d(TAG, "CheckSecondReceipt: START send real data");
                        SendRealData(P_ConsumerIP,P_DataType);
                    }
                    if(DataStatus>P_DataStatus) {
                        //if datastatus>old_datastatus then check period
                        //send real sensor data
                        Log.d(TAG, "CheckSecondReceipt: Start send real data to new consumer");
                        SendRealData(ConsumerIP,DataType);
                        //save the data status,date type,consumer_ip,period
                        saveUserStatus(DataStatus,DataType,ConsumerIP,Period,Today);
                    }
                }else{
                    Log.d(TAG, "Transaction waiting be mined");
                    //Do your UI operations like dialog opening or Toast here
                    Message meg = new Message();
                    meg.arg1=3;
                    handler.sendMessage(meg);
                    Thread.sleep(10000);
                    CheckSecondReceipt(TransactionHash);
                }
            }else{
                Log.d(TAG, "Send CheckRecipt request Failed");
                Message meg = new Message();
                meg.arg1=7;
                handler.sendMessage(meg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static int differentDaysByMillisecond(String dateStr,String dateStr2)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
        //SimpleDateFormat format2 = new SimpleDateFormat("yyyy.MM.dd");
        Date date2=null;
        try {
            date2 = format.parse(dateStr2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date date1 = null;
        try {
            date1 = format.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert date2 != null;
        assert date1 != null;
        int days;
        days = (int) ((date2.getTime() - date1.getTime()) / (1000*3600*24));
        //Log.d(TAG, "differentDaysByMillisecond: days"+days);
        return days;

    }

    private void getUserStatus() {
        //get old--datastatus, period, date,consumer_ip
        SharedPreferences mPerferences = getApplicationContext().getSharedPreferences("SETTING_DATA", Activity.MODE_PRIVATE);
        P_DataStatus = mPerferences.getInt("DataStatus", defaul_status);//如果没有获取到的话默认是"User Name"
        P_DataType = mPerferences.getInt("DataType", defaul_status);
        P_ConsumerIP= mPerferences.getString("ConsumerIP", defaul_IP);
        P_Period = mPerferences.getInt("Period", defaul_status);
        P_Date = mPerferences.getString("Date", defaul_date);
    }

    private void saveUserStatus(int datastatu, int datatype, String consumerip, int period, String date) {
        SharedPreferences mPerferences = getApplicationContext().getSharedPreferences("SETTING_DATA", Activity.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mPerferences.edit();
        mEditor.putInt("DataStatus", datastatu);
        mEditor.putInt("DataType", datatype);
        mEditor.putString("ConsumerIP", consumerip);
        mEditor.putInt("Period", period);
        mEditor.putString("Date", date);
        mEditor.commit();
    }

    private void SendRealData(String consumeraddress, int DataType ){
        //if data ,if type=1,send ACC data, if type =2, send GPS data,if type= 3, send GPS AND ACC data
        //TODO parser consumeraddress to real consumer server address

        Log.d(TAG, "Start choose data to send");
        String URL=defaul_IP;
        String ACC="NO ACC DATA";
        String GPS="NO GPS DATA";
        switch(DataType) {
            case 1 :
                // Statements
                ACC=GetAcc_data();
                //SEND POST REQUEST
                DataToConsumer(ACC,GPS,URL);
                break; // optional
            case 2 :
                // Statements
                ACC=GetAcc_data();
                 GPS=GetGPS_data();
                //SEND POST REQUEST
                DataToConsumer(ACC,GPS,URL);
                break; // optional
            case 3 :
                // Statements
                 ACC=GetAcc_data();
                 GPS=GetGPS_data();
                //SEND POST REQUEST
                DataToConsumer(ACC,GPS,URL);
                break; // optional

            // You can have any number of case statements.
            default : // Optional
                // Statements
                Log.d(TAG, "SendRealData: sendnothing");
        }
    }

    private String GetAcc_data() {
        Cursor accelerometer_data = getContentResolver().query(Accelerometer_Provider.Accelerometer_Data.CONTENT_URI, null, "accuracy > 0", null, "timestamp DESC");
        accelerometer_data.moveToFirst();
        if (!(accelerometer_data.getString(0)).equals("")) {
            StringBuffer accdatabuffer = new StringBuffer("");
            //Acc_timestamp = accelerometer_data.getLong(5);
            //Deviece_id = accelerometer_data.getString(1);
            //saveDviceID(Deviece_id);
            for (int i = 0; i < 20; i++) {
                accdatabuffer.append(accelerometer_data.getColumnName(0) + ":" + accelerometer_data.getDouble(0) + ";");
                accdatabuffer.append(accelerometer_data.getColumnName(1) + ":" + accelerometer_data.getString(1) + ";");
                accdatabuffer.append(accelerometer_data.getColumnName(2) + ":" + accelerometer_data.getDouble(2) + ";");
                accdatabuffer.append(accelerometer_data.getColumnName(3) + ":" + accelerometer_data.getString(3) + ";");
                accdatabuffer.append(accelerometer_data.getColumnName(4) + ":" + accelerometer_data.getDouble(4) + ";");
                accdatabuffer.append(accelerometer_data.getColumnName(5) + ":" + accelerometer_data.getLong(5) + ";");
                accdatabuffer.append(accelerometer_data.getColumnName(6) + ":" + accelerometer_data.getString(6) + ";");
                accdatabuffer.append(accelerometer_data.getColumnName(7) + ":" + accelerometer_data.getString(7) + ";");
                accelerometer_data.moveToNext();
            }
            accelerometer_data.close();
            Log.d(TAG, "Get_Acc_data data buffer: " + accdatabuffer);
            return accdatabuffer.toString();
        } else {
           String NODATA="NO Accelerometer data NOW";
             Log.d(TAG, "Get_Acc_data: " + NODATA);
            return NODATA;
        }
    }

    private String GetGPS_data() {
        Cursor Gps_data = getContentResolver().query(Locations_Provider.Locations_Data.CONTENT_URI, null, "accuracy > 0", null, "timestamp DESC");
        Gps_data.moveToFirst();
        if (!(Gps_data.getString(0)).equals("")) {
            StringBuffer gpsdatabuffer = new StringBuffer("");
            for (int i = 0; i < 5; i++) {
                gpsdatabuffer.append(Gps_data.getColumnName(0) + ":" + Gps_data.getDouble(0) + ";");
                gpsdatabuffer.append(Gps_data.getColumnName(1) + ":" + Gps_data.getString(1) + ";");
                gpsdatabuffer.append(Gps_data.getColumnName(2) + ":" + Gps_data.getDouble(2) + ";");
                gpsdatabuffer.append(Gps_data.getColumnName(3) + ":" + Gps_data.getString(3) + ";");
                gpsdatabuffer.append(Gps_data.getColumnName(4) + ":" + Gps_data.getDouble(4) + ";");
                gpsdatabuffer.append(Gps_data.getColumnName(5) + ":" + Gps_data.getLong(5) + ";");
                gpsdatabuffer.append(Gps_data.getColumnName(6) + ":" + Gps_data.getDouble(6) + ";");
                gpsdatabuffer.append(Gps_data.getColumnName(7) + ":" + Gps_data.getString(7) + ";");
                gpsdatabuffer.append(Gps_data.getColumnName(8) + ":" + Gps_data.getDouble(8) + ";");
                gpsdatabuffer.append(Gps_data.getColumnName(9) + ":" + Gps_data.getString(9) + ";");
                gpsdatabuffer.append(Gps_data.getColumnName(10) + ":" + Gps_data.getString(10) + ";");
                Gps_data.moveToNext();
            }
            Gps_data.close();
            Log.d(TAG, "Get_GPS_data data buffer: " + gpsdatabuffer.toString());
            return  gpsdatabuffer.toString();

        } else {
            String NODATA="NO GPS data NOW";
            Log.d(TAG, "Get_GPS_data: " + NODATA);
            return NODATA;
        }
    }

    private void DataToConsumer(String ACC, String GPS, String URL){
        URL url = null;
        Log.d(TAG, "start send real sensor data to consumer");
        try {
            url = new URL(URL);
            Log.d(TAG, "url: " + url.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection connection = null;
        try {
            //Create connectioN
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ACC", ACC);
            jsonObject.put("GPS", GPS);
            String jsonstr = jsonObject.toString();
            // Log.d(TAG, "run jsonstr: " + jsonstr);

            byte[] req = jsonstr.getBytes(StandardCharsets.UTF_8);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Length", Integer.toString(req.length));
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            //Send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.write(req);
            wr.flush();
            wr.close();

            //Get Response
            int responseCode = connection.getResponseCode();
            if(responseCode==200){
                BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder(); // or StringBuffer if not Java 5+
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                    Log.d(TAG, "Response data: " + line);
                }
            }else{
                Log.d(TAG, "Send Real Data request Failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            //return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            } else {
                Log.e(TAG, "Url Null");
            }
        }

}



    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.arg1==1)
            {   //Print Toast
                Toast.makeText(getApplicationContext(), "Add new DATA success", Toast.LENGTH_SHORT).show();
            }
            if(msg.arg1==2)
            {Toast.makeText(getApplicationContext(), "Add new DATA failed, user NOT exist", Toast.LENGTH_SHORT).show();}
//            if(msg.arg1==3)
//            {Toast.makeText(getApplicationContext(), "Transaction waiting be mined", Toast.LENGTH_SHORT).show();}
            if(msg.arg1==4)
            {Toast.makeText(getApplicationContext(), "Send CheckReceipt request Failed", Toast.LENGTH_SHORT).show();}
            if(msg.arg1==5)
            {Toast.makeText(getApplicationContext(), "Send Add new DATA request failed, please try again!", Toast.LENGTH_SHORT).show();}
            if(msg.arg1==6)
            {Toast.makeText(getApplicationContext(), "Send check data status request failed, please try again!", Toast.LENGTH_SHORT).show();}
            if(msg.arg1==7)
            {Toast.makeText(getApplicationContext(), "Send Check data status Transaction Receipt request Failed", Toast.LENGTH_SHORT).show();}
            return false;
        }
    });

}
