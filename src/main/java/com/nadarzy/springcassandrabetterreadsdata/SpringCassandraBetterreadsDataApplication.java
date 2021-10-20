package com.nadarzy.springcassandrabetterreadsdata;

import com.nadarzy.springcassandrabetterreadsdata.author.Author;
import com.nadarzy.springcassandrabetterreadsdata.author.AuthorRepository;
import com.nadarzy.springcassandrabetterreadsdata.connection.DataStaxAstraProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import java.nio.file.Path;

@SpringBootApplication
@EnableConfigurationProperties(DataStaxAstraProperties.class)
public class SpringCassandraBetterreadsDataApplication {

  @Autowired AuthorRepository authorRepository;

  public static void main(String[] args) {
    SpringApplication.run(SpringCassandraBetterreadsDataApplication.class, args);
  }

  @PostConstruct
  public void start() {
    System.out.println("application started");
    Author author = new Author();
    author.setId("id");
    author.setName("name");
    author.setPersonalName("personal name");
    authorRepository.save(author);
  }

  @Bean
  public CqlSessionBuilderCustomizer sessionBuilderCustomizer(
      DataStaxAstraProperties astraProperties) {
    Path bundle = astraProperties.getSecureConnectBundle().toPath();
    return builder -> builder.withCloudSecureConnectBundle(bundle);
  }
}
