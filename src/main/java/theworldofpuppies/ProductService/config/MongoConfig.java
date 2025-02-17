package theworldofpuppies.ProductService.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

    @Value("${spring.data.mongodb.database}")
    private String database;
    @Value("${spring.data.mongodb.uri}")
    private String uri;

    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(mongoClient(), database);
    }

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(uri);
    }
}
