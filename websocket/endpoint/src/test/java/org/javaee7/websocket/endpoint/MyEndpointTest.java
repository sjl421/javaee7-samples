package org.javaee7.websocket.endpoint;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

/**
 * @author Arun Gupta
 */
@RunWith(Arquillian.class)
public class MyEndpointTest {
    final String TEXT = "Hello World!";

    @ArquillianResource
    URI base;

    @Deployment(testable=false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(MyEndpoint.class,
                        MyEndpointTextClient.class,
                        MyEndpointBinaryClient.class);
    }
    
    @Test
    public void testTextEndpoint() throws URISyntaxException, DeploymentException, IOException, InterruptedException {
        MyEndpointTextClient.latch = new CountDownLatch(1);
        Session session = connectToServer(MyEndpointTextClient.class);
        assertNotNull(session);
        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String text) {
                MyEndpointTextClient.latch.countDown();
                assertEquals(TEXT, text);
            }
        });
        assertTrue(MyEndpointTextClient.latch.await(2, TimeUnit.SECONDS));
    }
    
    @Test
    public void testBinaryEndpoint() throws URISyntaxException, DeploymentException, IOException, InterruptedException {
        MyEndpointBinaryClient.latch = new CountDownLatch(1);
        Session session = connectToServer(MyEndpointBinaryClient.class);
        assertNotNull(session);
        session.addMessageHandler(new MessageHandler.Whole<ByteBuffer>() {
            @Override
            public void onMessage(ByteBuffer binary) {
                MyEndpointBinaryClient.latch.countDown();
                assertEquals(TEXT, binary);
            }
        });
        assertTrue(MyEndpointBinaryClient.latch.await(2, TimeUnit.SECONDS));
    }

    public Session connectToServer(Class endpoint) throws DeploymentException, IOException, URISyntaxException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        URI uri = new URI("ws://"
                        + base.getHost()
                        + ":"
                        + base.getPort()
                        + "/"
                        + base.getPath()
                        + "/websocket");
        System.out.println("Connecting to: " + uri);
        return container.connectToServer(endpoint, uri);
    }
}