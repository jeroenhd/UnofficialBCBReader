package nl.jeroenhd.app.bcbreader.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.net.ssl.SSLException;

/**
 * A credential container for BCI logins
 */

public class BCICredentials {
    private String username;
    private String password;
    private String authCookie;

    /**
     * Create credentials based on user login
     * @param username The username to authenticate with
     * @param password The password to authenticate with
     */
    public BCICredentials(String username, String password)
    {
        this.username = username;
        this.password = password;
    }

    /**
     * Fetch credentials from credential storage
     * @return Stored BCI credentials
     */
    public static BCICredentials fetch(Context context)
    {
        BCICredentials credentials = new BCICredentials("","");
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        String cookie = preferences.getString("bci_auth_cookie","");
        if (cookie.equals(""))
        {
            credentials.authCookie = null;
        } else {
            credentials.authCookie = cookie;
        }

        return credentials;
    }

    /**
     * Save the auth cookie
     */
    public void SaveCredentials(Context context)
    {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString("bci_auth_cookie", this.authCookie)
                .apply();
    }

    /**
     * Check if the BCI credentials have been confirmed to work
     * @return Whether or not there is an auth token
     */
    public boolean isAuthenticated()
    {
        return authCookie == null;
    }

    /**
     * Invalidate the auth token
     */
    public void Invalidate()
    {
        //TODO: send a logout

        this.authCookie = null;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getAuthCookie() {
        return authCookie;
    }

    /**
     * Checks if the token or the username/password combination are correct
     * @param context The context to use for creating the network connections
     * @return true if the credentials are OK, false if not
     * @throws SSLException In case something goes wrong securing the connection
     * @throws IOException Indicates that there was a networking problem
     * @throws Throwable For throwing the Volley exceptions
     */
    public boolean Validate(Context context) throws SSLException, IOException, Throwable
    {
        if (isAuthenticated()) {
            try {
                NetworkResponse response =
                        SuperSingleton.getInstance(context)
                        .getVolleyNetwork()
                        .performRequest(new StringRequest(API.BCI_VALIDATION_URL, null, null));

                if (ValidateHTML(new String(response.data, "UTF-8")))
                    return true;
            } catch (VolleyError volleyError) {
                volleyError.printStackTrace();
                throw volleyError.getCause();
            }
        }

        if (username == null || password == null)
            return false;

        // In case there is no token or the token auth failed
        NetworkResponse response = SuperSingleton
                .getInstance(context)
                .getVolleyNetwork()
                .performRequest(new StringRequest(Request.Method.POST, API.BCI_VALIDATION_URL, null, null){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        HashMap<String, String> params = new HashMap<>();
                        params.put("cardno", username);
                        params.put("password", password);
                        return params;
                    }


                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String,String> params = new HashMap<>();
                        params.put("Content-Type","application/x-www-form-urlencoded");
                        return params;
                    }
                });
        if (ValidateHTML(new String(response.data, "UTF-8")))
        {
            //TODO: check if this works with multiple cookies
            //The BCB members page sets three cookies: BCBBCINo, BCBBCIAuth and BCBBCINo again
            this.authCookie = response.headers.get("Set-Cookie");

            this.SaveCredentials(context);

            return true;
        }
        return false;
    }

    /**
     * Check if the HTML of the members page is the login page or not
     * @param HTMLResponse The HTML of the network response
     * @return false if the page is asking for a username/password, true if the page is listing BCI chapters
     */
    private boolean ValidateHTML(String HTMLResponse)
    {
        // Right now: simple check
        return !HTMLResponse.contains("Sign In");

        //TODO: implement this?
        //return BCIChapter.getBCIChapters(HTMLResponse).size() > 0;
    }

}
