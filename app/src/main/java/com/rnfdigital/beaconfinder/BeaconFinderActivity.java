package com.rnfdigital.beaconfinder;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import java.util.List;


public class BeaconFinderActivity extends Activity {

    private static final String LOG_TAG = "BeaconFinderActivity";
    private static final String UUID = "d2d27af6-fca8-4086-ac0d-3b90e4f2d372";
    private static final Region BEACON_REGION = new Region("regionId", UUID, 16629, 25543);
    private static final long DEFAULT_SCAN_PERIOD_IN_MS = 150;
    private static final long MIN_TIME_BETWEEN_PROXIMITY_CHANGES_IN_MS = 250;

    private TextView beaconRangeTextView;
    private BeaconManager beaconManager;
    private Utils.Proximity lastKnownProximity;
    private long timeOfLastProximityUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_finder);
        beaconRangeTextView = (TextView) findViewById(R.id.beacon_range_text);
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

    private void handleBeaconProximity(Utils.Proximity proximity) {
        if (proximity != lastKnownProximity) {

        }

        beaconRangeTextView.setText(proximity.toString());
    }

    private void prepareForRangingBeacons() {
        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
                if (beacons.size() > 0) {
                    Beacon beacon = beacons.get(0);
                    handleBeaconProximity(Utils.computeProximity(beacon));
                } else {
                    handleBeaconProximity(Utils.Proximity.UNKNOWN);
                }
            }
        });
    }

    private void startRangingForBeacons() {
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.setForegroundScanPeriod(DEFAULT_SCAN_PERIOD_IN_MS, 0);
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
