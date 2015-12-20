package android.com.behaviours_sdk.API;

/**
 * Created by Mohammed on 12/15/2015.
 */

import android.com.behaviours_sdk.Model.BehaviourJSON;

import retrofit.Callback;
import retrofit.http.GET;

public interface gitapi {

//    @GET("/get/curators.json")
//    gitmodel getCurators(
//            @Query("api_key") String key
//    );

//    @GET("/users/{user")      //here is the other url part.best way is to start using /
//    public void getFeed(@Path("user") String user, Callback<gitModel> response);
//      //string user is for passing values from edittext for eg: user=basil2style,google
    //response is the response from the server which is now in the POJO

    @GET("/users")      //here is the other url part.best way is to start using /
    public void getFeed( Callback<BehaviourJSON> response);

}