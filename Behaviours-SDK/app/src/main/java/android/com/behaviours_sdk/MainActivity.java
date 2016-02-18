package android.com.behaviours_sdk;

import android.com.behaviours_sdk.Model.MockClient;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Map;

import retrofit.RestAdapter;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;


//@ContentView(R.layout.activity_main)
public class MainActivity extends RoboActivity {

//    @Inject
//    Behaviours b;

    @InjectView(R.id.textView)
    private TextView textView;

    @InjectView(R.id.textView2)
    private TextView textView2;

    Map<String, Object> JSONMap ;

    final String API = "https://api.stackexchange.com/";                         //BASE URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);

        RestAdapter.Builder builder = new RestAdapter.Builder();
        builder.setClient(new MockClient());
        RestAdapter restAdapter =
                builder.setClient(new MockClient())
                        .setEndpoint(API)
                        .build();

        Behaviours behaviours = new Behaviours(restAdapter);


//        textView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
//            }
//        });


    }



}
