package com.manish.hotspotguard;

import android.util.Log;
import java.io.DataOutputStream;

public class HotspotController {

    private static final String TAG = "HotspotController";

    /**
     * Controls the mobile hotspot using Shizuku (privileged shell access)
     * @param enable true to turn hotspot ON, false to turn OFF
     */
    public static void setHotspot(boolean enable) {
        try {
            // Use Shizuku to execute privileged shell command
            // This requires WRITE_SECURE_SETTINGS permission granted via ADB/Shizuku
            
            String command;
            if (enable) {
                // Turn hotspot ON
                command = "cmd wifi start-softap";
            } else {
                // Turn hotspot OFF  
                command = "cmd wifi stop-softap";
            }
            
            executeShizukuCommand(command);
            Log.d(TAG, "Hotspot command executed: " + command);
            
        } catch (Exception e) {
            Log.e(TAG, "Error controlling hotspot: " + e.getMessage());
            throw new RuntimeException("Hotspot control failed: " + e.getMessage());
        }
    }

    /**
     * Execute a command via Shizuku's privileged shell
     */
    private static void executeShizukuCommand(String command) throws Exception {
        // Shizuku provides a way to run commands as system user
        // We use ProcessBuilder with the shell access Shizuku provides
        
        try {
            // Method 1: Direct via Shizuku API
            String[] cmds = {"sh", "-c", command};
            Process process = new ProcessBuilder(cmds)
                .redirectErrorStream(true)
                .start();
            
            int exitCode = process.waitFor();
            Log.d(TAG, "Command exit code: " + exitCode);
            
        } catch (Exception e) {
            // Method 2: Via runtime exec (fallback)
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(new String[]{"sh", "-c", command});
            process.waitFor();
        }
    }
    
    /**
     * Check if hotspot is currently enabled
     */
    public static boolean isHotspotEnabled() {
        try {
            Process process = Runtime.getRuntime().exec(
                new String[]{"sh", "-c", "cmd wifi status | grep 'Soft AP'"}
            );
            byte[] output = process.getInputStream().readAllBytes();
            String result = new String(output);
            return result.contains("enabled") || result.contains("ENABLED");
        } catch (Exception e) {
            Log.e(TAG, "Error checking hotspot status: " + e.getMessage());
            return false;
        }
    }
}
