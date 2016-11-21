package carve.feign;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/v1/hello")
public interface HelloService {

    @GET
    @Path("/world")
    @Produces(MediaType.APPLICATION_JSON)
    public World sayWorld();

}
