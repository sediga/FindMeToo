package com.puurva.findmetoo.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationDismissedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int notifiationIdotification = intent.getExtras().getInt("NotifiationId");
        /* Your code to handle the event here */
    }
}
