package ahoy.ahoydecember.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

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
    private Button update_button;
    private Toolbar toolbar;
    private TextView phoneDisplay;
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private String emergency_mobile,econtact,email;
    SessionManager session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        email = extras.getString("email");
        //how about adding back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        session = new SessionManager(getApplicationContext());
        phoneDisplay = (TextView) findViewById(R.id.phonedisplay);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        update_button = (Button) findViewById(R.id.update_button);
        emergency_contact = (EditText) findViewById(R.id.emergency_contact);

        update_button.setOnClickListener(this);
        // SQLite database handler
       // db = new SQLiteHandler(getApplicationContext());

        // get user data from session
        HashMap<String, String> user = session.getEmergencyContact();
        econtact = user.get(SessionManager.KEY_EMERGENCY_CONTACT);
        phoneDisplay.setText(econtact);




    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.update_button:
                validateForm();
                break;
        }
    }
    private void validateForm() {

        emergency_mobile = emergency_contact.getText().toString().trim();
        if (isValidPhoneNumber(emergency_mobile)) {

            //add php handling here to save all data
            serverupdate(emergency_mobile);


        } else {
            Toast.makeText(getApplicationContext(), "Please enter valid mobile number", Toast.LENGTH_SHORT).show();
        }
    }
    private static boolean isValidPhoneNumber(String mobile) {
        String regEx = "^[0-9]{10}$";
        return mobile.matches(regEx);
    }
    private void serverupdate(final String emergency_mobile) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

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

                        JSONObject user = jObj.getJSONObject("user");
                        String email1 = user.getString("email");
                        String emergency_number1 = user.getString("emergency_contact");
                        //String created_at = user.getString("created_at");
                        /*
                        // Inserting row in users table
                        db.addEmergencyContact(email, emergency_number, created_at);
                           */
                        session.putEmergencyContact(email1,emergency_number1);
                        phoneDisplay.setText(emergency_number1);
                        Toast.makeText(getApplicationContext(), "Emergency contact saved sucessfully!", Toast.LENGTH_LONG).show();



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
                params.put("emergency_contact", emergency_mobile);
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
