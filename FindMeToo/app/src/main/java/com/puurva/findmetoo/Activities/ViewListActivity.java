package com.puurva.findmetoo.Activities;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.puurva.findmetoo.R;
import com.puurva.findmetoo.ServiceInterfaces.ApiInterface;
import com.puurva.findmetoo.ServiceInterfaces.model.ProfileReviewModel;
import com.puurva.findmetoo.preference.PrefConst;
import com.puurva.findmetoo.uitls.Global;
import com.puurva.findmetoo.uitls.HttpClient;
import com.puurva.findmetoo.uitls.ReviewListViewAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewListActivity extends Activity {
    // Array of strings...
    ProfileReviewModel[] profileReviewModels = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        String deviceId = getIntent().getStringExtra("DeviceId");

        if(deviceId != null && deviceId.length() > 0) {
            final String token = getToken();
            ApiInterface apiService =
                    HttpClient.getClient().create(ApiInterface.class);
            Call<List<ProfileReviewModel>> call = apiService.getProfileReviews("Bearer " + token, deviceId);
            try {
                call.enqueue(new Callback<List<ProfileReviewModel>>() {
                    @Override
                    public void onResponse(Call<List<ProfileReviewModel>> call, Response<List<ProfileReviewModel>> response) {
                        if (response.isSuccessful()) {
                            profileReviewModels = new ProfileReviewModel[response.body().size()];
                            response.body().toArray(profileReviewModels);
                            ReviewListViewAdapter adapter = new ReviewListViewAdapter(ViewListActivity.this, profileReviewModels);

                            ListView listView = (ListView) findViewById(R.id.list_view);
                            listView.setAdapter(adapter);

                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view,
                                                        int position, long id) {
                                    Toast.makeText(getApplicationContext(),
                                            "Click ListItem Number " + position, Toast.LENGTH_LONG)
                                            .show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ProfileReviewModel>> call, Throwable t) {
                    }
                });
            }catch (Exception ex){
                Log.e("ReviewsDownload", ex.getMessage());
            }

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