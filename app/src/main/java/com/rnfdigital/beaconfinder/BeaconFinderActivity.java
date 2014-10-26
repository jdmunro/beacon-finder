package com.rnfdigital.beaconfinder;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.List;


public class BeaconFinderActivity extends Activity {

    private static final String LOG_TAG = "BeaconFinderActivity";
    private static final String UUID = "d2d27af6-fca8-4086-ac0d-3b90e4f2d372";
    private static final Region BEACON_REGION = new Region("regionId", UUID, null, null);
    private BeaconManager beaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_finder);
        prepareForRangingBeacons();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startRangingForBeacons();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRangingForBeacons();
    }

    private void prepareForRangingBeacons() {
        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
                Log.d(LOG_TAG, "Ranged beacons: " + beacons);
            }
        });
    }

    private void startRangingForBeacons() {
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override public void onServiceReady() {
                try {
                    beaconManager.startRanging(BEACON_REGION);
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "Cannot start ranging", e);
                }
            }
        });
    }

    private void stopRangingForBeacons() {
        try {
            beaconManager.stopRanging(BEACON_REGION);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Cannot stop but it does not matter now", e);
        }
    }
}
