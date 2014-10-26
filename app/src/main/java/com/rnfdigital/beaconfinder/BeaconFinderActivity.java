package com.rnfdigital.beaconfinder;

import android.app.Activity;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class BeaconFinderActivity extends Activity {

    private static final String LOG_TAG = "BeaconFinderActivity";
    private static final String UUID = "d2d27af6-fca8-4086-ac0d-3b90e4f2d372";
    private static final Region BEACON_REGION = new Region("regionId", UUID, 16629, 25543);
    private static final long DEFAULT_SCAN_PERIOD_IN_MS = 500;
    private static final long MIN_TIME_BETWEEN_PROXIMITY_CHANGES_IN_MS = 500;

    private TextView beaconRangeTextView;
    private BeaconManager beaconManager;
    private Utils.Proximity lastKnownProximity = Utils.Proximity.UNKNOWN;
    private long timeOfLastProximityUpdate = 0;
    private ToneGenerator toneGenerator;
    private Timer beepTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_finder);
        beaconRangeTextView = (TextView) findViewById(R.id.beacon_range_text);
        toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        prepareForRangingBeacons();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        performCleanup();
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

    private boolean hasMinimumTimeSinceLastUpdateElapsed() {
        return System.currentTimeMillis() - timeOfLastProximityUpdate >= MIN_TIME_BETWEEN_PROXIMITY_CHANGES_IN_MS;
    }

    private void handleBeaconProximity(Utils.Proximity proximity) {
        if (proximity != lastKnownProximity) {
            if (hasMinimumTimeSinceLastUpdateElapsed()) {
                beaconRangeTextView.setText(proximity.toString());
                lastKnownProximity = proximity;
                handleBeaconProximityDidChange(proximity);
                Log.d(LOG_TAG, "Updating beacon proximity: " + proximity);
            }
        }

        timeOfLastProximityUpdate = System.currentTimeMillis();
    }

    private long calculateBeepIntervalForProximity(Utils.Proximity proximity) {
        switch (proximity) {
            case IMMEDIATE:
                return 500;
            case NEAR:
                return 1000;
            case FAR:
                return 2000;
            default:
                return 0;
        }
    }

    private void startBeepingWithInterval(long interval) {
        beepTimer = new Timer();
        beepTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100);
            }
        }, 0, interval);
    }

    private void handleBeaconProximityDidChange(Utils.Proximity proximity) {
        cancelBeeping();

        if (proximity != Utils.Proximity.UNKNOWN) {
            final long beepInterval = calculateBeepIntervalForProximity(proximity);
            startBeepingWithInterval(beepInterval);
        }
    }

    private void cancelBeeping() {
        if (beepTimer != null) {
            beepTimer.cancel();
            beepTimer.purge();
            beepTimer = null;
        }
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

    private void performCleanup() {
        cancelBeeping();
        beaconManager.disconnect();
    }
}
