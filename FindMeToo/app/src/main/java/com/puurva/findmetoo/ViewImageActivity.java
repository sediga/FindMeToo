package com.puurva.findmetoo;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.puurva.findmetoo.ServiceInterfaces.ApiInterface;
import com.puurva.findmetoo.model.Token;
import com.puurva.findmetoo.model.UserModel;
import com.puurva.findmetoo.preference.PrefConst;
import com.puurva.findmetoo.preference.Preference;
import com.puurva.findmetoo.uitls.CallBackHelper;
import com.puurva.findmetoo.uitls.Global;
import com.puurva.findmetoo.uitls.HttpClient;
import com.puurva.findmetoo.uitls.SQLHelper;
import com.puurva.findmetoo.uitls.SQLiteManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewImageActivity extends AppCompatActivity {

    private SQLiteManager dbHelper;
    public Bitmap bitmap = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewimage);
        byte[] byteArray = getIntent().getByteArrayExtra("bitmap");
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        ImageView viewImageView =  this.findViewById(R.id.view_info_image);
        viewImageView.setImageBitmap(bmp);
    }

}
