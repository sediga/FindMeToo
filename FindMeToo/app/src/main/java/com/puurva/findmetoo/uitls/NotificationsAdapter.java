package com.puurva.findmetoo.uitls;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.puurva.findmetoo.R;
import com.puurva.findmetoo.ServiceInterfaces.model.ActivityModel;
import com.puurva.findmetoo.ServiceInterfaces.model.NotificationDetails;

public class NotificationsAdapter extends ArrayAdapter<NotificationDetails> {
    private final Context context;
    private final NotificationDetails[] values;

    public NotificationsAdapter(Context context, NotificationDetails[] values) {
        super(context, R.layout.notification_details, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.notification_details, parent, false);
        TextView textNotification = (TextView) rowView.findViewById(R.id.text_notification);
        TextView textNotificationTime = (TextView) rowView.findViewById(R.id.notification_time);
        TextView textNotificationStatus = (TextView) rowView.findViewById(R.id.notification_status);
        textNotification.setText(values[position].NotificationText);
        textNotificationTime.setText(values[position].CreatedOn.toString());
        if(values[position].MessageStatus != null) {
            textNotificationStatus.setText(values[position].MessageStatus.toString());
        }
        return rowView;
    }
}
