package com.example.aleksa.androgen;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.aleksa.androgen.data.PolenContract;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/* TODO this class expects the context to be MainActivity, use carefully or change in the future
A class that handles a GoogleApiClient for location, and the logic for getting a location

Gets the current location, or displays a message explaining why it is not available
 */
public class LocationTracker implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener{

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 100;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Context mContext;
    Handler mRequestExpiredHandler;
    Runnable mRequestExpiredRunnable;

    public LocationTracker(Context context) {

        mContext = context;

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void connect() {


        // Get a location manager
        LocationManager locationManager =
                (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        // Check whether the network provider is enabled or not
        // If it's not, we should ask the user to enable it first
        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);

            // TODO extract this to a string resource
            alertDialogBuilder.setMessage("Lokacija je isključena. Da li biste želeli " +
                    "da je uključite?")
                    .setCancelable(false)
                    .setPositiveButton(
                            "Uključi",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent setLocationSettings = new Intent(
                                            Settings.ACTION_LOCATION_SOURCE_SETTINGS
                                    );
                                    mContext.startActivity(setLocationSettings);
                                }
                            }
                    )
                    .setNegativeButton("Ne želim",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(mContext, "Određivanje lokacije nemoguće",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                    );

            alertDialogBuilder.show();

        }

        // Check again whether the provider is on, since the user maybe didn't turn it on
            mGoogleApiClient.connect();

    }

    // A method that informs the user that the request for location has failed
    private void informLocationRequestFailed(){
        // TODO extract this message to a string resource

        Snackbar.make(
                ((MainActivity)mContext).findViewById(R.id.main_pager),
                "Određivanje lokacije neuspelo",
                Snackbar.LENGTH_INDEFINITE)
                .setAction("Pokušaj ponovo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        connect();
                    }
                })
                .show();

        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        // Check whether the permissions were granted (will most likely trigger on SDK 23+)
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)
        {

            // Request ACCESS_COARSE_LOCATION permission
            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        }

        // TODO maybe check if we can getLastLocation here?

        // Get a handler which we will use later in case our request expires
        mRequestExpiredHandler = new Handler();

        mRequestExpiredRunnable = new Runnable() {
            @Override
            public void run() {
                informLocationRequestFailed();
            }
        };

        // Create a LocationRequest for getting our location
        LocationRequest request = LocationRequest.create();

        request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        request.setInterval(1000);
        request.setFastestInterval(100);

        // Set the number of updates to just 1, since we don't need real-time tracking
        request.setNumUpdates(1);

        // Set a timeout period, in case the request can't be fulfilled
        request.setExpirationDuration(1000 * 10);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, request, this);

        mRequestExpiredHandler.postDelayed(mRequestExpiredRunnable, 1000 * 10);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // TODO decide what to do
        Log.e("LocationTracker", "onConnectionSuspended");
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // TODO decide what to do with each of the possible results
        Log.e("LocationTracker", "onConnectionFailed");
    }

    @Override
    public void onLocationChanged(Location location) {
        // First determine the closest location, then select it and tell the user how far they are

        // Location reading succeeded, we don't need the Handler callback anymore
        mRequestExpiredHandler.removeCallbacks(mRequestExpiredRunnable);

        float[] distanceToNearest = new float[1];

        int nearestLocationId = Utilities.findNearestLocation(
                location.getLongitude(),location.getLatitude(), mContext, distanceToNearest);

        // Check if nearestLocationId is -1, meaning that the query for locations was unsuccessful
        if (nearestLocationId == -1){
            Log.e("LocationTracker", "Query of locations failed");
            // TODO maybe it should inform the user that the query failed or do something else
        }
        else {
            // Get nearest location name
            Uri queryUri = PolenContract.LocationEntry.buildLocationUri(nearestLocationId);
            Cursor nearestLocation = mContext.getContentResolver().query(
                    queryUri,
                    null,
                    null, null,
                    null
            );
            nearestLocation.moveToFirst();

            String nearestLocationName = nearestLocation.getString(
                    nearestLocation.getColumnIndex(PolenContract.LocationEntry.COLUMN_NAME));

            // If the distance is not greater than 30km, pick it and display a message
            if (distanceToNearest[0] <= 1000 * 30){

                String distanceText = String.format("%.2f", distanceToNearest[0] / 1000);

                String text = "Merna stanica \"" + nearestLocationName + "\" je udaljena "
                        + distanceText + "km i postavljena je za Vašu lokaciju";

                Utilities.setLocation(nearestLocationId, mContext);

                Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
            }
            else {
                // If the distance to nearest is greater than 30km, just let them pick manually

                MainActivity mainActivity = (MainActivity) mContext;

                String snackbarText = String.format(
                        "Najbliža lokacija je %s na %.2fkm. Izaberite ručno",
                        nearestLocationName, distanceToNearest[0]/1000);
                final Snackbar snackbar = Snackbar.make(
                        mainActivity.findViewById(R.id.main_pager),
                        snackbarText,
                        Snackbar.LENGTH_LONG);

                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                        // TODO launch the location picker here
                    }
                };
                snackbar.setAction("Izaberi ručno", listener);

                snackbar.show();
            }
        }
    }
}
