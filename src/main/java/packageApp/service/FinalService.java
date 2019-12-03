package packageApp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

@Component
public class FinalService {
    private static final Logger logger = LoggerFactory.getLogger(FinalService.class);

    @Value("${package.finished}")
    private String processedCatalog;

    public Stream<Path> listFiles(String packageFile) throws IOException {
        logger.info("Finished file: "+ packageFile);
        return Files.list(Paths.get(packageFile));
    }

    public void replaceFile(Path filePath) {

        try {
            logger.info("Replace file: " + filePath);
            Files.move(filePath, Paths.get(processedCatalog, filePath.getFileName().toString()));
        } catch (IOException e) {
            logger.warn("Error replace file:" + filePath + " " +e.getMessage());
        }
    }
}
