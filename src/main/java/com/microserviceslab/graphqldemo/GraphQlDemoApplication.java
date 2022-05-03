package com.microserviceslab.graphqldemo;

import com.microserviceslab.graphqldemo.service.BookService;
import graphql.GraphQL;
import graphql.language.TypeDefinition;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

import javax.xml.validation.Schema;
import java.io.IOException;

@SpringBootApplication
public class GraphQlDemoApplication {

    @Autowired
    BookService bookService;

    public static void main(String[] args) {
        SpringApplication.run(GraphQlDemoApplication.class, args);
    }

    @Bean
    public ConnectionFactoryInitializer connectionFactoryInitializer(ConnectionFactory factory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(factory);
        ResourceDatabasePopulator populator =
                new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
        initializer.setDatabasePopulator(populator);
        return initializer;
    }

    @Bean
    public GraphQL graphQL() throws IOException {
        SchemaParser schemaParser = new SchemaParser();
        ClassPathResource schema = new ClassPathResource("schema.graphql");
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema.getInputStream());
        //Mapear las querys a data fetchers

        RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
                .type(TypeRuntimeWiring.newTypeWiring("Query").dataFetcher("getBook",bookService.getBook()))
                .type(TypeRuntimeWiring.newTypeWiring("Query").dataFetcher("getBooks", bookService.getBooks()))
                .type(TypeRuntimeWiring.newTypeWiring("Mutation").dataFetcher("createBook", bookService.createBook()))
                .build();

        SchemaGenerator generator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = generator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
        return GraphQL.newGraphQL(graphQLSchema).build();
    }
    /*
    El dataFetcher Mapea el nombre de la query con su service correspondiente
    El run time wiring crea las rutas (QueryName,Service) añadiendo a si mismo varios dataFetchers
    * */

}
