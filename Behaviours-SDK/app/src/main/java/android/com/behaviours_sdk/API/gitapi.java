package android.com.behaviours_sdk.API;

/**
 * Created by Mohammed on 12/15/2015.
 */

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;

public interface gitapi {
    //https://api.stackexchange.com/2.2/info?site=stackoverflow

    @GET("/2.2/info?site=stackoverflow")      //here is the other url part.best way is to start using /
    public void getFeed( Callback<Response> response);

    @GET("/stack")      //here is the other url part.best way is to start using /
    public void getStack( Callback<Response> response);

}