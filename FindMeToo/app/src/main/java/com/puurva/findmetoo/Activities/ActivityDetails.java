package com.puurva.findmetoo.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TimePicker;

import com.puurva.findmetoo.Enums.ActivityTypes;
import com.puurva.findmetoo.R;
import com.puurva.findmetoo.ServiceInterfaces.ApiInterface;
import com.puurva.findmetoo.ServiceInterfaces.model.ActivityModel;
import com.puurva.findmetoo.ServiceInterfaces.model.ProfileModel;
import com.puurva.findmetoo.preference.PrefConst;
import com.puurva.findmetoo.uitls.CommonUtility;
import com.puurva.findmetoo.uitls.Global;
import com.puurva.findmetoo.uitls.HttpClient;
import com.puurva.findmetoo.uitls.ImageUtility;
import com.puurva.findmetoo.uitls.ProfileListAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityDetails extends AppCompatActivity implements View.OnClickListener {
    ImageView image = null;
    ActivityModel activity;
    Uri capturedImageUri = null;
    public static final int CAMERA_ACTIVITY = 1;
    public static final int GALLERY_ACTIVITY = 0;
    private Bitmap bitmap = null;

    ViewGroup.LayoutParams settingsLayoutParams;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_keyword);

        final EditText edit_title = this.findViewById(R.id.edit_activity_title);
        final EditText edit_description = this.findViewById(R.id.edit_activity_description);
        image = this.findViewById(R.id.edit_activity_badge);
        final String token = Global.preference.getValue(this, PrefConst.TOKEN, "");
        activity = getIntent().getParcelableExtra("Activity");
        if(Global.CurrentImage != null){
            image.setImageBitmap(Global.CurrentImage);
        } else {
            image.setImageResource(R.drawable.takeaphoto);
        }
        if(activity != null){
            edit_title.setText(activity.What);
            edit_title.setInputType(InputType.TYPE_NULL);
            edit_description.setText(activity.description);
            LinearLayout layoutEditActions = this.findViewById(R.id.activity_edit_actions);
            layoutEditActions.setVisibility(View.VISIBLE);
            Button btnDelete = layoutEditActions.findViewById(R.id.btn_activity_delete);
            Button btnCancel = layoutEditActions.findViewById(R.id.btn_activity_cancel);
            Button btnUpdate = layoutEditActions.findViewById(R.id.btn_activity_update);
            btnDelete.setOnClickListener(this);
            btnCancel.setOnClickListener(this);
            btnUpdate.setOnClickListener(this);
            final Button btnSettings = this.findViewById(R.id.btn_activity_settings);
            final LinearLayout activitySettings = this.findViewById(R.id.activity_settings);
            final LinearLayout parentLayout = this.findViewById(R.id.add_dialog_top);
            btnSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if(btnSettings.getText().equals("SETTINGS")) {
                            btnSettings.setText("HIDE");
                            activitySettings.setVisibility(View.VISIBLE);
                        }else if(btnSettings.getText().equals("HIDE")){
                            btnSettings.setText("SETTINGS");
                            activitySettings.setVisibility(View.GONE);
                        }
                    } catch (Exception ex) {
                        Log.e("cameraintenterror", ex.getMessage());
                    }
                }
            });

            Button pickPhoto = this.findViewById(R.id.button_pick);
            HandleMoreSettings();
            this.findViewById(R.id.layout_subscribers).setVisibility(View.VISIBLE);
            LoadActivitySubscribers(activity.ActivityID);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File imageFile = new File(CommonUtility.GetImageFileFullPath());
                        capturedImageUri = Uri.fromFile(imageFile);
                        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
                        startActivityForResult(takePicture, CAMERA_ACTIVITY);
                    } catch (Exception ex) {
                        Log.e("cameraintenterror", ex.getMessage());
                    }
                }
            });

            pickPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        startActivityForResult(photoPickerIntent, GALLERY_ACTIVITY);
                    } catch (Exception ex) {
                        Log.e("cameraintenterror", ex.getMessage());
                    }
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            String filePath = CommonUtility.GetImageFileFullPath();
            File file = new File(filePath);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;
            switch (requestCode) {
                case CAMERA_ACTIVITY: {
                    if (resultCode == Activity.RESULT_OK) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        float angle = ImageUtility.getExifAngle(this, filePath);
                        bitmap = ImageUtility.rotateImage(filePath, angle);
                        bitmap = ImageUtility.scaleImageToResolution(this.bitmap, 300, 200, file);
                    }
                }
                break;
                case GALLERY_ACTIVITY: {
                    if (resultCode == RESULT_OK) {
                        Uri imageUri = data.getData();
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                        bitmap = ImageUtility.scaleImageToResolution(this.bitmap, 300, 300, file);
                    }
                }
                break;
            }

            if (image != null && bitmap != null) {
//                bitmap = ImageUtility.scaleImageToResolution(this.bitmap, image.getWidth(), image.getWidth());
                image.setImageBitmap(bitmap);
            }

        }catch (Exception ex){
            Log.e("imageselector", ex.getMessage());
        }
    }

    private void LoadActivitySubscribers(String activityId) {
        final String token = getToken();
        try {
            Intent profileReviewsIntent = new Intent(this, ViewListActivity.class);
            ApiInterface apiService =
                    HttpClient.getClient().create(ApiInterface.class);
            Call<List<ProfileModel>> call = apiService.getActivitySubscribers("Bearer " + token, activityId);
            call.enqueue(new Callback<List<ProfileModel>>() {
                @Override
                public void onResponse(Call<List<ProfileModel>> call, Response<List<ProfileModel>> response) {
                    if(response.isSuccessful()) {
                        ProfileModel[] activitySubscribers = new ProfileModel[response.body().size()];
                        response.body().toArray(activitySubscribers);
                        if(activitySubscribers!=null) {
                            ProfileListAdapter adapter = new ProfileListAdapter(ActivityDetails.this, activitySubscribers);

                            ListView listView = (ListView) findViewById(R.id.list_view_subscribers);
                            listView.setAdapter(adapter);
                            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view,
                                                        int position, long id) {
                                        Intent profileIntent = new Intent(ActivityDetails.this, ProfileViewActivity.class);
                                        if(parent.getItemAtPosition(position) != null) {
                                            profileIntent.putExtra("ProfileModel", ((ProfileModel)parent.getItemAtPosition(position)));
                                        }
                                        startActivity(profileIntent);

                                }
                            });
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<ProfileModel>> call, Throwable t) {
                    Log.e("LoadActicitySubscribers", t.getMessage(), t);
                }
            });
        }catch (Exception ex){
            Log.e("ReviewsDownload", ex.getMessage());
            ShowError();
        }
    }

    private void HandleMoreSettings() {
        try {
            final EditText startDate = (EditText) this.findViewById(R.id.activity_start_date);
            final EditText endDate = (EditText) this.findViewById(R.id.activity_end_date);
            final Switch isPrivate = (Switch) findViewById(R.id.activity_is_private);
            startDate.setKeyListener(null);
            endDate.setKeyListener(null);
            isPrivate.setChecked(activity.activitySetting.ActivityType == ActivityTypes.ONREQUEST ? true : false);
            isPrivate.setVisibility(View.GONE);

            Date startDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(activity.activitySetting.StartTime);
            Date endDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(activity.activitySetting.EndTime);
            String formattedFromDate = Global.activityDateFormat.format(startDateTime);
            startDate.setText(formattedFromDate);
            startDate.setTag(Global.universalDateFormat.format(startDateTime));
//        startCalander.add(Calendar.HOUR, 4);
            String formattedToDate = Global.activityDateFormat.format(endDateTime);
            endDate.setText(formattedToDate);
            endDate.setTag(Global.universalDateFormat.format(endDateTime));

            final Calendar startCalander = Calendar.getInstance();
            final Calendar endCalander = Calendar.getInstance();
            startCalander.setTime(startDateTime);
            endCalander.setTime(endDateTime);
            final DatePickerDialog.OnDateSetListener startTimePicker = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    // TODO Auto-generated method stub
                    startCalander.set(Calendar.YEAR, year);
                    startCalander.set(Calendar.MONTH, monthOfYear);
                    startCalander.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // now show the time picker
                    new TimePickerDialog(ActivityDetails.this,
                            new TimePickerDialog.OnTimeSetListener() {

                                @Override
                                public void onTimeSet(TimePicker view,
                                                      int h, int min) {
                                    startCalander.set(Calendar.HOUR_OF_DAY, h);
                                    startCalander.set(Calendar.MINUTE, min);
//                                date = startCalander.getTime();

                                    startDate.setText(Global.activityDateFormat.format(startCalander.getTime()));
                                    startDate.setTag(Global.universalDateFormat.format(startCalander.getTime()));
                                }
                            }, startCalander.get(Calendar.HOUR_OF_DAY),
                            startCalander.get(Calendar.MINUTE), false).show();

                }

            };
            final DatePickerDialog.OnDateSetListener endTimePicker = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    // TODO Auto-generated method stub
                    endCalander.set(Calendar.YEAR, year);
                    endCalander.set(Calendar.MONTH, monthOfYear);
                    endCalander.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // now show the time picker
                    new TimePickerDialog(ActivityDetails.this,
                            new TimePickerDialog.OnTimeSetListener() {

                                @Override
                                public void onTimeSet(TimePicker view,
                                                      int h, int min) {
                                    endCalander.set(Calendar.HOUR_OF_DAY, h);
                                    endCalander.set(Calendar.MINUTE, min);
//                                date = startCalander.getTime();

                                    endDate.setText(Global.activityDateFormat.format(endCalander.getTime()));
                                    endDate.setTag(Global.universalDateFormat.format(endCalander.getTime()));
                                }
                            }, endCalander.get(Calendar.HOUR_OF_DAY),
                            endCalander.get(Calendar.MINUTE), false).show();

                }

            };
