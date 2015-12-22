package android.com.behaviours_sdk.Model;

import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;

import retrofit.client.Client;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

/**
 * Created by Mohammed on 12/18/2015.
 */
public class MockClient implements Client {

    @Override
    public Response execute(Request request) throws IOException {
        Uri uri = Uri.parse(request.getUrl());

        Log.d("MOCK SERVER", "fetching uri: " + uri.toString());

        String responseString = "";

        if(uri.getPath().equals("/path/user")) {
            responseString = "JSON STRING HERE";
        } else {
            responseString = "OTHER JSON RESPONSE STRING";
        }

        return new Response(request.getUrl(), 200, "nothing",
                Collections.EMPTY_LIST,
                new TypedByteArray("application/json", responseString.getBytes()));
    }
}