package packageApp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

@Service
public class StartService {
    private static final Logger logger = LoggerFactory.getLogger(StartService.class);

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    @Value("${package.input}")
    private String inputFiles;

    @Autowired
    private FinalService finalService;

    public StartService(JobLauncher jobLauncher, JobRegistry jobRegistry) {
        this.jobLauncher = jobLauncher;
        this.jobRegistry = jobRegistry;
    }


    public void startImport() {
        try {
            Stream<Path> files = finalService.listFiles(inputFiles);
            files.forEach(file -> {
                try {
                    logger.info("Start import: " + file.getFileName());

                    JobParameters jobParameters = new JobParametersBuilder()
                            .addString("resource", file.toFile().getCanonicalPath())
                            .toJobParameters();


                    Job job = jobRegistry.getJob("importInfoJob");
                    jobLauncher.run(job, jobParameters);
                } catch (Exception e) {
                    logger.info("Error: " + e.getMessage());
                }
            });
        } catch (IOException e) {
            logger.info("Error: " + e.getMessage());
        }
    }
}
