package com.hackathon.nova.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

public class ContactListHelper {

    public static String fetchContactByName(Context context, String targetName) {
        ContentResolver contentResolver = context.getContentResolver();

        Cursor cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " = ?",
                new String[]{targetName},
                null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String phoneNumber = cursor.getString(1); // Get phone number
                cursor.close();
                return phoneNumber;
            }
            cursor.close();
        }
        return "Contact Not Found";
    }

    public static List<String> fetchContacts(Context context) {
        List<String> contactList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER},
                null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                String phone = cursor.getString(1);
                contactList.add("CONTACT_NAME = " + name);
            }
            cursor.close();
        }
        return contactList;
    }
}
