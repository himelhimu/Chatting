package com.example.sabbir.chatting.model.util;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by sabbi on 10/15/2016.
 */

public class LibraryClass {
    public static String PREF="";
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

    public static void saveSP()
    {

    }

    public static String getSP()
    {
        return null;
    }
}
