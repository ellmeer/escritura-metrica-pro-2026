package com.escriturametrica.pro;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.webkit.WebViewAssetLoader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class MainActivity extends Activity {
    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;
    private static final int FILE_CHOOSER_REQUEST = 1470;
    private static final String HOME = "https://appassets.androidplatform.net/assets/index.html";

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(11, 15, 29));
        getWindow().setNavigationBarColor(Color.rgb(11, 15, 29));
        getWindow().getDecorView().setSystemUiVisibility(0);
        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(11, 15, 29));
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setSupportZoom(false);
        settings.setTextZoom(100);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        webView.addJavascriptInterface(new AndroidFiles(this), "AndroidFiles");

        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();
        webView.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                return !("https".equals(u.getScheme())
                    && "appassets.androidplatform.net".equals(u.getHost())
                    && u.getPath() != null && u.getPath().startsWith("/assets/"));
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = callback;
                try {
                    Intent intent = params.createIntent();
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST);
                    return true;
                } catch (ActivityNotFoundException ex) {
                    filePathCallback = null;
                    Toast.makeText(MainActivity.this,"No se encontró selector de archivos",Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        });
        webView.loadUrl(HOME);
    }

    @Override @SuppressWarnings("deprecation")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST && filePathCallback != null) {
            Uri[] results = null;
            if (resultCode == RESULT_OK && data != null) {
                ClipData clip = data.getClipData();
                if (clip != null && clip.getItemCount() > 0) {
                    results = new Uri[clip.getItemCount()];
                    for (int i=0; i<clip.getItemCount(); i++) results[i]=clip.getItemAt(i).getUri();
                } else if (data.getData() != null) {
                    results = new Uri[] {data.getData()};
                }
            }
            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
        }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
    @Override protected void onPause() {
        if(webView != null) webView.onPause();
        super.onPause();
    }
    @Override protected void onResume() {
        super.onResume();
        if(webView != null) webView.onResume();
    }
    @Override protected void onDestroy() {
        if(webView != null) { webView.removeJavascriptInterface("AndroidFiles"); webView.destroy(); webView=null; }
        super.onDestroy();
    }

    public static class AndroidFiles {
        private final Activity activity;
        AndroidFiles(Activity activity) { this.activity=activity; }

        @JavascriptInterface public void saveText(String name,String text,String mime) {
            if (text == null) return;
            saveBytes(name,text.getBytes(StandardCharsets.UTF_8),mime);
        }
        @JavascriptInterface public void saveBase64(String name,String base64,String mime) {
            if (base64 == null) return;
            try { saveBytes(name,Base64.decode(base64,Base64.DEFAULT),mime); }
            catch(Exception e) { toast("No se pudo exportar la imagen"); }
        }
        private String safeName(String n) {
            if(n == null) n="exportacion.json";
            n=n.replaceAll("[^a-zA-Z0-9._\\-]","_");
            if(n.length()>95) n=n.substring(n.length()-95);
            if(n.isEmpty()||n.startsWith(".")) n="exportacion.json";
            return n;
        }
        private void toast(String text) {
            activity.runOnUiThread(()->Toast.makeText(activity,text,Toast.LENGTH_SHORT).show());
        }
        private void saveBytes(String filename, byte[] data, String mime) {
            if(data.length > 25_000_000) {toast("El archivo es demasiado grande");return;}
            final String name=safeName(filename);
            final String media=(mime==null||mime.isEmpty())?"application/octet-stream":mime.split(";")[0];
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues values=new ContentValues();
                    values.put(MediaStore.Downloads.DISPLAY_NAME,name);
                    values.put(MediaStore.Downloads.MIME_TYPE,media);
                    values.put(MediaStore.Downloads.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS+"/EscrituraMetrica");
                    values.put(MediaStore.Downloads.IS_PENDING,1);
                    Uri uri=activity.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,values);
                    if(uri==null) throw new Exception("No se pudo crear el archivo");
                    try(OutputStream stream=activity.getContentResolver().openOutputStream(uri)) {
                        if(stream==null) throw new Exception("No se pudo escribir");
                        stream.write(data);
                    }
                    values.clear();values.put(MediaStore.Downloads.IS_PENDING,0);
                    activity.getContentResolver().update(uri,values,null,null);
                    toast("Guardado en Descargas/EscrituraMetrica");
                } else {
                    File dir=new File(activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),"EscrituraMetrica");
                    if(!dir.exists()&&!dir.mkdirs()) throw new Exception("Sin carpeta");
                    try(OutputStream stream=new FileOutputStream(new File(dir,name))) {stream.write(data);}
                    toast("Guardado en archivos de la app");
                }
            } catch(Exception e) {toast("No se pudo guardar la copia: "+e.getClass().getSimpleName());}
        }
    }
}
