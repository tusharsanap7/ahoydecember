package ahoy.ahoydecember.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.plus.Plus;

import java.util.HashMap;

import ahoy.ahoydecember.R;
import ahoy.ahoydecember.configuration.SQLiteHandler;
import ahoy.ahoydecember.configuration.SessionManager;

//google packages

public class MainActivity extends AppCompatActivity implements FragmentDrawer.FragmentDrawerListener,
        ConnectionCallbacks, OnConnectionFailedListener, OnMapReadyCallback {
    private Toolbar mToolbar;
    private FragmentDrawer drawerFragment;
    private FragmentDrawer Display;//showing email and username;
    //adding google sign out and session management things below
    SessionManager session;
    GoogleApiClient mGoogleApiClient;
    boolean mSignInClicked;
    private SQLiteHandler db;
    public String name, email, url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        drawerFragment = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);

        Display = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        Display.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);


        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addApi(LocationServices.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();
        //check if logged in if not proceed with login screen automatically
        session.checkLogin();

        // get user data from session

        HashMap<String, String> user = session.getUserDetails();
        // name
        name = user.get(SessionManager.KEY_NAME);
        // email
        email = user.get(SessionManager.KEY_EMAIL);//Don't ever fucking comment this shit man.
        //url
        url = user.get(SessionManager.KEY_URL);

        Display.setTextViewText(name, email);
        Display.setProfilePhoto(url);
        //maps implementation starts here
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }//onCreate Finish


    @Override
    public void onMapReady(GoogleMap map) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        map.setMyLocationEnabled(true);
    }


    //usual overriding the methods. Check into it why they are empty
    @Override
    public void onConnected(Bundle arg0) {
        // TODO Auto-generated method stub
        mSignInClicked = false;


    }

    @Override
    public void onConnectionSuspended(int arg0) {
        // TODO Auto-generated method stub
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        // TODO Auto-generated method stub

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

    //This is to close the app if back button is pressed!
    @Override
    public void onBackPressed() {
        //trying to avoid going back
        moveTaskToBack(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position);
    }

    private void displayView(int position) {
        switch (position) {
            case 0:

                break;
            case 1:
                break;
            case 4:
                //emergency_contact_no
                Intent intent = new Intent(getApplicationContext(), EmergencyContact.class);
                Bundle extras = new Bundle();
                extras.putString("email", email);
                intent.putExtras(extras);
                startActivity(intent);
                break;
            case 7:
                // Clear the session data.This will clear all session data and redirect user to LoginActivity.
                // case 6 is for logout button.
                exit_alert();
                break;
            default:
                break;
        }

    }

    private void exit_alert() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);
        builder.setTitle("Log Out");
        builder.setMessage("Are you sure?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                session.logoutUser();
                if (mGoogleApiClient.isConnected()) {
                    Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                    mGoogleApiClient.disconnect();
                    mGoogleApiClient.connect();
                    //updateUI(false);
                    System.err.println("LOG OUT ^^^^^^^^^^^^^^^^^^^^ SUCESS");
                }
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
}