package com.exemplo.calculadora;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName adminComponent;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        adminComponent = new ComponentName(this, AdminReceiver.class);
        
        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("file:///android_asset/index.html");
    }
    
    private class AndroidBridge {
        @JavascriptInterface
        public boolean isAdminActive() {
            return devicePolicyManager.isAdminActive(adminComponent);
        }
        
        @JavascriptInterface
        public void requestAdmin() {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
            startActivity(intent);
        }
        
        @JavascriptInterface
        public void removeAdmin() {
            devicePolicyManager.removeActiveAdmin(adminComponent);
        }
        
        @JavascriptInterface
        public String getInstalledApps() {
            try {
                PackageManager pm = getPackageManager();
                List<ApplicationInfo> apps = pm.getInstalledApplications(0);
                JSONArray jsonApps = new JSONArray();
                
                for (ApplicationInfo app : apps) {
                    if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        JSONObject obj = new JSONObject();
                        obj.put("name", pm.getApplicationLabel(app).toString());
                        obj.put("package", app.packageName);
                        obj.put("enabled", app.enabled);
                        jsonApps.put(obj);
                    }
                }
                return jsonApps.toString();
            } catch (Exception e) {
                return "[]";
            }
        }
        
        @JavascriptInterface
        public void disableApp(String packageName) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                devicePolicyManager.setPackagesSuspended(adminComponent, new String[]{packageName}, true);
            }
        }
        
        @JavascriptInterface
        public void enableApp(String packageName) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                devicePolicyManager.setPackagesSuspended(adminComponent, new String[]{packageName}, false);
            }
        }
    }
    }
