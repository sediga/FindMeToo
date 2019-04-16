package com.puurva.findmetoo.uitls;

import android.content.ContentValues;
import android.database.Cursor;

import com.puurva.findmetoo.ServiceInterfaces.model.DeviceModel;

import java.util.Calendar;

/**
 * Created by puurva on 2018-08-24.
 * sqlite database manager
 */

public class SQLHelper {

    public static boolean AddDevice(DeviceModel deviceModel){
        ContentValues values = new ContentValues();
        values.put("DeviceId", deviceModel.DeviceID);
        values.put("EmailId", deviceModel.EmailID);
        values.put("SoftwareVersion", deviceModel.SoftwareVersion);
        values.put("NotificationToken", deviceModel.NotificationToken);
        values.put("CreatedOn", Calendar.getInstance().getTime().toString());
        return Insert("DeviceInfo", values);
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

    public static boolean Insert( String table, ContentValues values){

        Global.mdb.insert(table, null, values);

        return true;
    }

    public static String getToken(String username) {
        String token = null;
        Cursor c = Global.mdb.rawQuery(
                "SELECT *    " +
                        "FROM apiuser " +
                        "WHERE deviceid = '" + username + "' " +
                        "LIMIT 1",
                null);

        if (c != null || c.getCount() > 0) {
            c.moveToFirst();
            token = c.getString(2);
        }
        return token;
    }

}
