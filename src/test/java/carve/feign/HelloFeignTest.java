package carve.feign;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.MDC;

import feign.Feign;
import feign.FeignException;
import feign.Logger.Level;
import feign.Request.Options;
import feign.RequestLine;
import feign.RequestTemplate;
import feign.jackson.JacksonDecoder;
import feign.ribbon.RibbonClient;
import feign.slf4j.Slf4jLogger;

public class HelloFeignTest {

    interface HelloWorld {
        @RequestLine("GET /hello/v1/hello/world")
        World world();
    }

    @BeforeClass
    public static void initStatic() {
        System.setProperty("ribbon.NIWSServerListClassName", "carve.feign.ConsulServerList");
    }

    @Test
    public void testWorld() {
        HelloWorld helloWorld = Feign.builder()
                .decoder(new JacksonDecoder())
                .logger(new Slf4jLogger(HelloFeignTest.class))
                .logLevel(Level.BASIC)
                .target(HelloWorld.class, "http://192.168.99.100:32768");
        World world = helloWorld.world();
        System.out.println(world.getHelloWorld());
    }

    @Test
    public void testWorldHeader() {
        MDC.put("TEST", "Value1");
        HelloWorld helloWorld = Feign.builder()
                .decoder(new JacksonDecoder())
                .logger(new Slf4jLogger(HelloFeignTest.class))
                .logLevel(Level.HEADERS)
                .requestInterceptor((RequestTemplate template) -> {
                    MDC.getCopyOfContextMap().forEach((k, v) -> template.header("X-MDC-" + k, v));
                })
                .target(HelloWorld.class, "http://192.168.99.100:32768");
        World world = helloWorld.world();
        System.out.println(world.getHelloWorld());
    }

    // @Test
    // public void testHystrix() {
    // HelloWorld helloWorld = HystrixFeign.builder()
    // .decoder(new JacksonDecoder())
    // .logger(new Slf4jLogger(HelloFeignTest.class))
    // .logLevel(Level.BASIC)
    // .retryer(Retryer.NEVER_RETRY)
    // .target(LoadBalancingTarget.create(HelloWorld.class, "http://hello"), ()
    // -> new World("Fallback!"));
    // World world = helloWorld.world();
    // for (int i = 0; i < 10; i++) {
    // System.out.println(world.getHelloWorld());
    // try {
    // Thread.sleep(1000);
    // } catch (InterruptedException e) {
    // e.printStackTrace();
    // }
    // world = helloWorld.world();
    // }
    // }

    @Test
    public void testRibbon() {
        HelloWorld helloWorld = Feign.builder()
                .client(RibbonClient.create())
                .decoder(new JacksonDecoder())
                .logger(new Slf4jLogger(HelloFeignTest.class))
                .logLevel(Level.BASIC)
                .target(HelloWorld.class, "http://hello");

        World world = helloWorld.world();
        for (int i = 0; i < 10; i++) {
            System.out.println(world.getHelloWorld());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            world = helloWorld.world();
        }
    }

    @Test
    public void testJaxRs() {
        Response response = ClientBuilder.newClient()
                .register(ResolveHostFilter.class)
                .target("http://hello").request().get();
        System.out.println(response.getStatus());
        System.out.println(response.getEntity());
    }

    @Test(expected = FeignException.class)
    public void testTimeout() {
        System.out.println("start");
        HelloWorld helloWorld = Feign.builder()
                .decoder(new JacksonDecoder())
                .options(new Options(100, 1000))
                .target(HelloWorld.class, "http://192.168.99.100:32766");
        helloWorld.world();
    }

}
