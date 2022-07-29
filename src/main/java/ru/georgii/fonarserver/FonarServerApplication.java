package ru.georgii.fonarserver;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import ru.georgii.fonarserver.server.FonarConfiguration;

@SpringBootApplication
public class FonarServerApplication {

    @Autowired
    FonarConfiguration fonarConfiguration;
    @Bean
    public SocketIOServer socketIOServer() {
        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(fonarConfiguration.busPort);
        return new SocketIOServer(config);
    }

    public static void main(String[] args) {
        SpringApplication.run(FonarServerApplication.class, args);
    }

}
