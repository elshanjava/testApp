package packageApp;

import packageApp.batchConfig.Directory;
import packageApp.service.StartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(Directory.class)
public class Application implements CommandLineRunner {

    @Autowired
    private StartService jobService;

    public static void main(String[] args){
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args){
        jobService.startImport();
    }
}
