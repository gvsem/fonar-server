package ru.georgii.fonarserver.server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FonarConfiguration {

    public static final String API_VERSION_PREFIX = "/v1.0";

    @JsonIgnore
    public static final String API_VERSION_PREFIX_ANT = API_VERSION_PREFIX + "/**";

    @Schema(example = "1.0-alpha.4", description = "Server version", required = true)
    public final String server_version = "1.0-alpha.4";

    @Schema(example = server, description = "Package name of server (any string)", required = true)
    public final String server = "fonar-message-router-" + server_version;

    @Schema(example = "Fonar Message Router", description = "Name of server software implementation", required = true)
    public final String server_software_name = "Fonar Message Router";

    @Schema(example = "FONAR", description = "Word FONAR indicates that Fonar API is used by this server", required = true)
    public static final String api_spec = "FONAR";

    @Schema(example = "1.0-alpha.0", description = "Version of FONAR API to check compatibility", required = true)
    public static final String api_version = "1.0-alpha.0";

    @Schema(example = "salt", description = "Salt string which users must create own registration keys with", required = true)
    @Value("${fonar-server.salt}")
    public String salt;

    @Schema(example = ":20777", description = "Full absolute url or :PORT expression to Fonar socket", required = true)
    @Value(":${server.port}")
    public String socketUrl;

    @Schema(example = "DemoServer", description = "Human-readable server namea", required = true)
    @Value("${fonar-server.name}")
    public String server_name;

    public String getApi_spec() {
        return api_spec;
    }

    public String getServer_version() {
        return server_version;
    }

    @Schema(example = "/v1.0", description = "Prefix on which Fonar REST API is located", required = true)
    public String getApiVersionPrefix() {
        return API_VERSION_PREFIX;
    }


}
