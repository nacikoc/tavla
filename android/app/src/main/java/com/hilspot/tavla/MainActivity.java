package com.hilspot.tavla;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.UiModeManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity {

    private WebView web;
    private long lastBack = 0;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Cikintili (centikli) ekranlarda siyah serit birakmadan tam genislik kullan;
            // sayfa tarafinda env(safe-area-inset-*) ile icerik centigin altina girmez.
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false); // zar/pul sesleri için
        s.setAllowFileAccess(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        web.setBackgroundColor(0xFF191008);
        // Yan yuklenen cihazlarda devtools baglamak zor; bir JS hatasi sessiz siyah ekran
        // yerine "adb logcat -s TavlaWV" ciktisinda gorunsun.
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage m) {
                Log.i("TavlaWV", m.message() + " (" + m.sourceId() + ":" + m.lineNumber() + ")");
                return true;
            }
        });

        // Sayfa yalnizca yerel varliklardan olusur. Kunyedeki e-posta adresine
        // dokunulunca WebView'in kendisi mailto: acamaz; e-posta uygulamasina
        // devrediyoruz. Baska her turlu disari cikis engellenir.
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest req) {
                return disaLink(req.getUrl().toString());
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, String url) {
                return disaLink(url);
            }
        });

        setContentView(web);
        hideSystemUi();

        // Cihazin TV olup olmadigini sayfaya bildiriyoruz: TV'de kumanda odak halkasi
        // ACILISTA gorunur olmali (TV kalite sarti), telefonda gorunmemeli.
        boolean tv = false;
        try {
            UiModeManager um = (UiModeManager) getSystemService(UI_MODE_SERVICE);
            tv = um != null && um.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
        } catch (Throwable ignored) { }
        web.loadUrl("file:///android_asset/index.html" + (tv ? "#tv" : ""));
        web.requestFocus(); // TV kumandasi (D-pad) olaylari WebView'e gitsin

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::geriBas);
        }
    }

    /** true dondurursek WebView o adresi YUKLEMEZ; biz ele aliriz. */
    private boolean disaLink(String url) {
        if (url == null) return true;
        if (url.startsWith("file:///android_asset/")) return false;   // kendi sayfamiz
        if (url.startsWith("mailto:")) {
            try {
                startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse(url)));
            } catch (ActivityNotFoundException e) {
                // TV kutularinda genelde e-posta uygulamasi yok — adresi yaz
                Toast.makeText(this, "nakisoft@gmail.com", Toast.LENGTH_LONG).show();
            }
            return true;
        }
        return true;   // baska her sey engellenir
    }

    private void hideSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUi();
    }

    /**
     * Geri tusu: oyunda secimi iptal eder; 2 sn icinde ikinci basis uygulamadan cikar.
     *
     * targetSdk 36'da "predictive back" varsayilan acik oldugu icin Android 13+
     * cihazlarda onBackPressed() ARTIK CAGRILMAZ. Bu yuzden ayni mantik hem eski
     * yola (onBackPressed) hem de yeni dispatcher'a baglanir; yoksa yeni telefonlarda
     * ilk Geri basisi uygulamayi dogrudan kapatirdi.
     */
    private void geriBas() {
        web.evaluateJavascript(
                "document.dispatchEvent(new KeyboardEvent('keydown',{key:'Escape'}))", null);
        long now = System.currentTimeMillis();
        if (now - lastBack < 2000) {
            finish();
        } else {
            lastBack = now;
            Toast.makeText(this, "Çıkmak için tekrar Geri'ye basın", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        geriBas();   // Android 12 ve oncesi
    }

    @Override
    protected void onPause() {
        super.onPause();
        web.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        web.onResume();
    }

    @Override
    protected void onDestroy() {
        web.destroy();
        super.onDestroy();
    }
}
