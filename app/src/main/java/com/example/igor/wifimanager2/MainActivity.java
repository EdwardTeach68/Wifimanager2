package com.example.igor.wifimanager2;

import android.content.Context;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    String networkSSID = "Home_wifi_1";
    String networkPass = "09820639";

    Button Connect_btn;
    Button Disconnect_btn;
    public TextView con_txt,txtview;

    boolean tryConnect;

    WifiManager wifiManager;
    SupplicantState connectionInfo;
    int netId;

    MyTask mt;

    List<WifiConfiguration> WifiConfig;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Connect_btn = (Button) findViewById(R.id.connect_btn);
        con_txt = (TextView) findViewById(R.id.connectionInfo);
        txtview= (TextView) findViewById(R.id.textView2);
        Disconnect_btn = (Button) findViewById(R.id.Disconnect);

        wifiManager = (WifiManager)MainActivity.this.getSystemService(WIFI_SERVICE);

        Disconnect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Disconnect();
            }
        });

        Connect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectToWifi();
            }
        });

        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() { // Определяем задачу
            @Override
            public void run() {
                mt = new MyTask();
                mt.execute();
            };
        }, 0L, 2L * 1000);
    }

    public void ConnectToWifi(){
        wifiManager.removeNetwork(netId);
        tryConnect = true;
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", networkSSID);
        wifiConfig.preSharedKey = String.format("\"%s\"", networkPass);
        netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();

    }

    public String WiFiConnectionInfo(){
        connectionInfo = wifiManager.getConnectionInfo().getSupplicantState();
        if(connectionInfo.toString() == "COMPLETED")
            tryConnect=false;
        if(connectionInfo.toString() == "SCANNING" && !tryConnect)
            wifiManager.removeNetwork(netId);
        return connectionInfo.toString();

    }

    public void Disconnect(){
        wifiManager.disconnect();
        wifiManager.removeNetwork(netId);
    }

    class MyTask extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... noargs) {
            return WiFiConnectionInfo();
        }

        protected void  onPostExecute(String result) {
            con_txt.setText(result);
            Toast toast = Toast.makeText(getApplicationContext(),
                    result, Toast.LENGTH_SHORT);
            toast.show();
        }


    }
}
