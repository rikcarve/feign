package carve.feign;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jaxrs.JAXRSContract;

@Path("/feign")
public class FeignResource {

    @GET
    @Path("world")
    @Produces("text/plain")
    public String world() {
    	HelloService helloService = Feign.builder()
    			.contract(new JAXRSContract())
    			.decoder(new JacksonDecoder())
    			.target(HelloService.class, "http://192.168.99.100:9001/hello");
        return helloService.sayWorld().getHelloWorld();
    }

}
