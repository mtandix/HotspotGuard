package com.manish.hotspotguard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private Button btnToggle;
    private TextView tvStatus, tvNetworkInfo, tvDescription;
    private SharedPreferences prefs;
    private boolean isMonitoring = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("hotspot_guard", MODE_PRIVATE);
        isMonitoring = prefs.getBoolean("is_monitoring", false);

        btnToggle = findViewById(R.id.btnToggle);
        tvStatus = findViewById(R.id.tvStatus);
        tvNetworkInfo = findViewById(R.id.tvNetworkInfo);
        tvDescription = findViewById(R.id.tvDescription);

        updateUI();

        btnToggle.setOnClickListener(v -> {
            if (!isMonitoring) {
                startMonitoring();
            } else {
                stopMonitoring();
            }
        });

        // Check if Shizuku is available
        checkShizuku();
    }

    private void checkShizuku() {
        try {
            // Check if Shizuku service is running
            boolean shizukuAvailable = rikka.shizuku.Shizuku.pingBinder();
            if (!shizukuAvailable) {
                tvNetworkInfo.setText("⚠️ Shizuku not running!\nPlease start Shizuku first.");
                tvNetworkInfo.setTextColor(getColor(android.R.color.holo_red_light));
                btnToggle.setEnabled(false);
                btnToggle.setText("Shizuku Required");
            }
        } catch (Exception e) {
            tvNetworkInfo.setText("⚠️ Shizuku not found!\nInstall Shizuku from Play Store.");
            tvNetworkInfo.setTextColor(getColor(android.R.color.holo_red_light));
            btnToggle.setEnabled(false);
        }
    }

    private void startMonitoring() {
        isMonitoring = true;
        prefs.edit().putBoolean("is_monitoring", true).apply();

        Intent serviceIntent = new Intent(this, HotspotMonitorService.class);
        serviceIntent.setAction("START");
        ContextCompat.startForegroundService(this, serviceIntent);

        updateUI();
        Toast.makeText(this, "✅ Monitoring Started!", Toast.LENGTH_SHORT).show();
    }

    private void stopMonitoring() {
        isMonitoring = false;
        prefs.edit().putBoolean("is_monitoring", false).apply();

        Intent serviceIntent = new Intent(this, HotspotMonitorService.class);
        serviceIntent.setAction("STOP");
        startService(serviceIntent);

        updateUI();
        Toast.makeText(this, "🛑 Monitoring Stopped!", Toast.LENGTH_SHORT).show();
    }

    private void updateUI() {
        if (isMonitoring) {
            btnToggle.setText("🛑 Stop Monitoring");
            btnToggle.setBackgroundColor(getColor(android.R.color.holo_red_dark));
            tvStatus.setText("🟢 ACTIVE - Monitoring Network");
            tvStatus.setTextColor(getColor(android.R.color.holo_green_dark));
            tvDescription.setText("Hotspot will turn OFF when 5G drops to 4G/3G\nHotspot will turn ON when 5G returns");
        } else {
            btnToggle.setText("▶️ Start Monitoring");
            btnToggle.setBackgroundColor(getColor(android.R.color.holo_green_dark));
            tvStatus.setText("⚫ INACTIVE - Not Monitoring");
            tvStatus.setTextColor(getColor(android.R.color.darker_gray));
            tvDescription.setText("Press Start to begin automatic\nhotspot management based on 5G signal");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isMonitoring = prefs.getBoolean("is_monitoring", false);
        updateUI();
        checkShizuku();
    }
}