//            startDate.setOnClickListener(null);
//            endDate.setOnClickListener(null);
            startDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    if (hasFocus) {
                    try {
                        new DatePickerDialog(ActivityDetails.this, startTimePicker, startCalander.get(Calendar.YEAR), startCalander.get(Calendar.MONTH),
                                startCalander.get(Calendar.DAY_OF_MONTH)).show();
//                    }
                    }catch (Exception ex){
                        Log.e("DatePicker", ex.getMessage(), ex);
                    }
                }
            });

            startDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {
                        new DatePickerDialog(ActivityDetails.this, startTimePicker, startCalander.get(Calendar.YEAR), startCalander.get(Calendar.MONTH),
                                startCalander.get(Calendar.DAY_OF_MONTH)).show();
                    }
                }
            });

            endDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        new DatePickerDialog(ActivityDetails.this, endTimePicker, endCalander.get(Calendar.YEAR), endCalander.get(Calendar.MONTH),
                                endCalander.get(Calendar.DAY_OF_MONTH)).show();
                }
            });

            endDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    // TODO Auto-generated method stub
                    if (hasFocus) {
                        new DatePickerDialog(ActivityDetails.this, endTimePicker, endCalander.get(Calendar.YEAR), endCalander.get(Calendar.MONTH),
                                endCalander.get(Calendar.DAY_OF_MONTH)).show();
                    }
                }
            });


        } catch (Exception ex){
            Log.e("LoadActivitySettings", ex.getMessage(), ex);
            ShowError();
        }
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch (id) {
            case R.id.btn_activity_cancel:
                finish();
                break;
            case R.id.btn_activity_delete:
                DeleteActivity();
                break;
            case R.id.btn_activity_update:
                UpdateActivity();
                break;
        }

    }

    private void DeleteActivity() {
        if(activity != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirm")
                    .setCancelable(true)
                    .setMessage("Are you sure?")
                    .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DeleteActivity(getToken(), activity.ActivityID);
                        }
                    })
                    .setNegativeButton("Cancel", null);
            builder.show();
        }
    }

    private void UpdateActivity() {
        if(activity != null) {
            final EditText edit_description = findViewById(R.id.edit_activity_description);
            final Switch isPrivate = (Switch) findViewById(R.id.activity_is_private);
            final String description = edit_description.getText().toString().trim();
            final EditText startDate = (EditText) this.findViewById(R.id.activity_start_date);
            final EditText endDate = (EditText) this.findViewById(R.id.activity_end_date);

            final String currentDateandTime = Global.universalDateFormat.format(new Date());
            activity.description = description;
            ActivityTypes activityTypes = isPrivate.isChecked() ? ActivityTypes.ONREQUEST : ActivityTypes.PUBLIC;
//            final ActivitySettingsModel activitySettings = new ActivitySettingsModel(null, startDate.getTag().toString(), endDate.getTag().toString(), activityTypes, ActivityStatuses.OPEN, 0, 0, null);
            activity.activitySetting.StartTime = startDate.getTag().toString();
            activity.activitySetting.EndTime = endDate.getTag().toString();
            activity.activitySetting.ActivityType = activityTypes;
            activity.activitySetting.ActivityId = activity.ActivityID;

            PutActivity(getToken(), activity);
        }
    }

    private void DeleteActivity(final String token, final String activityId) {
        ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        Call<Void> call = apiService.deleteActivity("Bearer " + token, activityId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                        finish();
//                        startActivity(getIntent());
//                        GetMatchingActivitiesByKeyword(token, activity.What);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("PostActivity", "Error on postActivity" + t.getMessage());
                ShowError();
            }
        });
    }

    private void PutActivity(final String token, final ActivityModel activity) {
        ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        Call<ActivityModel> call = apiService.putActivity("Bearer " + token, activity);
        call.enqueue(new Callback<ActivityModel>() {
            @Override
            public void onResponse(Call<ActivityModel> call, Response<ActivityModel> response) {
                if (response.isSuccessful()) {
                    ActivityModel newActivityModel = response.body();
                    if(newActivityModel != null) {
                        uploadImage(newActivityModel.DeviceID, newActivityModel.ActivityID, token);
//                        startActivity(getIntent());
//                        GetMatchingActivitiesByKeyword(token, activity.What);
                    }
                }
            }

            @Override
            public void onFailure(Call<ActivityModel> call, Throwable t) {
                Log.e("PostActivity", "Error on postActivity" + t.getMessage());
                ShowError();
            }
        });
    }

    private void uploadImage(String deviceId, String activity, String token) {
        try {
            String tempFileName = CommonUtility.GetImageFileFullPath();
            if (this.bitmap != null) {
                FileOutputStream out = new FileOutputStream(tempFileName);
                this.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored
                File file = new File(tempFileName);
//            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
                RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);

                MultipartBody.Part body = MultipartBody.Part.createFormData("upload", file.getName(), requestBody);

                RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "upload_test");

                PostActivityImage(deviceId, activity, token, body);
                out.flush();
                out.close();

            }else {
                finish();
            }
        } catch (Exception ex) {
            Log.e("ActivityImageUpload", ex.getMessage());
            ShowError();
        }
    }

    private void PostActivityImage(String deviceId, String activity, String token, MultipartBody.Part body) {
        ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        Call<Void> call = apiService.postActivityImage("Bearer " + token, deviceId, activity, body);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ActivityImageUploader", t.getMessage());
                ShowError();
            }
        });
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

    private void ShowError(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm")
                .setCancelable(true)
                .setMessage("Oops!, Something went wrong. Please try again")
                .setNegativeButton("OK", null);
        builder.show();
    }
}
