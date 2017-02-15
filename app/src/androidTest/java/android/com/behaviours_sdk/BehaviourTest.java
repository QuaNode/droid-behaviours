package android.com.behaviours_sdk;

import android.annotation.TargetApi;
import android.app.Application;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.test.ApplicationTestCase;
import android.com.behaviours_sdk.Behaviours;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.mockito.BDDMockito.given;

import org.junit.Test;
/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */

public class BehaviourTest extends ApplicationTestCase<Application>  {

    Behaviours behaviors ;
    public BehaviourTest() {

        super(Application.class);

    }

    public void testInitiateBehaviours(){

        behaviors = new Behaviours(new GETURLFunction() {
            @Nullable
            @Override
            public URL apply(@Nullable String path) throws IOException {

                switch (path) {

                    case "/behaviours":{

                        String json = "{'login':{'name':'login','path':'/login','method': 'POST','paramew ters': {'password': {'key': 'user.password','type': 'body'},'username': {'key': 'user.username','type': 'body'}}},'register': {'name':'register','path': '/register','method': 'POST','parameters': {'password': {'key': 'password','type': 'header'},'username': {'key': 'username','type': 'header'},'email': {'key': 'user.email','type': 'header' }}}}";
                        return getMockUrl(json);
                    }
                }
                return  null;
            }

            @Override
            public boolean equals(@Nullable Object var1) {
                return false;
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static URL getMockUrl(final String JSONStr) throws IOException {

        assertTrue("Mock JSON is NULL not found", JSONStr.isEmpty());
        final URLConnection mockConnection = Mockito.mock(URLConnection.class);
        InputStream stream = new ByteArrayInputStream(JSONStr.getBytes(StandardCharsets.UTF_8));
        given(mockConnection.getInputStream()).willReturn(stream);
        final URLStreamHandler handler = new URLStreamHandler() {

            @Override
            protected URLConnection openConnection(final URL arg0)
                    throws IOException {
                return mockConnection;
            }
        };
        final URL url = new URL("", "", 0, "", handler);
        return url;
    }
}