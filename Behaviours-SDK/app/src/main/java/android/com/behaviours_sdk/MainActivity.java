package android.com.behaviours_sdk;

import android.com.behaviours_sdk.API.gitapi;
import android.com.behaviours_sdk.Model.MockClient;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;


//@ContentView(R.layout.activity_main)
public class MainActivity extends RoboActivity {

//    @Inject
//    Behaviours b;

    @InjectView(R.id.textView)
    private TextView textView;
    Map<String, Object> JSONMap ;

    final String API = "https://api.stackexchange.com/";                         //BASE URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getInfo();
            }
        });
      // i inject it in the upper of calss
      //  setActionBar(tool);
        //setSupportActionBar(tool);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        getInfo();

        return super.onOptionsItemSelected(item);
    }

    private void getInfo(){
        //Retrofit section start from here...
//        RestAdapter restAdapter = new RestAdapter.Builder()
//                .setEndpoint(API).build();
//        gitapi git = restAdapter.create(gitapi.class);

        RestAdapter.Builder builder = new RestAdapter.Builder();
        builder.setClient(new MockClient());
        RestAdapter restAdapter =
                builder.setClient(new MockClient())
                        .setEndpoint(API)
                        .build();

        gitapi git = restAdapter.create(gitapi.class);

        git.getStack(new Callback<Response>() {
            @Override
            public void success(Response response, Response ignore) {

                // another one is
                  String bodyString = new String(((TypedByteArray) response.getBody()).getBytes());

                  textView.setText(bodyString.toString());
                // JSON to Map
                java.lang.reflect.Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
                Gson gson = new Gson();
                JSONMap = gson.fromJson(bodyString.toString(), mapType);

            }

            @Override
            public void failure(RetrofitError error) {
                textView.setText("Failed to get it ");
            }
        });
    }


}
