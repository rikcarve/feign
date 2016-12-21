package carve.feign;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.health.ServiceHealth;

public class ResolveHostFilter implements ClientRequestFilter {

    private static Consul consul = Consul.builder().withHostAndPort(HostAndPort.fromParts("192.168.99.100", 8500))
            .build();

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        String host = requestContext.getUri().getHost();
        System.out.println(host);
        String newHost = getUpdatedListOfServers(host).get(0);
        requestContext.setUri(URI.create("http://" + newHost));
    }

    private List<String> getUpdatedListOfServers(String service) {
        List<String> result = new ArrayList<>();
        List<ServiceHealth> nodes = consul.healthClient().getHealthyServiceInstances(service).getResponse();
        for (ServiceHealth node : nodes) {
            result.add(node.getService().getAddress() + ":" + node.getService().getPort());
        }
        return result;
    }

}
