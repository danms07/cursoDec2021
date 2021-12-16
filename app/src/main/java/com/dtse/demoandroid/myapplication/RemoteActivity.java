package com.dtse.demoandroid.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.huawei.agconnect.remoteconfig.AGConnectConfig;
import com.huawei.agconnect.remoteconfig.ConfigValues;

public class RemoteActivity extends AppCompatActivity {

    private TextView remoteText;
    private AGConnectConfig config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);
        remoteText=findViewById(R.id.remoteText);
         config= AGConnectConfig.getInstance();
         config.applyDefault(R.xml.remote_config);
         remoteText.setText(config.getValueAsString("text"));
        config.fetch(10).addOnSuccessListener((configValues)->onParameters(configValues));
    }

    private void onParameters(ConfigValues configValues) {
        config.apply(configValues);
        String text=config.getValueAsString("text");
        remoteText.setText(text);

        switch(text){
            case "hola": {
                //haz algo
            }

            case "otro":{
                //haz otra cosa
            }
        }
    }
}