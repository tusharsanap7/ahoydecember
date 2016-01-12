package ahoy.ahoydecember.configuration;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by Deepak on 25/12/2015.
 */
public class SQLiteHandler extends SQLiteOpenHelper{
    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "ahoy";

    // Login table name
    private static final String TABLE_USER = "user";
    private static final String TABLE_EMERGENCY_CONTACT = "emergency_contact_table";
    // Login Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE_NUMBER = "phonenumber";
    private static final String KEY_AGE = "age";
    private static final String KEY_PHOTO_URL = "photourl";
    private static final String KEY_CREATED_AT = "created_at";
    private static final String KEY_EMERGENCY_CONTACT="emergency_contact";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        //user table
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE," + KEY_PHONE_NUMBER + " TEXT,"
                +KEY_AGE + " TEXT," + KEY_PHOTO_URL + "TEXT,"
                + KEY_CREATED_AT + " TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);

        //emergency_contact table
        String CREATE_EMERGENCY_CONTACT_TABLE= "CREATE TABLE " + TABLE_EMERGENCY_CONTACT + "("
                + KEY_EMAIL + " INTEGER PRIMARY KEY,"+ KEY_EMERGENCY_CONTACT+ " TEXT,"
                + KEY_CREATED_AT + " TEXT" + ")";

        Log.d(TAG, "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMERGENCY_CONTACT);
        // Create tables again
        onCreate(db);
    }

    /**
     * Storing user details in database
     * */
    public void addUser(String name, String email, String photourl, String phonenumber, String age, String created_at) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name); // Name
        values.put(KEY_EMAIL, email); // Email
        values.put(KEY_PHONE_NUMBER, phonenumber); // phonenumber
        values.put(KEY_PHOTO_URL, photourl); // phonenumber
        values.put(KEY_AGE, age); // phonenumber
        values.put(KEY_CREATED_AT, created_at); // Created At

        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New user inserted into sqlite: " + id);
    }

    //emergency_contact_table
    public void addEmergencyContact(String email, String emergency_contact, String created_at) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_EMAIL, email); // Email
        values.put(KEY_PHONE_NUMBER, emergency_contact); // phonenumber
        values.put(KEY_CREATED_AT, created_at); // Created At

        // Inserting Row
        long id = db.insert(TABLE_EMERGENCY_CONTACT, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "Emergency contact added!: " + id);
    }
    /**
     * Getting user data from database
     * */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("name", cursor.getString(1));
            user.put("email", cursor.getString(2));
            user.put("phonenumber", cursor.getString(3));
            user.put("photourl", cursor.getString(4));
            user.put("age", cursor.getString(5));
            user.put("created_at", cursor.getString(6));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }

    public HashMap<String, String> getEmergencyContact() {
        HashMap<String, String> contact = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_EMERGENCY_CONTACT;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            contact.put("email", cursor.getString(1));
            contact.put("emergency_contact", cursor.getString(2));
            contact.put("created_at", cursor.getString(3));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + contact.toString());

        return contact;
    }

    /**
     * Re crate database Delete all tables and create them again
     * */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.delete(TABLE_EMERGENCY_CONTACT,null,null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }
}
