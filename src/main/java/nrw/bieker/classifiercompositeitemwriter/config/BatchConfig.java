package nrw.bieker.classifiercompositeitemwriter.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nrw.bieker.classifiercompositeitemwriter.model.InputItem;
import nrw.bieker.classifiercompositeitemwriter.model.Item;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.item.support.builder.ClassifierCompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.classify.Classifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.sql.DataSource;
import java.util.Locale;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class BatchConfig {

    private final StepBuilderFactory stepBuilderFactory;
    private final JobBuilderFactory jobBuilderFactory;

    @Bean
    public BatchConfigurer batchConfigurer(){
        return new DefaultBatchConfigurer(dataSource());
    }

    private DataSource dataSource() {
        return DataSourceBuilder
                .create().url("jdbc:h2:tcp://localhost/~/src/classifier-composite-itemwriter/test")
                .driverClassName("org.h2.Driver")
                .username("sa")
                .password("test")
                .type(HikariDataSource.class).build();
    }

    private JdbcCursorItemReader<InputItem> reader() {
        return new JdbcCursorItemReaderBuilder<InputItem>()
                .name("classifier-jdbc-cursor-item-reader")
                .dataSource(dataSource())
                .sql("Select id, name from person")
                .beanRowMapper(InputItem.class)
                .build();
    }

    private ItemProcessor<InputItem, Item> processor(){
        return inputItem -> {
            Item item = new Item();
            item.setId(inputItem.getId());
            item.setName(inputItem.getName().toUpperCase(Locale.ROOT));
            return item;
        };
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<Item> evenflatFileItemWriter(@Value("#{jobParameters['outputPath']}") String outputPath) {
        return new FlatFileItemWriterBuilder<Item>()
                .name("test-classifier-builder")
                .resource(new FileSystemResource( outputPath.concat("even.csv")))
                .delimited()
                .delimiter(";")
                .names(FIELDS)
                .headerCallback(new StringHeaderWriter(toStringFieldNames(FIELDS)))
                .encoding("windows-1250")
                .shouldDeleteIfExists(true)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<Item> oddflatFileItemWriter(@Value("#{jobParameters['outputPath']}") String outputPath) {
        return new FlatFileItemWriterBuilder<Item>()
                .name("test-classifier-builder")
                .resource(new FileSystemResource(outputPath.concat("odd.csv")))
                .delimited()
                .delimiter(";")
                .names(FIELDS)
                .headerCallback(new StringHeaderWriter(toStringFieldNames(FIELDS)))
                .encoding("windows-1250")
                .shouldDeleteIfExists(true)
                .build();
    }

    private ClassifierCompositeItemWriter<Item> classifierCompositeItemWriter() {
        @SuppressWarnings("ConstantConditions")
        Classifier<Item, ItemWriter<? super Item>> personClassifier =
                new InputItemClassifier(evenflatFileItemWriter(null), oddflatFileItemWriter(null));
        return new ClassifierCompositeItemWriterBuilder<Item>()
                .classifier(personClassifier)
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Bean
    public Step step()  {
        return stepBuilderFactory.get("classifier-composite-demo-step2")
                .<InputItem,Item>chunk(1)
                .reader(reader())
                .processor(processor())
                .writer(classifierCompositeItemWriter())
                .stream(oddflatFileItemWriter(null))
                .stream(evenflatFileItemWriter(null))
                .build();
    }

    @Bean
    public Job job()  {
        return jobBuilderFactory.get("classifier-composite-demo-job2")
                .start(step())
                .build();
    }

    public String toStringFieldNames(String[] fields) {
        try {
            StringBuilder sb = new StringBuilder();
            for(String field : fields) {
                sb.append(field.concat(";"));
            }
            return sb.toString();
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private final String[] FIELDS = {"id", "name"};
}
