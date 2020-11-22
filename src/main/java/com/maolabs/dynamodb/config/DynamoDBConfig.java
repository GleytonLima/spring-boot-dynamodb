package com.maolabs.dynamodb.config;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Aqui usamos beans para injetar o Dynamodb de acordo com o profile definido
 * no arquivo application.properties
 */
@Configuration
@Service
public class DynamoDBConfig {

    @Value("${maolabs.dynamodb.service-endpoint}")
    private String SERVICE_ENDPOINT;
    @Value("${maolabs.dynamodb.signing-region}")
    private String SIGNING_REGION;

    /**
     * Para DynamoDB rodando localmente por meio da dependência do Maven.
     *
     * É necessário que o aws cli esteja
     * configurado com credenciais -AWS Access Key ID e AWS Secret Access Key (com quaisquer valores que sejam)
     *
     * @return Table
     */
    @Bean
    @Profile("dev-local")
    public DynamoDB getDynamodbMaven() {
        System.setProperty("sqlite4java.library.path", "native-libs");
        AmazonDynamoDB client = DynamoDBEmbedded.create().amazonDynamoDB();
        return new DynamoDB(client);
    }


    /**
     * Para DynamoDB rodando localmente por meio do executável ou docker.
     *
     * É necessário que o aws cli esteja  configurado com credenciais
     * AWS Access Key ID e AWS Secret Access Key (com quaisquer valores que sejam)
     *
     * @return Table
     */
    @Bean
    @Profile("dev-local-docker-ou-jar")
    public DynamoDB getDynamodbDockerOuExecutavel() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(new
                        AwsClientBuilder.EndpointConfiguration(SERVICE_ENDPOINT, SIGNING_REGION))
                .build();
        return new DynamoDB(client);
    }


    /**
     * Para DynamoDB pesquisar na nuvem (será necessário que a máquina tenha as
     * credenciais de acesso à AWS)
     *
     * @return Table
     */
    @Bean
    @Profile("dev-nuvem")
    public DynamoDB getDynamodbNuvem() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .build();
        return new DynamoDB(client);
    }
}
