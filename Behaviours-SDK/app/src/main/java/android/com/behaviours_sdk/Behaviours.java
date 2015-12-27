package android.com.behaviours_sdk;

import android.com.behaviours_sdk.API.gitapi;
import android.com.behaviours_sdk.Model.MockClient;
import android.util.Log;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Mohammed on 12/15/2015.
 */
public class Behaviours {
    private static Behaviours ourInstance = new Behaviours();
     String API = "https://api.stackexchange.com/";                         //BASE URL
    // String API = "/path/user";                         //BASE URL


    public static Behaviours getInstance() {

        return ourInstance;
    }

    private Behaviours() {
        initiateBehavoir();
    }

    private void initiateBehavoir(){

        RestAdapter.Builder builder = new RestAdapter.Builder();
        builder.setClient(new MockClient());
        //create an adapter for retrofit with base url
        RestAdapter restAdapter =
            builder.setClient(new MockClient()).build();

        gitapi git = restAdapter.create(gitapi.class);

        git.getFeed(new Callback<Response>() {
            @Override
            public void success(Response behaviourJSON, Response response) {
                Log.d("sucess" , "Data recieved " + behaviourJSON);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("fail" , "Fail to get data from server");
            }
        });

    }
    /*
        here it will return lamdaa expersion
        check name of behavoiud exist in the json

        the lmada will get data object
        and map the object to the header and make request
     */
    public Behaviours getBehavoir(String behavoirName){

        // name is exist in the josn or not (it will be like key)
        // return the map " value of this name "
        // return the map assigned to this name
        // and make a closure
        switch (behavoirName){
            case "one":{

                break;
            }

        }
        return  ourInstance;
    }

}
