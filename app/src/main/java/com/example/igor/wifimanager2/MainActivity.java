package com.example.igor.wifimanager2;


import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;




public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    ArrayList<Wifi> WifiList = new ArrayList<>();
    Button Connect_btn;
    Button Disconnect_btn;
    TextView con_txt;

    boolean tryConnect,isStarted;
    WifiManager wifiManager;
    SupplicantState connectionInfo;
    int netId,prevNetId=-1;

    public int BestWifiID;
    int rssiLevel;

    MyTask mt;

    List<ScanResult> ScanList = new ArrayList<>();
    ScanResult ScanElement;


    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    public String mUsername,mPhotoUrl,mUserEmail,mUserId;

    private static final int NOTOFOCANION_ID =132 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Wifi NewWiFi = new Wifi("Home_Wi-Fi","09820639");
        WifiList.add(NewWiFi);
        NewWiFi = new Wifi("Home_wifi_1","09820639");
        WifiList.add(NewWiFi);
        NewWiFi = new Wifi("DIR-300_15/1","13198155");
        WifiList.add(NewWiFi);
        NewWiFi = new Wifi("AlexDev's iPhone","asdfghjkl");
        WifiList.add(NewWiFi);
        NewWiFi = new Wifi("IPhone Роман","Bigeconomy12");
        WifiList.add(NewWiFi);
        NewWiFi = new Wifi("MEIZU MX3","asdfghjkl");
        WifiList.add(NewWiFi);
        NewWiFi = new Wifi("Lg","asdfghjkl");
        WifiList.add(NewWiFi);

        Connect_btn = (Button) findViewById(R.id.connect_btn);
        Disconnect_btn = (Button) findViewById(R.id.Disconnect);
        con_txt = (TextView) findViewById(R.id.textView3) ;

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
                isStarted=true;
            }
        });

        auth();
        getWifiPremission();

        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mt = new MyTask();
                mt.execute();
            }
        }, 0L, 2L * 1000);
    }

    //region [Google Auth]
    private void auth(){
            mFirebaseDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API)
                    .build();

            mFirebaseAuth = FirebaseAuth.getInstance();
            mFirebaseUser = mFirebaseAuth.getCurrentUser();
            if (mFirebaseUser == null) {
                startActivity(new Intent(this, AuthActivity.class));
                finish();
                return;
            } else {
                mUsername = mFirebaseUser.getDisplayName();
                if (mFirebaseUser.getPhotoUrl() != null) {
                    mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
                }
                mUserEmail = mFirebaseUser.getEmail();
                mUserId = mFirebaseUser.getUid();
                CheckUserExist();
            }

    }
    private void CheckUserExist(){

        mFirebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(mUserId)){
                    mFirebaseDatabase.child(mUserId).child("img_url").setValue(mPhotoUrl);
                    mFirebaseDatabase.child(mUserId).child("email").setValue(mUserEmail);
                    mFirebaseDatabase.child(mUserId).child("name").setValue(mUsername);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    //endregion



    private void getWifiPremission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        12345);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                            String permissions[],int[] grantResults) {
        if(requestCode == 12345) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "PERMISSION_GRANTED",
                        Toast.LENGTH_SHORT).show();
                // permission was granted, yay! Do the
                // contacts-related task you need to do.

            } else {

                Toast.makeText(this, "Hren",
                        Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void Scan(){
        wifiManager.startScan();
        ScanList = wifiManager.getScanResults();
        if(connectionInfo.toString() !="COMPLETED" && !tryConnect){
            rssiLevel =0;
            FindBestConnection();
        }

        if(connectionInfo.toString() =="COMPLETED"){
            rssiLevel = WifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi(), 5);
            if(rssiLevel<2)
                FindBestConnection();
        }

    }

    public void FindBestConnection(){
       for (int i = 0;i<ScanList.size()-1;i++) {
            if(WifiManager.calculateSignalLevel(ScanList.get(i).level, 5)<WifiManager.calculateSignalLevel(ScanList.get(i+1).level, 5)){
                ChekWifiExist(i+1);
            }else{
                ChekWifiExist(i);
            }

        }
        if(!WifiList.get(BestWifiID).networkSSID.equals(wifiManager.getConnectionInfo().getSSID()) && WifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi(), 5)<WifiManager.calculateSignalLevel(ScanElement.level,5))
            ConnectToWifi(WifiList.get(BestWifiID).networkSSID,WifiList.get(BestWifiID).networkPass);

    }

    public void ChekWifiExist(int id){
        for(int i=0;i<WifiList.size();i++){
            if(ScanList.get(id).SSID.equals(WifiList.get(i).networkSSID)){
                ScanElement = ScanList.get(id);
                BestWifiID = i;
            }
        }
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
        connectionInfo = wifiManager.getConnectionInfo().getSupplicantState();
        if(wifiManager.isWifiEnabled()) {

            if (connectionInfo.toString() == "COMPLETED") {
                showNotification("Подключено к: " + wifiManager.getConnectionInfo().getSSID());
                tryConnect = false;
            }
            if (connectionInfo.toString() == "SCANNING" && !tryConnect) {
                showNotification("Поиск сети...");
                wifiManager.removeNetwork(netId);
            }
        }else{
            showNotification("Wi-Fi отключен");

        }
        return connectionInfo.toString();
    }

    public void WiFiState(){

        if(!wifiManager.isWifiEnabled()){
            prevNetId = netId;


        }
        if(wifiManager.isWifiEnabled()){
            if(prevNetId !=-1) {
                wifiManager.removeNetwork(prevNetId);
                prevNetId = -1;
            }

            wifiManager.startScan();
        }
    }

    public void Disconnect(){
        isStarted = false;
        showNotification("Stoped");
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
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.wifi_icon))
                .setTicker("WiFi Manager")
                .setWhen(System.currentTimeMillis())
                .setContentTitle("WiFi Manager")
                .setContentText(title)
                .setOngoing(true)
        ;
        /*Intent yesReceive = new Intent();
        yesReceive.setAction("STOP_ACTION");
        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(this, 12345, yesReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.mipmap.ic_launcher, "Stop", pendingIntentYes);*/
        android.app.Notification notification = builder.build();
        notificationManager.notify(NOTOFOCANION_ID, notification);

    }


    private class MyTask extends AsyncTask<Void, Void, String> {


        @Override
        protected String doInBackground(Void... noargs) {
            if(isStarted) {
                WiFiConnectionInfo();
                WiFiState();
                Scan();
            }
            return "WP";
        }

        protected void  onPostExecute(String result) {
            con_txt.setText(" "+WifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi(),5));
            /*Toast toast = Toast.makeText(getApplicationContext(),
                    result, Toast.LENGTH_SHORT);
            toast.show();*/
        }


    }
}

