package ahoy.ahoydecember.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import ahoy.ahoydecember.R;
import ahoy.ahoydecember.configuration.SQLiteHandler;
import ahoy.ahoydecember.configuration.SessionManager;


public class SignupActivity extends AppCompatActivity implements OnClickListener,
        ConnectionCallbacks, OnConnectionFailedListener {

    private TextView loginLink;
    private static final int RC_SIGN_IN = 0;
    // Logcat tag
    private static final String TAG = "SignupActivity";

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    /*
     * A flag indicating that a PendingIntent is in progress and prevents us
     * from starting further intents.
     */
    private Toolbar toolbar;
    private SessionManager session;
    private SQLiteHandler db;
    private ProgressDialog pDialog;//added later
    private boolean mIntentInProgress;
    private boolean mSignInClicked;
    private ConnectionResult mConnectionResult;
    private Button btnSignup;
    private String email,personName,personPhotoUrl,personGooglePlusProfile;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        //adding the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        //how about adding back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());
        btnSignup = (Button) findViewById(R.id.btn_sign_up);
        loginLink = (TextView) findViewById(R.id.link_login);

        btnSignup.setOnClickListener(this);
        loginLink.setOnClickListener(this);//loginLink

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Method to resolve any signin errors
     * */
    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
                    0).show();
            return;
        }

        if (!mIntentInProgress) {
            // Store the ConnectionResult for later usage
            mConnectionResult = result;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to
                // resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int responseCode,
                                    Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }
    @Override
    public void onConnected(Bundle arg0) {
        mSignInClicked = false;
        // Get user's information
        getProfileInformation();
        // Update the UI after signin
        updateUI(true);

    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
        updateUI(false); //trying out change for constant popping

    }

    /**
     * Fetching user's information name, email, profile pic
     * */
    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi
                        .getCurrentPerson(mGoogleApiClient);
                personName = currentPerson.getDisplayName();
                personPhotoUrl = currentPerson.getImage().getUrl();
                personGooglePlusProfile = currentPerson.getUrl();
                email = Plus.AccountApi.getAccountName(mGoogleApiClient);
                Log.e(TAG, "Name: " + personName + ", plusProfile: "
                        + personGooglePlusProfile + ", email: " + email
                        + ", Image: " + personPhotoUrl);



            } else {
                Toast.makeText(getApplicationContext(),
                        "Person information is null", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updating the UI, showing/hiding buttons and profile layout
     * */
    private void updateUI(boolean isSignedIn) {
        if (isSignedIn) {
            //singing out bc
            if (mGoogleApiClient.isConnected()) {
                Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                mGoogleApiClient.disconnect();
                mGoogleApiClient.connect();
            }
            Intent intent = new Intent(getApplicationContext(), PhoneNumberActivity.class);
            Bundle extras = new Bundle();
            extras.putString("username",personName);
            extras.putString("email",email);
            extras.putString("profilephotourl",personPhotoUrl);
            intent.putExtras(extras);
            startActivity(intent);

        }
        else {
            btnSignup.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign_up:
                // Signin button clicked
                signInWithGplus();
                break;
            case R.id.link_login:
                // Start the Signup activity
                Intent j  = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(j);
                break;


        }
    }
    /**
     * Sign-in into google
     * */
    private void signInWithGplus() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent i =new Intent(this,LoginActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

