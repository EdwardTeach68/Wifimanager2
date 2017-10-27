package com.example.igor.wifimanager2;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;




public class MainActivity extends AppCompatActivity {
    String networkSSID = "Arounda";
    String networkPass = "27101996";
    ArrayList<Wifi> WifiList = new ArrayList<Wifi>();
    Button Connect_btn;
    Button Disconnect_btn;
    public TextView con_txt,txtview;

    boolean tryConnect,wifiEanabled;
    ArrayList<HashMap<String, String>> arraylist = new ArrayList<HashMap<String, String>>();
    WifiManager wifiManager;
    SupplicantState connectionInfo;
    int netId,prevNetId;
    int size = 0;
    MyTask mt;

    List<ScanResult> ScanList = new ArrayList<>();

    private static final int NOTOFOCANION_ID =132 ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Wifi NewWiFi = new Wifi("Arounda","27101996");
        WifiList.add(NewWiFi);
        NewWiFi = new Wifi("DIR-300_15/1","13198155");
        WifiList.add(NewWiFi);
        NewWiFi = new Wifi("AlexDev's iPhone","asdfghjkl");
        WifiList.add(NewWiFi);

        Connect_btn = (Button) findViewById(R.id.connect_btn);
        con_txt = (TextView) findViewById(R.id.connectionInfo);
        txtview= (TextView) findViewById(R.id.textView2);
        Disconnect_btn = (Button) findViewById(R.id.Disconnect);

        wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);

        Disconnect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Disconnect();
            }
        });

        Connect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Scan();




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

        registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context c, Intent intent)
            {
                ScanList = wifiManager.getScanResults();
                size = ScanList.size();
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));


    }

    public void Scan(){
        /*wifiManager.startScan();
        ScanList = wifiManager.getScanResults();
        for (ScanResult scanResult : ScanList) {
            int level = WifiManager.calculateSignalLevel(scanResult.level, 5);
            txtview.setText(scanResult.SSID);
            if(level>3 && WiFiConnectionInfo() !="COMPLETED" ){
              if(scanResult.SSID == WifiList.get(0).networkSSID)
                  ConnectToWifi(scanResult.SSID,WifiList.get(0).networkPass);
            }
        }

        for (ScanResult result : ScanList) {
            Toast.makeText(this, result.SSID + " " + result.level,
                    Toast.LENGTH_SHORT).show();
        }*/
        arraylist.clear();
        wifiManager.startScan();

        Toast.makeText(this, "Scanning...." + size, Toast.LENGTH_SHORT).show();

    }

    public void ConnectToWifi(String ssid,String pass){
        wifiManager.removeNetwork(netId);
        tryConnect = true;
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        wifiConfig.preSharedKey = String.format("\"%s\"", pass);
        netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();

    }

    public String WiFiConnectionInfo(){
        if(wifiManager.isWifiEnabled()) {
            connectionInfo = wifiManager.getConnectionInfo().getSupplicantState();
            if (connectionInfo.toString() == "COMPLETED") {
                showNotification("Подключенно к: " + wifiManager.getConnectionInfo().getSSID());
                tryConnect = false;
            }
            if (connectionInfo.toString() == "SCANNING" && !tryConnect) {
                showNotification("Поиск сети");
                wifiManager.removeNetwork(netId);
            }
        }
        return connectionInfo.toString();
    }

    public void WiFiState(){
        if(!wifiManager.isWifiEnabled()){
            prevNetId = netId;

        }
        if(wifiManager.isWifiEnabled()){
            if(prevNetId!=0) {
                wifiManager.removeNetwork(prevNetId);
                prevNetId = 0;
            }

            wifiManager.startScan();
        }


    }

    public void Disconnect(){
        wifiManager.disconnect();
        wifiManager.removeNetwork(netId);
        
    }


    public void showNotification(String title){
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher))
                .setTicker("WiFi Manager - подключен")
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle("WiFi Manager")
                .setContentText(title)
               // .setSound(Uri.parse("android.resource://com.example.alexdev.notificationtutor/" + R.raw.calltoprayer2))
                //.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
        ;
        android.app.Notification notification = builder.build();
        notificationManager.notify(NOTOFOCANION_ID, notification);
    }
    class MyTask extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... noargs) {
            WiFiState();
            return WiFiConnectionInfo();
        }

        protected void  onPostExecute(String result) {
            con_txt.setText(result);
            Toast toast = Toast.makeText(getApplicationContext(),
                    result, Toast.LENGTH_SHORT);
            //toast.show();
        }


    }
}
