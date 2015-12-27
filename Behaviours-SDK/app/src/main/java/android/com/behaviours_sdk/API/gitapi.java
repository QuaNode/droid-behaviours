package android.com.behaviours_sdk.API;

/**
 * Created by Mohammed on 12/15/2015.
 */

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;

public interface gitapi {
    //https://api.stackexchange.com/2.2/info?site=stackoverflow

//    @GET("/get/curators.json")
//    gitmodel getCurators(
//            @Query("api_key") String key
//    );

//    @GET("/users/{user")      //here is the other url part.best way is to start using /
//    public void getFeed(@Path("user") String user, Callback<gitModel> response);
//      //string user is for passing values from edittext for eg: user=basil2style,google
    //response is the response from the server which is now in the POJO

    @GET("/2.2/info?site=stackoverflow")      //here is the other url part.best way is to start using /
    public void getFeed( Callback<Response> response);

    @GET("/stack")      //here is the other url part.best way is to start using /
    public void getStack( Callback<Response> response);

}