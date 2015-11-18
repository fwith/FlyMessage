package com.fwith.mobile.android.flymessage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by fwith on 15. 6. 20..
 */
public class InstallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String referrer = intent.getStringExtra("referrer");

        if (referrer != null) {
            SharedPreferences.Editor editor = context.getSharedPreferences("android_prefs", Context.MODE_PRIVATE).edit();
            editor.putString("referrer_source", referrer);
            editor.apply();
            trackReferrerAttributes(referrer, context);
        }
    }

    private void trackReferrerAttributes(String referrer, Context context) {
        try {
            referrer = URLDecoder.decode(referrer, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return;
        }

        if (referrer == null || referrer.equals("")) {
            return;
        }

        String utmsource = "";
        String utmmedium = "";
        String utmterm = "";
        String utmcontent = "";
        String utmcampaign = "";

        String[] uriTokens = referrer.split("&");
        for (int i = 0; i < uriTokens.length; i++) {
            String[] valTokens = uriTokens[i].split("=");
            switch (valTokens[0]) {
                case "utm_source":
                    utmsource = valTokens[1];
                    break;
                case "utm_medium":
                    utmmedium = valTokens[1];
                    break;
                case "utm_term":
                    utmterm = valTokens[1];
                    break;
                case "utm_content":
                    utmcontent = valTokens[1];
                    break;
                case "utm_campaign":
                    utmcampaign = valTokens[1];
                    break;
            }
        }

        SharedPreferences.Editor editor = context.getSharedPreferences("android_prefs", Context.MODE_PRIVATE).edit();
        editor.putString("referrer",
                        "utmsource : " + utmsource
                        + ", utmmedium : " + utmmedium
                        + ", utmterm : " + utmterm
                        + ", utmcontent : " + utmcontent
                        + ", utmcampaign : " + utmcampaign);
        editor.apply();
    }
}
