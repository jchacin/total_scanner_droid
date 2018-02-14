package com.estudioaj.codescanner;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.estudioaj.codescanner.util.GlobalVariables;

public class BrowserActivity extends AppCompatActivity {

    private WebView webView;
    private GlobalVariables global = new GlobalVariables();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_browser);

            Toast.makeText(this, R.string.redirecting, Toast.LENGTH_LONG).show();

            //get url from main activity
            Intent intent = getIntent();
            String reading = intent.getStringExtra("READING");

            //set toolbar
            Toolbar toolbar = (Toolbar) findViewById(R.id.include_toolbar);
            toolbar.setTitle(R.string.browser);
            setSupportActionBar(toolbar);

            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);

            //initialize web browser
            this.webView = (WebView) findViewById(R.id.web_view);
            webView.setWebViewClient(new WebViewClient() /*{
	            public boolean shouldOverrideUrlLoading(WebView view, String url){
			        view.loadUrl(url);
			        return false;
			    }
            }*/);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setVerticalScrollBarEnabled(false);
            webView.setHorizontalScrollBarEnabled(false);
            //set url on
            webView.loadUrl(reading);
        }catch (Exception ex){
            Toast.makeText(BrowserActivity.this, getString(R.string.problem_loading_link), Toast.LENGTH_LONG).show();
            global.setInitializedBrowser(false);
            finish();
        }
    }

    //go back if there are url or finish activity
    @Override
    public boolean onSupportNavigateUp() {
        if (this.webView.canGoBack()){
            webView.goBack();
        }else{
            global.setInitializedBrowser(false);
            this.finish();
        }
        return true;
    }
}
