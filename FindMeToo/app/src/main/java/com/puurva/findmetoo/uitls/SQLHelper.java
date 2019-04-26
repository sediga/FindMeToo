package com.puurva.findmetoo.uitls;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.puurva.findmetoo.ServiceInterfaces.model.DeviceModel;
import com.puurva.findmetoo.ServiceInterfaces.model.Token;
import com.puurva.findmetoo.ServiceInterfaces.model.UserModel;

import java.util.Calendar;

/**
 * Created by puurva on 2018-08-24.
 * sqlite database manager
 */

public class SQLHelper {

    public static boolean AddDevice(DeviceModel deviceModel){
        boolean retValue = false;
        try {
            ContentValues values = new ContentValues();
            values.put("DeviceId", deviceModel.DeviceID);
            values.put("EmailId", deviceModel.EmailID);
            values.put("SoftwareVersion", deviceModel.SoftwareVersion);
            values.put("NotificationToken", deviceModel.NotificationToken);
            values.put("CreatedOn", Calendar.getInstance().getTime().toString());
            retValue = Insert("DeviceInfo", values);
        }catch (Exception e){
            Log.e("AddDevice", e.getMessage(), e);
        }
        return retValue;
    }

    public static int UpdateDevice(DeviceModel deviceModel){
        ContentValues values = new ContentValues();
//        values.put("DeviceId", deviceModel.DeviceID);
        if(deviceModel.EmailID != null && !deviceModel.EmailID.isEmpty()) {
            values.put("EmailId", deviceModel.EmailID);
        }
        values.put("SoftwareVersion", deviceModel.SoftwareVersion);
        if(deviceModel.NotificationToken != null) {
            values.put("NotificationToken", deviceModel.NotificationToken);
        }
//        values.put("CreatedOn", Calendar.getInstance().getTime().toString());
        return Global.mdb.updateWithOnConflict("DeviceInfo", values, "DeviceId = ?", new String[]{deviceModel.DeviceID}, SQLiteDatabase.CONFLICT_ROLLBACK);
    }
    public static boolean UpsertDevice(DeviceModel deviceModel){
        if(deviceModel.DeviceID == null){
            return AddDevice(deviceModel);
        }
        DeviceModel existingDevice = GetDevice(deviceModel.DeviceID);
        if(existingDevice == null){
            return AddDevice(deviceModel);
        }
        return UpdateDevice(deviceModel) > 0;
    }

    public static DeviceModel GetLatestDevice(){
        Cursor c = Global.mdb.rawQuery(
                "SELECT  * FROM DeviceInfo ORDER BY CreatedOn DESC LIMIT 1",
                null);

        if (c == null || c.getCount() == 0) {
            return null;
        }

        c.moveToFirst();
        Global.device_info = new DeviceModel(
                c.getString(0),
                c.getString(1),
                c.getString(2),
                c.getString(3)
        );

        return  Global.device_info;
    }

    public static DeviceModel GetDevice(String deviceId){
        Cursor c = Global.mdb.rawQuery(
                "SELECT  * FROM DeviceInfo WHERE DeviceId = '"+deviceId+"'",
                null);

        if (c == null || c.getCount() == 0) {
            return null;
        }

        c.moveToFirst();
        Global.device_info = new DeviceModel(
                c.getString(0),
                c.getString(1),
                c.getString(2),
                c.getString(3)
        );

        return  Global.device_info;
    }

    public static boolean Insert( String table, ContentValues values){

        Global.mdb.insert(table, null, values);

        return true;
    }

    public static String getToken(String deviceId) {
        String token = null;
        try {
            Cursor c = Global.mdb.rawQuery(
                    "SELECT *    " +
                            "FROM apiuser " +
                            "WHERE deviceid = '" + deviceId + "' " +
                            "LIMIT 1",
                    null);

            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                token = c.getString(2);
            }
        }catch (Exception e){
            Log.e("GetToken:", e.getMessage(), e);
        }
        return token;
    }

    public static void RegisterToken(Token token) {
        String storedToken = getToken(Global.AndroidID);
        ContentValues values = new ContentValues();
        values.put("token", token.access_token);
        if(storedToken == null) {
            values.put("deviceid", Global.AndroidID);
            Insert("apiuser", values);
        }else if(storedToken.compareTo(token.access_token) != 0){

            Global.mdb.update("apiuser", values, "deviceid = ?", new String[]{Global.AndroidID});
        }
//        saveToken(token.access_token);
    }

    public static boolean getUser(String username, String password) {
        Cursor c = Global.mdb.rawQuery(
                "SELECT *    " +
                        "FROM t_user " +
                        "WHERE email = '" + username + "' AND password = '" + password + "' " +
                        "LIMIT 1",
                null);

        if (c == null || c.getCount() == 0) {
//            Global.showShortToast(this, "user name or password is invalid.");
            return false;
        }

//        Global.preference.put(this, PrefConst.USERNAME, username);
//        Global.preference.put(this, PrefConst.PASSWORD, password);

        c.moveToFirst();
        Global.current_user = new UserModel(
                c.getInt(0),
                c.getString(1),
                c.getString(2)
        );
        return true;
    }

}
