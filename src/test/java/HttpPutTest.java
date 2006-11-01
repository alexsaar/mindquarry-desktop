import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class HttpPutTest extends TestCase {
    public void testPutOperation() throws HttpException, IOException {
        HttpClient httpClient = new HttpClient();
        httpClient.getState().setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT,
                        AuthScope.ANY_REALM),
                new UsernamePasswordCredentials("", ""));

        PutMethod put = new PutMethod(
                "http://172.16.5.144:8888/resources/client/test.xml");
        put.setDoAuthentication(true);
        put.setRequestEntity(new ByteArrayRequestEntity("test2".getBytes()));

        httpClient.executeMethod(put);
        System.out.println("Returned status: " + put.getStatusCode());
        System.out.println(put.getResponseBodyAsString());

        put.releaseConnection();
    }
}
