package com.example.igor.wifimanager2;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> Networkssid = new ArrayList<>();
    ArrayList<String> networkPass = new ArrayList<>();
    Context context;
    Button Connect_btn;
    Button Disconnect_btn;
    TextView SSID_txt,PASS_txt;
    WifiInfo connectionInfo;
    int netId;
    int i=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Connect_btn = (Button) findViewById(R.id.connect_btn);

        Disconnect_btn = (Button) findViewById(R.id.Disconnect);
        SSID_txt = (TextView) findViewById(R.id.SSID);
        PASS_txt = (TextView) findViewById(R.id.Pass);
        Disconnect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Disconnect();
            }
        });

        Connect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetWifiInfo();
            }
        });
        Networkssid.add("Arounda");
        networkPass.add("27101996");

        Networkssid.add("Arounda 5GHz");
        networkPass.add("27101996");

        Networkssid.add("DIR-300_15/1");
        networkPass.add("13198155");

        Networkssid.add("ATIS2");
        networkPass.add("atis1996");
        Networkssid.add("Salon2");
        networkPass.add("22222222");
        Networkssid.add("Riposino");
        networkPass.add("09888877788");

        SSID_txt.setText(Networkssid.get(0));
        PASS_txt.setText(networkPass.get(0));


    }

    public void SetWifiInfo(){

        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", Networkssid.get(i));
        wifiConfig.preSharedKey = String.format("\"%s\"", networkPass.get(i));
        SSID_txt.setText(Networkssid.get(i));
        PASS_txt.setText(networkPass.get(i));

        WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
        connectionInfo = wifiManager.getConnectionInfo();
        if(connectionInfo==null){

            wifiManager.disconnect();
            wifiManager.removeNetwork(netId);
            i++;
        }
    }

    public void Disconnect(){
        WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        wifiManager.disconnect();
        wifiManager.removeNetwork(netId);
        i++;
        SSID_txt.setText(Networkssid.get(i));
        PASS_txt.setText(networkPass.get(i));
    }
}
