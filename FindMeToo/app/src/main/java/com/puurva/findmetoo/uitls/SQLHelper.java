package com.puurva.findmetoo.uitls;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.puurva.findmetoo.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by puurva on 2018-08-24.
 * sqlite database manager
 */

public class SQLHelper {

    public static boolean Insert( String table, ContentValues values){

        Global.mdb.insert(table, null, values);

        return true;
    }
}
