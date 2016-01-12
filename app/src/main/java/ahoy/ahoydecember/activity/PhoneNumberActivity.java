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

public class PhoneNumberActivity extends AppCompatActivity implements View.OnClickListener {

    //variables for verify button and phonenumber text field
    private static final String TAG = PhoneNumberActivity.class.getSimpleName();
    private EditText inputMobile,age;
    private Button verify_button;
    private String email,personName,personPhotoUrl,mobile,useragex;
    private Toolbar toolbar;
    private ProgressDialog pDialog;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        personName = extras.getString("username");
        email = extras.getString("email");
        personPhotoUrl = extras.getString("profilephotourl");
        //adding the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //how about adding back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //assigning on clicks
        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        verify_button = (Button) findViewById(R.id.verifybutton);
        inputMobile = (EditText) findViewById(R.id.inputMobile);
        age = (EditText) findViewById(R.id.age);
        verify_button.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.verifybutton:
                validateForm();
                break;
        }
    }
    /**
     * Validating user details form
     */
    private void validateForm() {

        mobile = inputMobile.getText().toString().trim();
        useragex = age.getText().toString().trim();
        //validating age
        if(!isValidAge(useragex) || useragex.length()>3){
            Toast.makeText(getApplicationContext(),"Bhai sahi se bata na kitna bada hai!",Toast.LENGTH_SHORT).show();
            return;
        }
        // validating mobile number
        // it should be of 10 digits length
        if (isValidPhoneNumber(mobile)) {

            //add php handling here to save all data
            sendtoserver();


        } else {
            Toast.makeText(getApplicationContext(), "Please enter valid mobile number", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Regex to validate the mobile number
     * mobile number should be of 10 digits length
     */

    private static boolean isValidPhoneNumber(String mobile) {
        String regEx = "^[0-9]{10}$";
        return mobile.matches(regEx);
    }
    private static boolean isValidAge(String useragex) {
        String regEx = "[0-9]+$";
        return useragex.matches(regEx);
    }
    //sending everything to server
    private void sendtoserver(){
        //registering.
        registerUser(personName,email,personPhotoUrl,mobile,useragex);
    }

    /**
    * Function to store user in MySQL database will post params(tag, name,
    * email) to register url
    * */
    private void registerUser(final String name, final String email,
                              final String photourl, final String phonenumber, final String age) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQL
                        // Now store the user in sqlite

                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString("name");
                        String email = user.getString("email");
                        String photourl = user.getString("photourl");
                        String phonenumber = user.getString("phonenumber");
                        String age = user.getString("age");
                        String created_at = user.getString("created_at");

                        // Inserting row in users table
                        db.addUser(name, email, photourl, phonenumber, age, created_at);

                        Toast.makeText(getApplicationContext(), "User successfully registered. Try login now!", Toast.LENGTH_LONG).show();

                        // Launch login activity
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else {

                        // Error occurred in registration. Get the error
                        // message
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
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", name);
                params.put("useremail", email);
                params.put("userphotourl", photourl);
                params.put("userphonenumber", phonenumber);
                params.put("userage", age);

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

