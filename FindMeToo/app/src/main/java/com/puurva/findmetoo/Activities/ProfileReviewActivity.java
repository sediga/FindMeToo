package com.puurva.findmetoo.Activities;

import android.database.Cursor;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.puurva.findmetoo.Enums.RequestStatus;
import com.puurva.findmetoo.R;
import com.puurva.findmetoo.ServiceInterfaces.model.ActivityNotification;
import com.puurva.findmetoo.ServiceInterfaces.model.ProfileModel;
import com.puurva.findmetoo.ServiceInterfaces.model.ProfileReviewModel;
import com.puurva.findmetoo.preference.PrefConst;
import com.puurva.findmetoo.uitls.CommonUtility;
import com.puurva.findmetoo.uitls.Global;

public class ProfileReviewActivity extends AppCompatActivity implements View.OnClickListener {
        //implements View.OnClickListener {

    private String imageFilePath;
    private String deviceID;
//    private String activityId;
    private ActivityNotification activityNotification;
    private ProfileModel profileModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_profile);

//        deviceID = getIntent().getStringExtra("DeviceID");
        profileModel = getIntent().getParcelableExtra("ProfileModel");
        Button saveReview = this.findViewById(R.id.btn_review_save);
        Button cancelReview = this.findViewById(R.id.btn_review_cancel);
        saveReview.setOnClickListener(this);
        cancelReview.setOnClickListener(this);

        //        activityId = getIntent().getStringExtra("ActivityID");

        if(profileModel != null) {
            fillProfile();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            event.startTracking();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
                && !event.isCanceled()) {
            setIntent(null);
            finish();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch (id) {
            case R.id.btn_review_save:
                saveReview(RequestStatus.REJECTED);
                finish();
                break;
            case R.id.btn_review_cancel:
                finish();
                break;
        }

    }

    private void saveReview(RequestStatus requestStatus) {
        final String token = getToken();
        RatingBar ratingBar = this.findViewById(R.id.edit_profile_rating);
        EditText txtReview = this.findViewById(R.id.txt_profile_review);
        try {
            ProfileReviewModel profileReviewModel =
                    new ProfileReviewModel(Global.AndroidID, profileModel.getDeviceId(), Global.current_user.getUsername(),
                            txtReview.getText().toString(), ratingBar.getRating());
            CommonUtility.PostProfileReview(token, profileReviewModel);
        } catch (Exception ex) {
            Log.e("ProfileReview", ex.getMessage());
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

    private void fillProfile() {
        final String token = getToken();
        final TextView txtProfileName = ((TextView) findViewById(R.id.txt_view_name));
        final TextView txtHobies = ((TextView) findViewById(R.id.txt_view_hobies));
        final TextView txtProfileReview = ((TextView) findViewById(R.id.txt_profile_review));
        final TextView txtReviews = ((TextView) findViewById(R.id.txt_view_reviews));
        final TextView txtViews = ((TextView) findViewById(R.id.txt_view_views));
        final ImageView image1 = ((ImageView) findViewById(R.id.imgViewPhoto));
        txtProfileName.setText(profileModel.getProfileName());
        txtHobies.setText(profileModel.getHobies());
        txtReviews.setText(txtReviews.getText() + " : " + ((Long) profileModel.getReviews()).toString());
        txtViews.setText(txtViews.getText() + " : " + ((Long) profileModel.getViews()).toString());
        image1.setMaxWidth(profileModel.getProfilePhoto().getWidth());
        image1.setMaxHeight(profileModel.getProfilePhoto().getHeight());
        image1.setImageBitmap(profileModel.getProfilePhoto());
    }

}
