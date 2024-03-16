package com.example.textmessagingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

public class OmJanamanchi extends AppCompatActivity
{

    Switch switchMode;
    EditText phoneNumber;
    TextView text_message;
    EditText input_text;
    ImageButton send_button;
    IntentFilter intentFilter;

    private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TextView inText = (TextView) findViewById(R.id.text_message);
            inText.setText(intent.getExtras().getString("message"));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        switchMode = findViewById(R.id.switchMode);
        phoneNumber = findViewById(R.id.phoneNumber);
        text_message = findViewById(R.id.text_message);
        input_text = findViewById(R.id.input_text);
        send_button = findViewById(R.id.send_button);
        intentFilter = new IntentFilter();

        intentFilter.addAction("SMS_RECEIVED_ACTION");

        switchMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent aiApp = new Intent(OmJanamanchi.this, AIAPP.class);
                startActivity(aiApp);
            }
        });

        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String myMsg = input_text.getText().toString();
                String theNumber = text_message.getText().toString();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        sendMessage(theNumber, myMsg);
                    }
                }, 3000);


            }
        });
    }

    protected void sendMessage(String theNumber, String myMsg)
    {
        String SENT = "Message Sent";
        String DELIVERED = "Message Delivered";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(theNumber, null, myMsg, sentPI, deliveredPI);
    }

    @Override
    protected void onResume()
    {
        registerReceiver(intentReceiver, intentFilter);
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        unregisterReceiver(intentReceiver);
        super.onPause();
    }


}