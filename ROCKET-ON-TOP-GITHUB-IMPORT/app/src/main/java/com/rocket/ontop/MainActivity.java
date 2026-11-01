package com.rocket.ontop;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.WindowManager;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {
    private WebView webView;
    private PermissionRequest pendingRequest;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        webView = new WebView(this);
        setContentView(webView);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        webView.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return serveLocalAsset(request.getUrl().getPath());
            }
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                try {
                    android.net.Uri uri = android.net.Uri.parse(url);
                    if (!"rocket.local".equals(uri.getHost())) return super.shouldInterceptRequest(view, url);
                    return serveLocalAsset(uri.getPath());
                } catch (Exception e) { return super.shouldInterceptRequest(view, url); }
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onPermissionRequest(PermissionRequest request) {
                runOnUiThread(() -> {
                    boolean asksMic = false;
                    for (String r : request.getResources()) {
                        if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(r)) asksMic = true;
                    }
                    if (asksMic && checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        request.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});
                    } else if (asksMic) {
                        pendingRequest = request;
                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 501);
                    } else request.deny();
                });
            }
        });
        // Intercepted HTTPS origin gives WebAudio / getUserMedia a secure context.
        webView.loadUrl("https://rocket.local/index.html");
    }

    private WebResourceResponse serveLocalAsset(String path) {
        if (path == null || path.equals("/")) path = "/index.html";
        String relative = path.startsWith("/") ? path.substring(1) : path;
        if (relative.contains("..")) return new WebResourceResponse("text/plain", "UTF-8", null);
        String mime = "application/octet-stream";
        if (relative.endsWith(".html")) mime = "text/html";
        else if (relative.endsWith(".js")) mime = "application/javascript";
        else if (relative.endsWith(".css")) mime = "text/css";
        else if (relative.endsWith(".png")) mime = "image/png";
        else if (relative.endsWith(".jpg") || relative.endsWith(".jpeg")) mime = "image/jpeg";
        try {
            InputStream stream = getAssets().open("www/" + relative);
            Map<String, String> headers = new HashMap<>();
            headers.put("Access-Control-Allow-Origin", "https://rocket.local");
            headers.put("Cache-Control", "no-cache");
            return new WebResourceResponse(mime, null, 200, "OK", headers, stream);
        } catch (Exception e) {
            return new WebResourceResponse("text/plain", "UTF-8", null);
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == 501 && pendingRequest != null) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                pendingRequest.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});
            } else pendingRequest.deny();
            pendingRequest = null;
        }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        if (webView != null) { webView.loadUrl("about:blank"); webView.destroy(); }
        super.onDestroy();
    }
}
