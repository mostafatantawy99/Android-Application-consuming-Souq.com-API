package com.ahnaser.myfirstapp.activities;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ahnaser.myfirstapp.MyApplication;
import com.ahnaser.myfirstapp.R;
import com.ahnaser.myfirstapp.extras.L;
import com.ahnaser.souqapi.SouqAPIConnection;
import com.android.volley.VolleyError;

import org.json.JSONObject;

public class OauthExample extends ActionBarActivity {

    private WebView webView;
    private ProgressDialog pd;
    private SouqAPIConnection connection;
    private String authorizationToken;

    private String redirectUri="https://api.souq.com/oauth/authorize/";
    private String scopes="customer_profile,cart_management,customer_demographics,customer_profile,cart_management,customer_demographics";

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth_example);

        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor=sharedPreferences.edit();

        OAuth();


    }

    void OAuth(){
        connection=new SouqAPIConnection(MyApplication.CLIENT_ID,MyApplication.API_KEY_SOUQ,this);

        webView=(WebView) findViewById(R.id.webView);
        webView.requestFocus(View.FOCUS_DOWN);
        pd = ProgressDialog.show(this, "", "Loading...",true);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                //This method will be executed each time a page finished loading.
                //The only we do is dismiss the progressDialog, in case we are showing any.
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, String url) {
                if (url.startsWith("https://api.souq.com/oauth/authorize/")) {
                    Log.i("Authorize", "");
                    Uri uri = Uri.parse(url);

                    authorizationToken = uri.getQueryParameter("code");
                    if (authorizationToken == null) {
                        Log.i("Authorize", "The user doesn't allow authorization.");
                        return true;
                    }
                    Log.i("Authorize", "Auth token received: " + authorizationToken);

                    connection.setResponseObserver(new SouqAPIConnection.ResponseObserver() {
                        @Override
                        public void onError(VolleyError error) {
                            Log.v("STATUS CODE: ",Integer.toString(error.networkResponse.statusCode));
                        }

                        @Override
                        public void onSuccess(JSONObject response, int statusCode) {
                            L.t(getApplicationContext(), "VALUE: " + connection.getAccessToken().getValue());
                            L.t(getApplicationContext(), "CUSTOMER ID: " + connection.getAccessToken().getCustomerId());
                            editor.putString("customer_id",connection.getAccessToken().getCustomerId());
                            editor.putString("value", connection.getAccessToken().getValue());
                            editor.apply();
                            OauthExample.this.finish();
                        }
                    });

                    connection.setAccessTokenFromServer(authorizationToken, redirectUri, scopes);

                } else {
                    Log.i("Authorize", "Redirecting to: " + url);
                    webView.loadUrl(url);
                }
                return true;
            }
        });

        String authUrl = connection.getAuthenticationUrl("https://api.souq.com/oauth/authorize/",scopes);
        Log.i("Authorize", "Loading Auth Url: " + authUrl);
        //Load the authorization URL into the webView
        webView.loadUrl(authUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_oauth_example, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
