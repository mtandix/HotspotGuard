package com.manish.hotspotguard;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.core.app.NotificationCompat;

public class HotspotMonitorService extends Service {

    private static final String TAG = "HotspotGuard";
    private static final String CHANNEL_ID = "hotspot_guard_channel";
    private static final int NOTIFICATION_ID = 1001;

    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private Handler handler;
    private boolean isRunning = false;
    private boolean was5G = false;
    private boolean hotspotEnabled = false;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;

        String action = intent.getAction();
        if ("START".equals(action)) {
            startMonitoring();
        } else if ("STOP".equals(action)) {
            stopMonitoring();
            stopSelf();
        }
        return START_STICKY;
    }

    private void startMonitoring() {
        if (isRunning) return;
        isRunning = true;

        startForeground(NOTIFICATION_ID, buildNotification("Monitoring network...", "Watching for 5G signal changes"));

        // Check initial state
        was5G = is5GConnected();
        Log.d(TAG, "Initial state - 5G: " + was5G);

        // Register network callback
        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities capabilities) {
                super.onCapabilitiesChanged(network, capabilities);
                handleNetworkChange(capabilities);
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                handleNetworkLost();
            }
        };

        connectivityManager.registerNetworkCallback(request, networkCallback);
        Log.d(TAG, "Network monitoring started");
    }

    private void handleNetworkChange(NetworkCapabilities capabilities) {
        boolean current5G = capabilities.hasTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);

        // More accurate 5G detection
        boolean isNR = false;
        try {
            // Check for 5G NR transport
            android.telephony.TelephonyManager tm = 
                (android.telephony.TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            int networkType = tm.getDataNetworkType();
            isNR = (networkType == android.telephony.TelephonyManager.NETWORK_TYPE_NR);
        } catch (Exception e) {
            Log.e(TAG, "Error checking network type: " + e.getMessage());
        }

        Log.d(TAG, "Network change - 5G NR: " + isNR + " | was5G: " + was5G);

        if (was5G && !isNR) {
            // Dropped from 5G to something lower
            Log.d(TAG, "5G LOST - Turning hotspot OFF");
            updateNotification("📉 5G Lost - Hotspot OFF", "Dropped to 4G/3G. Hotspot disabled.");
            setHotspotEnabled(false);
            was5G = false;
        } else if (!was5G && isNR) {
            // 5G returned
            Log.d(TAG, "5G RETURNED - Turning hotspot ON");
            updateNotification("📶 5G Back - Hotspot ON", "5G signal restored. Hotspot enabled.");
            setHotspotEnabled(true);
            was5G = true;
        }
    }

    private void handleNetworkLost() {
        if (was5G) {
            Log.d(TAG, "Network lost - Turning hotspot OFF");
            updateNotification("📵 No Network - Hotspot OFF", "Network lost. Hotspot disabled.");
            setHotspotEnabled(false);
            was5G = false;
        }
    }

    private boolean is5GConnected() {
        try {
            android.telephony.TelephonyManager tm = 
                (android.telephony.TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            int networkType = tm.getDataNetworkType();
            return networkType == android.telephony.TelephonyManager.NETWORK_TYPE_NR;
        } catch (Exception e) {
            return false;
        }
    }

    private void setHotspotEnabled(boolean enabled) {
        try {
            // Using Shizuku to run privileged command
            HotspotController.setHotspot(enabled);
            hotspotEnabled = enabled;
            Log.d(TAG, "Hotspot set to: " + enabled);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set hotspot: " + e.getMessage());
            updateNotification("❌ Error", "Failed to control hotspot. Check Shizuku.");
        }
    }

    private void stopMonitoring() {
        isRunning = false;
        if (networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering callback: " + e.getMessage());
            }
            networkCallback = null;
        }
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "HotspotGuard",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Monitors 5G network for hotspot control");
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification(String title, String content) {
        Intent notifIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notifIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(android.R.drawable.ic_menu_share)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    private void updateNotification(String title, String content) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, buildNotification(title, content));
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMonitoring();
    }
}
