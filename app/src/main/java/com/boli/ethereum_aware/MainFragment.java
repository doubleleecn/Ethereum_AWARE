package com.boli.ethereum_aware;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    OnSensorPickListener mCallback;
    private ListView lv;
    private Adapter mAdapter;
    private ArrayList<String> list;
    private Button bt_selectall;
    private Button bt_cancel;
    private Button bt_deselectall;
    private Button bt_yes;
    private int checkNum; // 记录选中的条目数量
    private String defaul="";//默认的全部不勾选
    private String selectall="";//全部勾选

    public MainFragment() {
        // Required empty public constructor
    }

    public interface OnSensorPickListener {
        public void onSensorSelected(String result);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnSensorPickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnNumberPickerListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_main, container, false);
        View v =  inflater.inflate(R.layout.fragment_main, container, false);

        /* 实例化各个控件 */
    lv = (ListView) v.findViewById(R.id.lv);
    bt_selectall = (Button) v.findViewById(R.id.selectall);
    bt_cancel = (Button) v.findViewById(R.id.cancel);
    bt_deselectall = (Button) v.findViewById(R.id.inverseselect);
    bt_yes=(Button) v.findViewById(R.id.ok);
    list = new ArrayList<String>();
    initDate();

    //初始化勾选框信息，默认都是以未勾选为单位
    for(int n=0;n<list.size();n++)
    {
        defaul =defaul +"0";
        selectall= selectall +"1";
    }

    // 实例化自定义的MyAdapter
    mAdapter = new Adapter(list, this.getActivity());
    // 绑定Adapter
    lv.setAdapter(mAdapter);
    getCheck();//获取信息，也可说是初始化信息

//        // 全选按钮的回调接口
    bt_selectall.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // 遍历list的长度，将MyAdapter中的map值全部设为true
            for (int i = 0; i < list.size(); i++) {

                Adapter.getIsSelected().put(i, true);
            }
            // 数量设为list的长度
            checkNum = list.size();
            // 刷新listview和TextView的显示
            dataChanged();

        }
    });
    // 取消按钮的回调接口
    bt_cancel.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // 遍历list的长度，将已选的按钮设为未选
            Log.d("list", "CHECKnUM0: "+checkNum);
            for (int i = 0; i < list.size(); i++) {

                if (Adapter.getIsSelected().get(i)) {
                    Adapter.getIsSelected().put(i, false);
                    checkNum--;// 数量减1
//                    Log.d("list", "CHECKnUM1: "+checkNum);
                }
            }
            // 刷新listview和TextView的显示
            dataChanged();

        }
    });

    // 反选按钮的回调接口
    bt_deselectall.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // 遍历list的长度，将已选的设为未选，未选的设为已选
            for (int i = 0; i < list.size(); i++) {
                if (Adapter.getIsSelected().get(i)) {
                    Adapter.getIsSelected().put(i, false);
                    checkNum--;
                } else {
                    Adapter.getIsSelected().put(i, true);
                    checkNum++;
                }
            }
            // 刷新listview和TextView的显示
            dataChanged();
        }
    });

    //确定返回的按钮
    bt_yes.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            String str="";//确定后直接将信息写入preference保存以备下一次读取使用
            for(int i=0;i<list.size();i++)
            {
                if(Adapter.getIsSelected().get(i))
                {
                    str= str+'1';
                }
                else
                {
                    str = str+'0';
                }
            }
            saveCheck("sensor_check",str);//将数据已字符串形式保存起来，下次读取再用
            Toast toast = Toast.makeText(getActivity(), "saved",
                    Toast.LENGTH_LONG);
            toast.show();
            mCallback.onSensorSelected(str);
        }
    });

    //绑定listView的监听器
    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
        long arg3) {
            // 取得ViewHolder对象，这样就省去了通过层层的findViewById去实例化我们需要的cb实例的步骤
            ViewHolder holder = (ViewHolder) arg1.getTag();
            // 改变CheckBox的状态
            holder.cb.toggle();
            // 将CheckBox的选中状况记录下来
            Adapter.getIsSelected().put(arg2, holder.cb.isChecked());

            // 调整选定条目
            if (holder.cb.isChecked() == true) {
                checkNum++;
            } else {
                checkNum--;
            }

        }
    });
        return v;
}

    //得到保存在这个activity中的数据
    public void getCheck()
    {
        SharedPreferences mPerferences = getActivity().getSharedPreferences("SETTING_DATA", 0);
        //SharedPreferences mPerferences = PreferenceManager.getDefaultSharedPreferences(getActivity());//获取默认的preference
        //获取activity私有的preference
        SharedPreferences m_private;
        m_private = this.getActivity().getPreferences(Context.MODE_PRIVATE);
        String counter=mPerferences.getString("sensor_check", defaul);//如果没有获取到的话默认是0
//       Log.d("counter", "GETCHECK mainfragment: "+counter);
        for(int i=0;i<list.size();i++)
        {
            if(counter.charAt(i)=='1')
            {
                Adapter.getIsSelected().put(i, true);
            }
        }
    }

    //保存需要保存的数据
    public void saveCheck(String ID,String data)
    {
        //保存shuju
        SharedPreferences mPerferences = getActivity().getSharedPreferences("SETTING_DATA", 0);
       // SharedPreferences mPerferences=PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences m_private=this.getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor=mPerferences.edit();
        mEditor.putString(ID, data);
//        Log.d("saveCheck", "saveCheck mainfragment: "+data);
        mEditor.commit();
    }

    // 初始化数据
    private void initDate() {
        String name[]= { "Accelerometer", "Location"};
        for (int i = 0; i < name.length; i++) {
            list.add("Sensor:" + name[i]);
        }
    }

    // 刷新listview和TextView的显示
    private void dataChanged() {
        // 通知listView刷新
        mAdapter.notifyDataSetChanged();
        // TextView显示最新的选中数目
        //tv_show.setText("已选中" + checkNum + "项");  //这个功能还不完善，保存后再打开没把这个保存进去，会算错。
    }

}
