package android.com.behaviours_sdk;

import android.com.behaviours_sdk.API.gitapi;
import android.com.behaviours_sdk.Model.BehaviourJSON;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/*Data hosted with ♥ by Pastebin.com-Download Raw-See Original
        package com.makeinfo.flowerpi;
*/

/*
    To be able to use RoboGuice’s annotations in your Android activities,
    their classes must extend RoboActivity instead of Activity.
    Similarly, if you want to use the annotations inside an Android service,
     its class must extend RoboService instead of Service.
 */
public class MainActivity extends ActionBarActivity {

    Button click;
    TextView tv;
    EditText edit_user;
    ProgressBar pbar;
    String API = "https://api.github.com";                         //BASE URL

    /*
        Normally, you would use the setContentView method and pass a layout resource to it in order to
        set the layout of an Activity. RoboGuice offers an alternative means to do the same thing,
         the @ContentView annotation.

        @ContentView(R.layout.activity_main)
        public class MainActivity extends RoboActivity {

        }
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        To initialize the two UI widgets defined in the XML in a RoboActivity, you could write the following:

            @InjectView(R.id.email)
            private TextView email;

            @InjectView(R.id.okay)
            private Button okay;
         */
         /*
            with mocking
            RestAdapter.Builder builder = new RestAdapter.Builder();
            builder.setClient(new MockClient());
          */
        //Retrofit section start from here...
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(API).build();                                        //create an adapter for retrofit with base url

            gitapi git = restAdapter.create(gitapi.class);                            //creating a service for adapter with our GET class

            //Now ,we need to call for response
            //Retrofit using gson for JSON-POJO conversion

            git.getFeed(new Callback<BehaviourJSON>() {
                @Override
                public void success(BehaviourJSON gitmodel, Response response) {
                    //we get json object from github server to our POJO or model class

                   // tv.setText("Github Name :"+gitmodel.getName()+"\nWebsite :"+gitmodel.getBlog()+"\nCompany Name :"+gitmodel.getCompany());

                    //pbar.setVisibility(View.INVISIBLE);                               //disable progressbar
                }

                @Override
                public void failure(RetrofitError error) {
                    tv.setText(error.getMessage());
                    pbar.setVisibility(View.INVISIBLE);                               //disable progressbar
                }
            });

    }


}
