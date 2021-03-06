package com.toplion.cplusschool.Wangyi;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.toplion.cplusschool.Activity.BaseActivity;
import com.toplion.cplusschool.R;

/**
 * 加载网易云阅读及漫画的页面
 *
 * @author tengyy
 * @date 2016-8-12
 */
public class KaoLaActivity extends BaseActivity {
    private WebView webview;                // WebView
    private ProgressBar pb_webview;//等待框

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wangwebview);
        String url = getIntent().getStringExtra("url");
        pb_webview = (ProgressBar) findViewById(R.id.pb_webview);
        webview = (WebView) findViewById(R.id.about_webview);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);// 设置js可以直接打开窗口，如window.open()，默认为false
        // 设置WebView属性，能够执行Javascript脚本
        webSettings.setJavaScriptEnabled(true);
        // 设置可以访问文件
        webSettings.setAllowFileAccess(true);
        webSettings.setDomStorageEnabled(true);
        // 设置支持缩放
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDomStorageEnabled(true);//此属性是可以使webview 适应h5的各种标签(防止出现空白页面)
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);//不使用缓存
        //自适应屏幕
        webSettings.setUseWideViewPort(true);//设置此属性，可任意比例缩放
        webSettings.setLoadWithOverviewMode(true);
        // 设置Web视图
        webview.setWebViewClient(new webViewClient());
        // 加载需要显示的网页
        webview.loadUrl(url);

    }

    // Web视图
    private class webViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d("", "start->" + url);
            pb_webview.setVisibility(View.VISIBLE);
            webview.setEnabled(false);
            super.onPageStarted(view, url, favicon);
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("", "shouldOverrideUrlLoading->" + url);
            if (url.endsWith(".apk")) {
                Uri uri = Uri.parse(url);
                Intent viewIntent = new Intent(Intent.ACTION_VIEW, uri);
                KaoLaActivity.this.startActivity(viewIntent);
            } else {
                view.loadUrl(url);
            }
            return true;
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d("", "finish->" + url);
            //tv_webview_title.setText(view.getTitle());
            pb_webview.setVisibility(View.GONE);
            webview.setEnabled(true);
            super.onPageFinished(view, url);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
            webview.goBack(); // goBack()表示返回WebView的上一页面
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void init() {
    }

    @Override
    public void setListener() {
    }
}
