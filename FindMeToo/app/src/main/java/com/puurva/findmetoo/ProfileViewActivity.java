package com.puurva.findmetoo;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.puurva.findmetoo.ServiceInterfaces.ApiInterface;
import com.puurva.findmetoo.model.ProfileModel;
import com.puurva.findmetoo.preference.PrefConst;
import com.puurva.findmetoo.uitls.Global;
import com.puurva.findmetoo.uitls.HttpClient;

import org.w3c.dom.Text;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileViewActivity extends AppCompatActivity {
        //implements View.OnClickListener {

    private String imageFilePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        fillProfile();
    }

    private  void photoClicked()
    {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 1);
    }


    public String getPath(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        String imagePath = cursor.getString(column_index);

        return cursor.getString(column_index);
    }

    private void fillProfile()
    {
        String deviceId = String.valueOf(Global.current_user.getId());
        final String token = Global.preference.getValue(this, PrefConst.TOKEN, "");
        final TextView txtProfileName = ((TextView) findViewById(R.id.txt_view_name));
        final TextView txtHobies = ((TextView) findViewById(R.id.txt_view_hobies));
        final TextView txtAbout = ((TextView) findViewById(R.id.txt_view_about));
        ApiInterface apiService =
                HttpClient.getClient().create(ApiInterface.class);
        Call<ProfileModel> call = apiService.getProfile("Bearer " + token, deviceId);
        call.enqueue(new Callback<ProfileModel>() {
            @Override
            public void onResponse(Call<ProfileModel> call, Response<ProfileModel> response) {
                if(response.isSuccessful()) {
                    ProfileModel profileModel = response.body();
                    if(profileModel!=null) {
                        txtProfileName.setText(profileModel.getProfileName());
                        txtHobies.setText(profileModel.getHobies());
                        txtAbout.setText(profileModel.getAbout());
                    }
                }
            }

            @Override
            public void onFailure(Call<ProfileModel> call, Throwable t) {
            }
        });
    }
}
