package com.example.netspeedindicator;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {

    private static final boolean SHOW_SPEED_IN_BITS = false;

    private TrafficSpeedMeasurer mTrafficSpeedMeasurer;
    private TextView mTextView, upSpeedView, downSpeedView;
    private Switch mASwitch;

    private boolean switchState;


    private static final long B = 1;
    private static final long KB = B * 1024;
    private static final long MB = KB * 1024;
    private static final long GB = MB * 1024;


    // Notification Channel, Builder, Manager

    private static final String Channel_ID = "net_speed_ind";
    private static final String Channel_name = "Net Speed ";
    private static final String Channel_desc = "Net Speed Notification Download and Upload";


    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(Channel_ID, Channel_name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(Channel_desc);
            channel.setSound(null,null);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        mTextView = findViewById(R.id.connection);
        mASwitch = findViewById(R.id.off_on_notif);
        upSpeedView = findViewById(R.id.up_speed);
        downSpeedView = findViewById(R.id.down_speed);

        mASwitch.setChecked(true);

        switchState = mASwitch.isChecked();

        mTrafficSpeedMeasurer = new TrafficSpeedMeasurer(TrafficSpeedMeasurer.TrafficType.ALL);
        mTrafficSpeedMeasurer.startMeasuring();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTrafficSpeedMeasurer.stopMeasuring();
    }

   /* @Override
    protected void onPause() {
        super.onPause();
        mTrafficSpeedMeasurer.removeListener(mStreamSpeedListener);
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        mTrafficSpeedMeasurer.registerListener(mStreamSpeedListener);
    }

    private ITrafficSpeedListener mStreamSpeedListener = new ITrafficSpeedListener() {

        @Override
        public void onTrafficSpeedMeasured(final double upStream, final double downStream) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String upStreamSpeed = Utils.parseSpeed(upStream, SHOW_SPEED_IN_BITS);
                    String downStreamSpeed = Utils.parseSpeed(downStream, SHOW_SPEED_IN_BITS);

                    //mTextView.setText("Up : " + upStreamSpeed + "\n\n" + "Down : " + downStreamSpeed);

                    upSpeedView.setText(upStreamSpeed);
                    downSpeedView.setText(downStreamSpeed);

                    displaySpeed(upStreamSpeed, downStreamSpeed, (int) downStream);


                }
            });
        }
    };


    private void displaySpeed(String upSpeed, String downSpeed, int downStream){

        //switch state if true then show notification

        switchState = mASwitch.isChecked();

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, Channel_ID)
                //.setAutoCancel(!switchState)
                .setShowWhen(false)
               // .setOngoing(switchState)
                .setContentTitle("Down : "+downSpeed + "          Up : "+upSpeed)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);




            // SHOW_SPEED_IN_BITS is set to false
            double value = SHOW_SPEED_IN_BITS ? downStream * 8 : downStream;

            if (value < KB) {
                mBuilder.setSmallIcon(R.drawable.wkb000);
            } else if (value < MB) {
                try{
                    mBuilder.setSmallIcon((int) (R.drawable.wkb000 + ((int)value/KB)));
                }catch (Exception e){
                    Log.e("Icon", "Error making Icon");
                }
                //mBuilder.setSmallIcon(R.drawable.wkb000);
                Log.e("KB : ", String.valueOf((int)value/KB));

            } else if (value < GB) {

                try{
                    mBuilder.setSmallIcon((int) (R.drawable.wkb000 + ((int)value/MB)));
                }catch (Exception e){
                    Log.e("Icon", "Error making Icon");
                }
                //mBuilder.setSmallIcon(R.drawable.wkb000);
                Log.e("KB : ", String.valueOf((int)value/MB));

            } else {
                mBuilder.setSmallIcon(R.drawable.wmb190);
            }

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);

        if(switchState){
            mBuilder.setAutoCancel(false);
            mBuilder.setOngoing(true);
            managerCompat.notify(1, mBuilder.build());
        }else{
            mBuilder.setAutoCancel(true);
            mBuilder.setOngoing(false);
            managerCompat.cancelAll();
        }
    }
}
