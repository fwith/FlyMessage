package com.fwith.mobile.android.flymessage;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageSendFragment extends Fragment {
    EditText editTextMessage;
    EditText editTextPhoneNum;
    Button buttonSendSMS;
    SQLController dbcon;
    BroadcastReceiver sentReceiver;
    BroadcastReceiver deliveryReceiver;

    public MessageSendFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get tracker.
        Tracker t = ((BaseApplication) getActivity().getApplication()).getTracker(BaseApplication.TrackerName.APP_TRACKER);
        // Enable Advertising Features.
        t.enableAdvertisingIdCollection(true);
        t.setScreenName("MessageSendFragment");
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    @Override
    public void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(getActivity()).reportActivityStart(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(getActivity()).reportActivityStop(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_send, container, false);
        editTextMessage = (EditText) view.findViewById(R.id.editText);
        editTextPhoneNum = (EditText) view.findViewById(R.id.editText2);
        buttonSendSMS = (Button) view.findViewById(R.id.button);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.tool_bar);
        toolbar.setTitle("FlyMessage~");
        toolbar.setTitleTextColor(Color.WHITE);
        dbcon = new SQLController(getActivity());
        dbcon.open();

        buttonSendSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String smsNum = editTextPhoneNum.getText().toString();
                String smsText = editTextMessage.getText().toString();

                if (isValidCellPhoneNumber(smsNum)) {
                    if (smsNum.length() > 0 && smsText.length() > 0) {
                        sendSMS(smsNum, smsText);
                    } else {
                        Toast.makeText(getActivity(), "메시지와 전화번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "전화번호 규칙이 맞지 않습니다.", Toast.LENGTH_SHORT).show();
                }

                Tracker t = ((BaseApplication) getActivity().getApplication()).getTracker(BaseApplication.TrackerName.APP_TRACKER);
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("MessageSendFragment")
                        .setAction("ButtonSendSMS")
                        .setLabel("click").build());
            }
        });

        SharedPreferences pref = getActivity().getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editTextMessage.setText(pref.getString("smsText", ""));
        editTextPhoneNum.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        Button buttonDebug = (Button) view.findViewById(R.id.button2);
        buttonDebug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("android_prefs", Context.MODE_PRIVATE);
                Toast.makeText(getActivity(), "referrer_source ->> " + sharedPreferences.getString("referrer_source", ""), Toast.LENGTH_LONG).show();
                Toast.makeText(getActivity(), "referrer ->> " + sharedPreferences.getString("referrer", ""), Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        sentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        // 전송 성공
                        Toast.makeText(getActivity(), "Fly~ 성공", Toast.LENGTH_SHORT).show();
                        editTextPhoneNum.setText("");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        // 전송 실패
                        Toast.makeText(getActivity(), "Fly~ 실패", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        // 서비스 지역 아님
                        Toast.makeText(getActivity(), "서비스 지역이 아닙니다", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        // 무선 꺼짐
                        Toast.makeText(getActivity(), "무선(Radio)가 꺼져있습니다", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        // PDU 실패
                        Toast.makeText(getActivity(), "PDU Null", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        deliveryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        // 도착 완료
                        Toast.makeText(getActivity(), "SMS 도착 완료", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        // 도착 안됨
                        Toast.makeText(getActivity(), "SMS 도착 실패", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        getActivity().registerReceiver(sentReceiver, new IntentFilter("SMS_SENT_ACTION"));
        getActivity().registerReceiver(deliveryReceiver, new IntentFilter("SMS_DELIVERED_ACTION"));
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            getActivity().unregisterReceiver(sentReceiver);
            getActivity().unregisterReceiver(deliveryReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean isValidCellPhoneNumber(String cellphoneNumber) {
        boolean returnValue = false;
        String regex = "^\\s*(010|011|012|013|014|015|016|017|018|019)(-|\\)|\\s)*(\\d{3,4})(-|\\s)*(\\d{4})\\s*$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(cellphoneNumber);
        if (m.matches()) {
            returnValue = true;
        }
        return returnValue;
    }

    public void sendSMS(String smsNumber, String smsText) {
        Tracker t = ((BaseApplication) getActivity().getApplication()).getTracker(BaseApplication.TrackerName.APP_TRACKER);

        try {
            PendingIntent sentIntent = PendingIntent.getBroadcast(getActivity(), 0, new Intent("SMS_SENT_ACTION"), 0);
            PendingIntent deliveredIntent = PendingIntent.getBroadcast(getActivity(), 0, new Intent("SMS_DELIVERED_ACTION"), 0);

            dbcon.insertData(smsNumber, smsText);
            SharedPreferences pref = getActivity().getSharedPreferences("pref", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("smsText", smsText);
            editor.commit();

            SmsManager mSmsManager = SmsManager.getDefault();
            mSmsManager.sendTextMessage(smsNumber, null, smsText, sentIntent, deliveredIntent);

            t.send(new HitBuilders.EventBuilder()
                    .setCategory("MessageSendFragment")
                    .setAction("buttonSendSMS")
                    .setLabel("success").build());
        } catch (Exception e) {
            t.send(new HitBuilders.EventBuilder()
                    .setCategory("MessageSendFragment")
                    .setAction("buttonSendSMS")
                    .setLabel("fail").build());
        }
    }
}
