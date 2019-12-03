package packageApp.batchConfig;

import packageApp.Information.Information;
import packageApp.Information.InformationItemProcessor;
import packageApp.service.FinalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Paths;

import static org.springframework.batch.core.BatchStatus.COMPLETED;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(BatchConfiguration.class);

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private FinalService finalService;

    private Resource resource;


    @Bean
    @StepScope
    public FlatFileItemReader<Information> reader(@Value("#{jobParameters['resource']}") String fileName) {
        FlatFileItemReader<Information> reader = new FlatFileItemReader<>();
        reader.setLinesToSkip(1);
        resource = new FileSystemResource(fileName);
        reader.setResource(resource);
        reader.setLineMapper(new DefaultLineMapper<Information>() {{
                setLineTokenizer(new DelimitedLineTokenizer() {{
                        setNames(new String[] {"id", "name", "value"});
                    }});
                setFieldSetMapper(new BeanWrapperFieldSetMapper<Information>() {{
                        setTargetType(Information.class);
                    }});
            }});
        return reader;
    }

    @Bean
    public ItemProcessor<Information, Information> processor() {
        return new InformationItemProcessor();
    }

    @Bean
    public ItemWriter<Information> writer() {
        JdbcBatchItemWriter<Information> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql("INSERT INTO information (id, name, value) VALUES (:id, :name, :value)");
        writer.setDataSource(dataSource);
        return writer;
    }

    @Bean
    public Job importInfoJob(ImportAndMoveFile start, Step s1) {
        return jobBuilderFactory.get("importInfoJob")
                .incrementer(new RunIdIncrementer())
                .listener(start)
                .flow(s1)
                .end()
                .build();
    }

    @Bean
    public Step step1(StepBuilderFactory stepBuilderFactory, ItemReader<Information> reader,
                      ItemWriter<Information> writer, ItemProcessor<Information, Information> processor) {
        return stepBuilderFactory.get("step1")
                .<Information, Information>chunk(10)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
        JobRegistryBeanPostProcessor postProcessor = new JobRegistryBeanPostProcessor();
        postProcessor.setJobRegistry(jobRegistry);
        return postProcessor;
    }

    @Component
    class ImportAndMoveFile extends JobExecutionListenerSupport {

        @Override
        public void afterJob(JobExecution jobExecution) {
            try {
                finalService.replaceFile(Paths.get(resource.getURI()));
                logger.info("Moving file to finished catalog");
            } catch (IOException e) {
                logger.warn("Error moving file: " + e.getMessage());
            }
        }
    }
}
