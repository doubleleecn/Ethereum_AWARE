package com.boli.ethereum_aware;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

public class IntervalFragment extends Fragment implements NumberPicker.OnValueChangeListener, NumberPicker.OnScrollListener {

    OnNumberPickerListener mCallback;
    private NumberPicker mNumberPicker;
    private NumberPicker sNumberPicker;
    private Button save_button;
    private int m=0;
    private int s=2;
    private String defaul="0002";
    private String min;
    private String sec;
    private String result;


    // private OnFragmentInteractionListener mListener;

    public IntervalFragment() {
        // Required empty public constructor
    }

    public interface OnNumberPickerListener {
        public void onNumberPickerSelected(String result);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnNumberPickerListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnNumberPickerListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View t =  inflater.inflate(R.layout.fragment_interval, container, false);
        sNumberPicker = (NumberPicker) t.findViewById(R.id.S_Picker);
        mNumberPicker = (NumberPicker) t.findViewById(R.id.M_Picker);
        save_button = (Button) t.findViewById(R.id.interval_button);
        //initial the Min/Sec numberPicker
        getInterval();
        mNumberPicker.setMaxValue(30);
        mNumberPicker.setMinValue(0);
        mNumberPicker.setValue(m);
        mNumberPicker.setOnValueChangedListener(this);
        mNumberPicker.setOnScrollListener(this);

        sNumberPicker.setMaxValue(60);
        sNumberPicker.setMinValue(0);
        sNumberPicker.setValue(s);
        sNumberPicker.setOnValueChangedListener(this);
        sNumberPicker.setOnScrollListener(this);

        save_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 遍历list的长度，将已选的按钮设为未选
                m = mNumberPicker.getValue();
                if(m>10){
                     min = String.valueOf(m);
                }else {
                    min = "0"+String.valueOf(m);
                }
                s= sNumberPicker.getValue();
                if(s>10){
                    sec = String.valueOf(s);
                }else {
                    sec = "0"+String.valueOf(s);
                }
                result=min+sec;
                Log.d("Value", "sec: "+result);
                saveInterval("number",result);
                Toast toast = Toast.makeText(getActivity(), "saved",
                        Toast.LENGTH_LONG);
                toast.show();

                mCallback.onNumberPickerSelected(result);
             }
        });
        return t;
    }

    @Override
    public void onScrollStateChange(NumberPicker view, int scrollState) {

        switch (scrollState) {
            case NumberPicker.OnScrollListener.SCROLL_STATE_FLING:
                Log.d("State", "onScrollStateChange: scroll state fling ");
                break;
            case NumberPicker.OnScrollListener.SCROLL_STATE_IDLE:
                Log.d("State", "onScrollStateChange: scroll state idle ");
                break;
            case NumberPicker.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                Log.d("State", "onScrollStateChange: scroll state touch scroll ");
                break;
        }
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
//        Log.i("tag", "oldValue:" + oldVal + "   ; newValue: " + newVal);
    }


    public void getInterval()
    {
        SharedPreferences mPerferences = getActivity().getSharedPreferences("SETTING_DATA", Activity.MODE_PRIVATE);

        SharedPreferences m_private;
        m_private = this.getActivity().getPreferences(Context.MODE_PRIVATE);
        String counter=mPerferences.getString("number", defaul);//default value is 0
//        Log.d("counter", "GETINTERVAL: "+counter);
        if(counter!="0002") {
            m = Character.getNumericValue(counter.charAt(0)) * 10 + Character.getNumericValue(counter.charAt(1));
            s = Character.getNumericValue(counter.charAt(2)) * 10 + Character.getNumericValue(counter.charAt(3));
        }
    }

    //record required data
    public void saveInterval(String NUMBER,String data)
    {

        SharedPreferences mPerferences = getActivity().getSharedPreferences("SETTING_DATA", Activity.MODE_PRIVATE);
         //SharedPreferences mPerferences=PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences m_private=this.getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor=mPerferences.edit();
        mEditor.putString(NUMBER, data);
        mEditor.commit();
    }

}
