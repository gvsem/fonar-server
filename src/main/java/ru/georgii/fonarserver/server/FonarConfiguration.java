package ru.georgii.fonarserver.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FonarConfiguration {

    @Value("${server.port}")
    public Integer webPort;

    @Value("${fonar-server.bus.port}")
    public Integer busPort;

    @Value("${fonar-server.salt}")
    public String salt;

    @Value("${fonar-server.socket.url}")
    public String socketUrl;

    @Value("${fonar-server.name}")
    public String serverName;

}
