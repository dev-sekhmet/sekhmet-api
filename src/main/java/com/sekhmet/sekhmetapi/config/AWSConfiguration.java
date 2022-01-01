package com.sekhmet.sekhmetapi.config;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AWSConfiguration {

    @Bean
    public AmazonS3 amazonS3(@Value("${application.s3.region}") String region, @Value("${application.s3.endpoint}") String endpoint) {
        return AmazonS3ClientBuilder
            .standard()
            .withPathStyleAccessEnabled(true)
            // .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("test", "test"))) //(2)
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
            .build();
    }
}
