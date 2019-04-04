package com.puurva.findmetoo.Activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.puurva.findmetoo.R;
import com.puurva.findmetoo.uitls.SQLiteManager;

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
