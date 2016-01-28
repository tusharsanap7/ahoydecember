package ahoy.ahoydecember.activity;
// Remove Shared Preferences from here and add sqlite mahn!
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.plus.Plus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import ahoy.ahoydecember.R;
import ahoy.ahoydecember.configuration.AppConfig;
import ahoy.ahoydecember.configuration.AppController;
import ahoy.ahoydecember.configuration.SQLiteHandler;
import ahoy.ahoydecember.configuration.SessionManager;

public class EmergencyContact extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = EmergencyContact.class.getSimpleName();
    private EditText emergency_contact;
    private Button update_button,select_contact;
    private Toolbar toolbar;
    private TextView contact,title,name;
    private ProgressDialog pDialog;
    private LinearLayout listlayout;
    private String email;
    private static final int REQUEST_CODE_PICK_CONTACTS = 1;
    private Uri uriContact;
    private String contactID;// contacts unique ID
    //store the emergency contact name and number
    private String e_contact_name,e_contact_number;
    private String e_name_display, e_phone_display; // just trying with some global variables
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //intent to get email from main activity
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        email = extras.getString("email");

        //how about adding back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        pDialog = new ProgressDialog(this);//process diaglogue box
        pDialog.setCancelable(false);

        select_contact=(Button)findViewById(R.id.select_contact);
        title=(TextView)findViewById(R.id.title);
        contact =(TextView) findViewById(R.id.contact);
        name=(TextView) findViewById(R.id.name);
        listlayout=(LinearLayout)findViewById(R.id.listlayout);

        //select_contact.setVisibility(View.VISIBLE);
        fetch_contact();






    }//oncreate over

    private void fetch_contact(){
// Tag used to cancel the request
        String tag_string_req = "Fetching Phone Number in Database";

        pDialog.setMessage("Fetching Data ...");
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_EMERGENCY_CONTACT_FETCH, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Fetching Response: " + response);
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQL
                        // Now store the user in sqlite
                        //why not try some dope global variable shitz and update there.

                        JSONObject user = jObj.getJSONObject("user");
                        e_name_display = user.getString("emergency_name");
                        e_phone_display = user.getString("emergency_contact");

                        showListView();

                        Toast.makeText(getApplicationContext(), "Emergency contact fetched sucessfully!", Toast.LENGTH_LONG).show();



                    }
                    else {
                        //error
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Emergency Contact Fetch Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    void showListView(){
        listlayout.setVisibility(View.VISIBLE);
        title.setVisibility(View.GONE);
        select_contact.setVisibility(View.GONE);
        name.setText(e_name_display);
        contact.setText(e_phone_display);
    }

    public void del_clicked(View v){
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);
        builder.setTitle("Delete Emergency Contact");
        builder.setMessage("Are you sure?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                delete_contact();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){


        }}

    //contact Fetch
    public void onClickSelectContact(View btnSelectContact) {
        // using native contacts selection
        // Intent.ACTION_PICK = Pick an item from the data, returning what was selected.
        startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == RESULT_OK) {
            Log.d(TAG, "Response: " + data.toString());
            uriContact = data.getData();

            retrieveContactName();
            retrieveContactNumber();

            //calling validate form here to get +91 and make length to 12
            validateForm();
        }
    }



    //Retrieve the user number
    private void retrieveContactNumber() {


        String contactNumber = null;

        // getting contacts ID
        Cursor cursorID = getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {

            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();

        Log.d(TAG, "Contact ID: " + contactID);

        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactID},
                null);

        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        cursorPhone.close();

        Log.d(TAG, "Contact Phone Number: " + contactNumber);
        //resultcontact+=contactNumber;
        e_contact_number = contactNumber;  //global variable to store the number
        //display_contact.setText(resultcontact);
    }
    //retrieve the user contact
    private void retrieveContactName() {

        String contactName = null;

        // querying contact data store
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);

        if (cursor.moveToFirst()) {

            // DISPLAY_NAME = The display name for the contact.
            // HAS_PHONE_NUMBER =   An indicator of whether this contact has at least one phone number.

            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }

        cursor.close();

        Log.d(TAG, "Contact Name: " + contactName);
        //resultcontact+=""+contactName;
        e_contact_name = contactName;


    }

    private void validateForm() {

        e_contact_number = e_contact_number.replace(" ", "");
        Log.e("CONTACT",e_contact_number);
        if (e_contact_number.length()==10) {
            e_contact_number = "+91" + e_contact_number;
        }//this is to make uniform 13 digit phone number
        if (isValidPhoneNumber(e_contact_number)) {

            //add php handling here to save all data
            serverupdate();
            //showListView();

        } else {
            Toast.makeText(getApplicationContext(), "Please enter valid mobile number", Toast.LENGTH_SHORT).show();
        }
    }
    private static boolean isValidPhoneNumber(String mobile) {
        String regEx = "^[0-9+]{13}$";
        return mobile.matches(regEx);
    }
    private void serverupdate() {
        // Tag used to cancel the request
        String tag_string_req = "Saving Phone Number in Database";

        pDialog.setMessage("Updating ...");
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_EMERGENCY_CONTACT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response);
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQL
                        // Now store the user in sqlite
                        //why not try some dope global variable shitz and update there.
                        Toast.makeText(getApplicationContext(), "Emergency contact saved sucessfully!", Toast.LENGTH_LONG).show();
                        JSONObject user = jObj.getJSONObject("user");
                        e_name_display = user.getString("emergency_name");
                        e_phone_display = user.getString("emergency_contact");

                        showListView();


                    }
                    else {

                        //error
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Emergency Contact Update Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("emergency_contact", e_contact_number);
                params.put("emergency_name",e_contact_name);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //once user presses delete button

    private void delete_contact(){
        String tag_string_req = "Deleting Emergency Phone Number From Database";

        pDialog.setMessage("Deleting ...");
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_EMERGENCY_CONTACT_DELETE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response);
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQL
                        // Now store the user in sqlite
                        //why not try some dope global variable shitz and update there.
                        Toast.makeText(getApplicationContext(), "Emergency contact deleted sucessfully!", Toast.LENGTH_LONG).show();


                        listlayout.setVisibility(View.GONE);
                        title.setVisibility(View.VISIBLE);
                        select_contact.setVisibility(View.VISIBLE);


                    }
                    else {

                        //error
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Emergency Contact Update Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }



    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
