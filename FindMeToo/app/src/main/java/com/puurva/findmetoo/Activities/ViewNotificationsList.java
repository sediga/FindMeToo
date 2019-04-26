package com.puurva.findmetoo.Activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.puurva.findmetoo.Enums.ListViewTypes;
import com.puurva.findmetoo.Enums.NotificationType;
import com.puurva.findmetoo.Enums.RequestStatus;
import com.puurva.findmetoo.R;
import com.puurva.findmetoo.ServiceInterfaces.ApiInterface;
import com.puurva.findmetoo.ServiceInterfaces.model.ActivityModel;
import com.puurva.findmetoo.ServiceInterfaces.model.ActivityNotification;
import com.puurva.findmetoo.ServiceInterfaces.model.NotificationDetails;
import com.puurva.findmetoo.ServiceInterfaces.model.ProfileModel;
import com.puurva.findmetoo.ServiceInterfaces.model.ProfileReviewModel;
import com.puurva.findmetoo.preference.PrefConst;
import com.puurva.findmetoo.uitls.ActivitiesAdapter;
import com.puurva.findmetoo.uitls.Global;
import com.puurva.findmetoo.uitls.HttpClient;
import com.puurva.findmetoo.uitls.NotificationsAdapter;
import com.puurva.findmetoo.uitls.ReviewListViewAdapter;

import org.w3c.dom.Text;

import java.io.StringReader;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewNotificationsList extends Activity {
    // Array of strings...
    NotificationDetails[] notificationDetails = null;
    ProfileModel[] activitySubscribers = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications_list);

        String deviceId = getIntent().getStringExtra("DeviceId");
        LoadMyNotifications(deviceId);
    }

    @Override
    protected void onResume(){
        super.onResume();
        String deviceId = getIntent().getStringExtra("DeviceId");
        LoadMyNotifications(deviceId);
    }

    private void LoadMyNotifications(String deviceId) {
        final String token = getToken();
        ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        Call<List<NotificationDetails>> call = apiService.geMyNotifications("Bearer " + token, deviceId);
        try {
            call.enqueue(new Callback<List<NotificationDetails>>() {
                @Override
                public void onResponse(Call<List<NotificationDetails>> call, Response<List<NotificationDetails>> response) {
                    if (response.isSuccessful()) {
                        notificationDetails = new NotificationDetails[response.body().size()];
                        response.body().toArray(notificationDetails);
                        NotificationsAdapter adapter = new NotificationsAdapter(ViewNotificationsList.this, notificationDetails);

                        TextView headerText = (TextView) findViewById(R.id.title_notifications);
                        if(notificationDetails.length == 0){
                            headerText.setText("No notifications found");
                        } else {
                            headerText.setText(notificationDetails.length + " notifications found");
                        }
                        ListView listView = (ListView) findViewById(R.id.list_view_notifications);

                        listView.setAdapter(adapter);
                        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int position, long id) {
                                NotificationDetails notificationDetail = (NotificationDetails) parent.getItemAtPosition(position);
                                ActivityNotification activityNotification = null;
                                Intent intentClass = new Intent(ViewNotificationsList.this, MapsActivity.class);
                                intentClass.putExtra("NotificationId",notificationDetail.NotificationId);
                                if(notificationDetail.MessageObject != null){
                                    if(((LinkedTreeMap)notificationDetail.MessageObject).containsKey("FromDeviceId")){
                                        activityNotification = new ActivityNotification(notificationDetail.DeviceId, notificationDetail.ActivityId, RequestStatus.valueOf(((LinkedTreeMap) notificationDetail.MessageObject).get("NotificationRequestStatus").toString()), NotificationType.valueOf(((LinkedTreeMap) notificationDetail.MessageObject).get("RequestNotificationType").toString()));
                                        intentClass.putExtra("ActivityNotification", activityNotification);
                                    } else if(((LinkedTreeMap)notificationDetail.MessageObject).containsKey("What")){
                                        intentClass.putExtra("ActivityIdOfNotification", ((LinkedTreeMap)notificationDetail.MessageObject).get("ActivityID").toString());
                                    }
                                }
                                startActivity(intentClass);
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<List<NotificationDetails>> call, Throwable t) {
                }
            });
        }catch (Exception ex){
            Log.e("ReviewsDownload", ex.getMessage());
        }
    }

    private String getToken() {
        String username = Global.preference.getValue(this, PrefConst.USERNAME, "");
        String token = Global.preference.getValue(this, PrefConst.TOKEN, "");
        if (token == null || token == "") {
            token = getToken(username);
        }
        return token;
    }

    private String getToken(String username) {
        String token = null;
        Cursor c = Global.mdb.rawQuery(
                "SELECT *    " +
                        "FROM apiuser " +
                        "WHERE deviceid = '" + username + "' " +
                        "LIMIT 1",
                null);

        if (c == null || c.getCount() == 0) {
            Global.showShortToast(this, "Api User not found!");
            finish();
        } else {
            c.moveToFirst();
            token = c.getString(2);
        }
        return token;
    }

}
