package android.com.behaviours_sdk;

import android.com.behaviours_sdk.API.gitapi;
import android.com.behaviours_sdk.Model.MockClient;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

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

        //   with mocking
        RestAdapter.Builder builder = new RestAdapter.Builder();
        builder.setClient(new MockClient());

        //create an adapter for retrofit with base url
        RestAdapter restAdapter =
                builder.setClient(new MockClient())
                        .setEndpoint(API)
                        .build();

        gitapi git = restAdapter.create(gitapi.class);
        //Now ,we need to call for response
        //Retrofit using gson for JSON-POJO conversion
        git.getStack(new Callback<Response>() {
            @Override
            public void success(Response response, Response ignore) {

                // another one is
                  String bodyString = new String(((TypedByteArray) response.getBody()).getBytes());

                  textView.setText(bodyString.toString());
//
//                TypedInput body = response.getBody();
//                try {
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(body.in()));
//                    StringBuilder out = new StringBuilder();
//                    String newLine = System.getProperty("line.separator");
//                    String line;
//                    while ((line = reader.readLine()) != null) {
//                        out.append(line);
//                        out.append(newLine);
//                    }
//
//                    Log.d("JSON", out.toString());
//                    textView.setText(out.toString());
//                    Log.d("JSON", out.toString());
//                    // Prints the correct String representation of body.
//                    System.out.println(out);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

                // JSON to Map
                /*
                java.lang.reflect.Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
                Gson gson = new Gson();
                Map<String, Object> categoryicons = gson.fromJson(JSON.toString(), mapType);
                */
                /* another method
                  ObjectMapper mapper = new ObjectMapper();
                    HashMap<String,Object> result =
                            new HashMap<String, Object>().readValue(out, HashMap.class);
                 */

            }

            @Override
            public void failure(RetrofitError error) {
                textView.setText("Failed to get it ");
            }
        });
    }


}
