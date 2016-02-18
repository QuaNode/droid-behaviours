package android.com.behaviours_sdk.API;

/**
 * Created by Mohammed on 12/15/2015.
 */

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.POST;

public interface BehavioursEndPoints {

    @GET("/behaviours")
    public void getBehaviours(Callback<Response> response);
    @POST("/behaviours")
    public void getBehaviours(Callback<Response> response);
}