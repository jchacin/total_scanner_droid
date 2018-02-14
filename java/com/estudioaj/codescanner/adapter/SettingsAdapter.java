package com.estudioaj.codescanner.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Switch;
import android.widget.Toast;

import com.estudioaj.codescanner.R;
import com.estudioaj.codescanner.model.Setting;

import java.util.ArrayList;

/**
 * Created by JOSE CHACIN on 24/08/2017.
 */

public class SettingsAdapter extends BaseAdapter {
    AppCompatActivity context;
    SharedPreferences preferences;
    ArrayList<Setting> settings;

    public SettingsAdapter(AppCompatActivity context, SharedPreferences preferences, ArrayList<Setting> settings){
        this.context = context;
        this.preferences = preferences;
        this.settings = settings;
    }

    @Override
    public int getCount() {
        return settings.size();
    }

    @Override
    public Object getItem(int i) {
        return settings.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        try {
            LayoutInflater inflater = context.getLayoutInflater();
            View view = inflater.inflate(R.layout.adapter_item, null);

            Switch itemSwitch = (Switch)view.findViewById(R.id.item_switch);
            itemSwitch.setText(settings.get(position).getOption());
            if (settings.get(position).getSwitched())
                itemSwitch.setChecked(true);

            itemSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        //get option
                        Setting setting = settings.get(position);
                        SharedPreferences.Editor editor = preferences.edit();

                        //change setting selected
                        if (setting.getSwitched())
                            setting.setSwitched(false);
                        else
                            setting.setSwitched(true);

                        //save change
                        editor.putBoolean(setting.getKey(), setting.getSwitched());
                        editor.commit();
                    }catch (Exception ex){
                        Log.e("ADAPTER", ex.getMessage());
                    }
                }
            });

            return view;
        }catch (Exception ex){
            Log.e("ADAPTER", ex.getMessage());
            return convertView;
        }
    }
}
