package com.puurva.findmetoo.uitls;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;
import com.puurva.findmetoo.ServiceInterfaces.model.UserModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DBUtils {
    public static List<UserModel> userList = new ArrayList<>();
    public static FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    public static void AddNewUser(String uid, String userName, String displayName) {
        UserModel newUser = new UserModel(uid, userName, displayName);
        try {

            firebaseFirestore.collection("users").document(uid).set(newUser);
        } catch (Exception ex) {
            Log.e("UpdateBoardStatus", ex.getMessage(), ex);
        }
    }

//    public static void AddDevice(String deviceNotificationId, String softwareVersion) {
//        Device newDevice = new Device(deviceNotificationId, softwareVersion);
//        try {
//            FirebaseDatabase database = FirebaseDatabase.getInstance();
//            DatabaseReference myRef = database.getReference();
//            myRef.child("devices").child(deviceNotificationId).setValue(newDevice);
//        } catch (Exception ex) {
//            Log.e("UpdateBoardStatus", ex.getMessage(), ex);
//        }
//    }

//    public static void removeAllUsers() {
//        for (int i = 0; i < userList.size(); i++) {
//            removeUser(userList.get(i).Uid);
//        }
//    }

//    public static void removeUser(String uid) {
//        try {
//            FirebaseDatabase database = FirebaseDatabase.getInstance();
//            DatabaseReference userRef = database.getReference().child("users");
//
//            userRef.child(uid).removeValue();
//        } catch (Exception ex) {
//            Log.e("UpdateBoardStatus", ex.getMessage(), ex);
//        }
//    }


}
