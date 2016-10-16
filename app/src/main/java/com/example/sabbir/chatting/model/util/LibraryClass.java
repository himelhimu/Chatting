package com.example.sabbir.chatting.model.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by sabbi on 10/15/2016.
 */

public class LibraryClass {
    public static String PREF="com.example.sabbir.chatting.PREF";
    private static DatabaseReference reference;

    private LibraryClass()
    {}

    public static DatabaseReference getFirebase()
    {
        if (reference==null)
        {
            reference= FirebaseDatabase.getInstance().getReference();
        }
        return reference;
    }

    static public void saveSP(Context context, String key, String value ){
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putString(key, value).apply();
    }

    static public String getSP(Context context, String key ){
        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String token = sp.getString(key, "");
        return( token );
    }


}
