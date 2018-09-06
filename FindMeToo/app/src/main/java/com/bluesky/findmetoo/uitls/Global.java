package com.bluesky.findmetoo.uitls;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.bluesky.findmetoo.model.UserModel;
import com.bluesky.findmetoo.preference.Preference;

public class Global {

    public static UserModel current_user;
    public static SQLiteDatabase mdb;
    public static Preference preference;

    public static final int PERMISSION_REQUEST_CODE = 1111;
    public static String[] select_column = new String[]{"id", "first_name", "last_name", "latitude", "longitude"};
    public static final String BASE_URL = "http://findmetoo.com/";

    //-----------Toast Functions---------------------------------

    public static void showShortToast(Context context, int res_id){
        Toast.makeText(context, res_id, Toast.LENGTH_SHORT).show();
    }

    public static void showShortToast(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

}
