package packageApp.batchConfig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "packages")
public class Directory {
    private String input;
    private String finished;

}
