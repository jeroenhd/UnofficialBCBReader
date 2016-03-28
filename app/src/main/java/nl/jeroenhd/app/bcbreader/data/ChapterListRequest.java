package nl.jeroenhd.app.bcbreader.data;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ChapterRequest
 * Stolen from https://developer.android.com/training/volley/request-custom.html
 */
public class ChapterListRequest extends Request<List<Chapter>> {
    private final Map<String, String> headers;
    private final Response.Listener<List<Chapter>> listener;

    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param url     URL of the request to make
     * @param headers Map of request headers
     */
    public ChapterListRequest(String url, Map<String, String> headers,
                              Response.Listener<List<Chapter>> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        this.headers = headers;
        this.listener = listener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected void deliverResponse(List<Chapter> response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<List<Chapter>> parseNetworkResponse(NetworkResponse response) {
        try {
            // Build a new Gson
            GsonBuilder builder = new GsonBuilder();
            // Prepare a valid list
            List<Chapter> chapterList = new ArrayList<>();
            // Have Gson use out ChapterListDeserializer for the chapter list
            builder.registerTypeAdapter(chapterList.getClass(), new ChapterListDeserializer());
            // Create the usable Gson object
            Gson gson = builder.create();

            // Get the downloaded JSON
            String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));

            // Decode the JSON
            //noinspection unchecked
            chapterList = gson.fromJson(json, chapterList.getClass());

            return Response.success(
                    chapterList,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            // Something went wrong with the encoding
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            // Bad JSON!
            return Response.error(new ParseError(e));
        }
    }
}