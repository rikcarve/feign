package carve.feign;

import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.MDC;

import feign.Feign;
import feign.Logger.Level;
import feign.RequestLine;
import feign.RequestTemplate;
import feign.jackson.JacksonDecoder;
import feign.ribbon.LoadBalancingTarget;
import feign.slf4j.Slf4jLogger;

@Path("/feign")
public class FeignWorldResource {

    interface HelloWorld {
        @RequestLine("GET /hello/v1/hello/world")
        World world();
    }

    static {
        System.setProperty("ribbon.NIWSServerListClassName", "carve.feign.ConsulServerList");
    }

    @GET
    @Path("/world")
    @Produces("text/plain")
    public String world() {
        MDC.put("REQUEST-ID", UUID.randomUUID().toString());
        HelloWorld helloWorld = Feign.builder()
                .decoder(new JacksonDecoder())
                .logger(new Slf4jLogger(FeignWorldResource.class))
                .logLevel(Level.BASIC)
                .requestInterceptor((RequestTemplate template) -> {
                    MDC.getCopyOfContextMap().forEach((k, v) -> template.header("X-MDC-" + k, v));
                })
                .target(LoadBalancingTarget.create(HelloWorld.class, "http://hello"));
        World world = helloWorld.world();
        return world.getHelloWorld();
    }
}
