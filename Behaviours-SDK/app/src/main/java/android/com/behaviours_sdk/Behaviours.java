package android.com.behaviours_sdk;

import android.com.behaviours_sdk.API.gitapi;
import android.com.behaviours_sdk.Model.BehaviourJSON;
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
    String API = "https://api.github.com";                         //BASE URL


    public static Behaviours getInstance() {

        return ourInstance;
    }

    private Behaviours() {
        initiateBehavoir();
    }

    private void initiateBehavoir(){

        //create an adapter for retrofit with base url
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(API).build();


        gitapi git = restAdapter.create(gitapi.class);

        git.getFeed(new Callback<BehaviourJSON>() {
            @Override
            public void success(BehaviourJSON behaviourJSON, Response response) {
                //we get json object from github server to our POJO or model class

                Log.d("sucess" , "Data recieved " + behaviourJSON.getName());
               /* tv.setText("Github Name :"+gitmodel.getName()+"\nWebsite :"
                        + gitmodel.getBlog()+"\nCompany Name :"+gitmodel.getCompany());
                pbar.setVisibility(View.INVISIBLE);*/                               //disable progressbar
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("fail" , "Fail to get data from server");
            }
        });

    }
    public Behaviours getBehavoir(String behavoirName){

        switch (behavoirName){
            case "one":{

                break;
            }

        }
        return  ourInstance;
    }

}
