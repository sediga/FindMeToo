package com.puurva.findmetoo.uitls;

import android.app.AlertDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.puurva.findmetoo.Activities.LoginActivity;
import com.puurva.findmetoo.ServiceInterfaces.model.DeviceModel;
import com.puurva.findmetoo.ServiceInterfaces.model.UserModel;
import com.puurva.findmetoo.preference.Preference;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Global {

    public static boolean ISMARKERCLICKED = false;
    public static String AndroidID = null;
    public static UserModel current_user;
    public static SQLiteDatabase mdb;
    public static Preference preference;
    public static DeviceModel device_info;
    public static boolean has_device_registered;
    public static boolean is_loggedin = false;
    public static Bitmap CurrentImage = null;

    public static final int PERMISSION_REQUEST_CODE = 1111;
    public static String[] select_column = new String[]{"id", "first_name", "last_name", "Lat", "Long"};
    public static final String BASE_URL = "http://findmetoo.com/";
    public static String FILE_PATH_SUFFIX = "/.findmetoo/Files/";
    public static String TOKEN = null;
    public static SimpleDateFormat universalDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
    public static SimpleDateFormat activityDateFormat = new SimpleDateFormat("MMM dd, hh:mm a", Locale.US);
    //-----------Toast Functions---------------------------------

    public static void showShortToast(Context context, int res_id){
        Toast.makeText(context, res_id, Toast.LENGTH_SHORT).show();
    }

    public static void showShortToast(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showAlert(Context context, String title, String message) {
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                View view = LayoutInflater.from(LoginActivity.this).inflate(R.layout.dialog_keyword, null);
        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", null);
        builder.show();
    }
}
