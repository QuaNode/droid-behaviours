package android.com.behaviours_sdk;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;

import java.net.URL;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }


    public void test() throws Exception {
        // Create a MockWebServer. These are lean enough that you can create a new
        // instance for every unit test.
        MockWebServer server = new MockWebServer();

        // Schedule some responses.
        server.enqueue(new MockResponse().setBody("hello, world!"));

        // Start the server.
        server.play();

        // Ask the server for its URL. You'll need this to make HTTP requests.
        URL baseUrl = server.getUrl("/v1/chat/");
        // Exercise your application code, which should make those HTTP requests.
        // Responses are returned in the same order that they are enqueued.
//        Chat chat = new Chat(baseUrl);
//
//        chat.loadMore();
//        assertEquals("hello, world!", chat.messages());

        // Shut down the server. Instances cannot be reused.
        server.shutdown();
    }

}