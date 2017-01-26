package com.boli.ethereum_aware;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Timer;


public class User_Info extends Fragment {

    // TODO: Rename and change types of parameters
    private String TAG = "USER_INFO";
    private Button save_user_name;
    private TextView User_Name;
    private String username;
    private String P1, P2, P3;
    private String defaul = "User Name";
    private String httpUrl = "http://10.20.194.66:3000";

    // private OnFragmentInteractionListener mListener;

    public User_Info() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View f = inflater.inflate(R.layout.fragment_user__info, container, false);
        save_user_name = (Button) f.findViewById(R.id.save_user_name);
        User_Name = (TextView) f.findViewById(R.id.user_name);

        //initial textview
        getUsername();
        User_Name.setText(username);

        save_user_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //Toast.makeText(getContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                AlertDialog alertDialog;
                alertDialog = new AlertDialog.Builder(getContext()).create();
                alertDialog.setTitle("Create New User");
                alertDialog.setMessage("Do you want to create a new user?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "continue",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                username = User_Name.getText().toString();
                                Log.d(TAG, "onClick: continue create new user " + username);
                                saveUsername(username);
                                SendUserInfo(username);
                                dialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "cancle",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "onClick: cancled create new user ");
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });
        return f;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    private String getUsername() {
        SharedPreferences mPerferences = getActivity().getSharedPreferences("SETTING_DATA", Activity.MODE_PRIVATE);
        username = mPerferences.getString("UserName", defaul);//如果没有获取到的话默认是"User Name"
        return username;
    }

    private void saveUsername(String data) {
        SharedPreferences mPerferences = getActivity().getSharedPreferences("SETTING_DATA", Activity.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mPerferences.edit();
        mEditor.putString("UserName", data);
        mEditor.commit();
    }

    private static String byteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null) {
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

    private String AddZeroBefore(String param) {
        String result = param;
        for (int i = result.length(); i < 64; i++)
            result = "0" + result;
        return result;
    }

    private String AddZeroAfter(String param) {
        String result = param;
        for (int i = result.length(); i < 64; i++)
            result = result + "0";
        return result;
    }

    private void SendUserInfo(final String username) {
        //Log.d(TAG, "SendUserInfo: sendUserInfo" + username);
        new Thread(new Runnable() {
            @Override
            public void run() {
                //String httpUrl = "http://10.20.223.243:3000";
                String resultData = "";
                URL url = null;
                try {
                    url = new URL(httpUrl);
                    Log.d(TAG, "url: " + url.toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                HttpURLConnection connection = null;

                try {
                    //Create connection
                    String methodname = "0x506b8ca4";
                    SharedPreferences mPerferences = getActivity().getSharedPreferences("SETTING_DATA", Activity.MODE_PRIVATE);
                    P1 = "0000000000000000000000000000000000000000000000000000000000000020";
                    P2 = Integer.toString(username.length());

                    byte[] para5 = username.getBytes(StandardCharsets.UTF_8);
                    P3 = byteArrayToHexStr(para5);
                    //P1 = P1.replaceAll( "-|_","");
                    P2 = AddZeroBefore(P2);
                    P3 = AddZeroAfter(P3);
                    String params = methodname + P1 + P2 + P3;

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
                    jsonObject.put("id", 3);


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
                    Log.d(TAG, "ADD NEW USER TransactionHash: " + TransactionHash);
                    Thread.sleep(20000);
                    //check the transaction whether success
                    Log.d(TAG, "start check recipt");
                    CheckReceipt(TransactionHash);
                    }else{
                        Log.d(TAG, "Send Add new user request failed");
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

        }).start();
    }

    private void CheckReceipt(String hash) {
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
            jsonObject.put("id", 31);
            String jsonstr = jsonObject.toString();
            Log.d(TAG, "run jsonstr: " + jsonstr);

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
                    //Log.d(TAG, "CheckRecipt logs: "+Logs);
                    JSONArray Temlogdata = new JSONArray(Logs);
                    String LogData = Temlogdata.getString(0);
                    //Log.d(TAG, "CheckRecipt Temlogdata: "+LogData);
                    JSONObject resultLogdata = new JSONObject(LogData);
                    String logdata = resultLogdata.getString("data");
                    if(logdata.charAt(64)=='6'){
                        Log.d(TAG, "Add new user success");
                        Message meg = new Message();
                        meg.arg1=1;
                        handler.sendMessage(meg);
                    }else{
                        Log.d(TAG, "Add new user failed, user "+username+" already exist");
                        Message meg = new Message();
                        meg.arg1=2;
                        handler.sendMessage(meg);
                    }
                }else{
                    Log.d(TAG, "Transaction waiting be mined");
                    //Do your UI operations like dialog opening or Toast here
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

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.arg1==1)
            {   //Print Toast
                Toast.makeText(getContext(), "Add new user success", Toast.LENGTH_SHORT).show();
            }
            if(msg.arg1==2)
            {Toast.makeText(getContext(), "Add new user failed, user already exist", Toast.LENGTH_SHORT).show();}
            if(msg.arg1==3)
            {Toast.makeText(getContext(), "Transaction waiting be mined", Toast.LENGTH_SHORT).show();}
            if(msg.arg1==4)
            {Toast.makeText(getContext(), "Send CheckReceipt request Failed", Toast.LENGTH_SHORT).show();}
            if(msg.arg1==5)
            {Toast.makeText(getContext(), "Send Add new user request failed, please try again!", Toast.LENGTH_SHORT).show();}
            return false;
        }
    });
}
