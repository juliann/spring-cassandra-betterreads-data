package com.nadarzy.springcassandrabetterreadsdata;

import com.nadarzy.springcassandrabetterreadsdata.author.Author;
import com.nadarzy.springcassandrabetterreadsdata.author.AuthorRepository;
import com.nadarzy.springcassandrabetterreadsdata.book.Book;
import com.nadarzy.springcassandrabetterreadsdata.book.BookRepository;
import com.nadarzy.springcassandrabetterreadsdata.connection.DataStaxAstraProperties;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@SpringBootApplication
@EnableConfigurationProperties(DataStaxAstraProperties.class)
public class SpringCassandraBetterreadsDataApplication {

  @Value("${datadump.location.author}")
  private String authorDumpLocation;

  @Value("${datadump.location.works}")
  private String worksDumpLocation;

  @Autowired AuthorRepository authorRepository;
  @Autowired BookRepository bookRepository;

  public static void main(String[] args) {
    SpringApplication.run(SpringCassandraBetterreadsDataApplication.class, args);
  }

  @PostConstruct
  public void start() {
    initAuthors();
    initWorks();
    System.out.println("all done");
  }

  private void initWorks() {
    Path path = Paths.get(worksDumpLocation);
    DateTimeFormatter dateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
    try (Stream<String> lines = Files.lines(path); ) {
      lines.forEach(
          line -> {
            String jsonString = line.substring(line.indexOf("{"));
            JSONObject jsonObject = null;
            try {
              jsonObject = new JSONObject(jsonString);
              Book book = new Book();
              book.setId(jsonObject.optString("key").replace("/works/", ""));
              book.setName(jsonObject.optString("title"));
              JSONObject description = jsonObject.optJSONObject("description");
              if (description != null) {
                book.setDescription(description.getString("value"));
              }
              JSONObject created = jsonObject.optJSONObject("created");
              if (created != null) {
                book.setPublishedDate(
                    LocalDate.parse(created.getString("value"), dateTimeFormatter));
              }

              JSONArray covers = jsonObject.optJSONArray("covers");

              if (covers != null) {
                List<String> coverIds = new ArrayList<>();
                for (int i = 0; i < covers.length(); i++) {
                  coverIds.add(covers.getString(i));
                }
                book.setCoverIds(coverIds);
              }

              JSONArray authors = jsonObject.optJSONArray("authors");
              if (authors != null) {
                List<String> authorIds = new ArrayList<>();
                for (int i = 0; i < authors.length(); i++) {
                  authorIds.add(
                      authors
                          .getJSONObject(i)
                          .getJSONObject("author")
                          .getString("key")
                          .replace("/authors/", ""));
                }
                book.setAuthorIds(authorIds);
                List<String> authorNames =
                    authorIds.stream()
                        .map(id -> authorRepository.findById(id))
                        .map(
                            opt -> {
                              if (!opt.isPresent()) {
                                return "unknown author";
                              }
                              return opt.get().getName();
                            })
                        .toList();
                book.setAuthorNames(authorNames);
              }
              System.out.println("saving " + book.getName());
              bookRepository.save(book);
            } catch (Exception j) {
              j.printStackTrace();
            }
          });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void initAuthors() {
    Path path = Paths.get(authorDumpLocation);
    try (Stream<String> lines = Files.lines(path); ) {
      lines.forEach(
          line -> {
            // read and parse the line
            String jsonString = line.substring(line.indexOf("{"));
            JSONObject jsonObject = null;
            try {
              jsonObject = new JSONObject(jsonString);

              // create author object
              Author author = new Author();
              author.setName(jsonObject.optString("name"));
              author.setPersonalName(jsonObject.optString("personal_name"));
              author.setId(jsonObject.optString("key").replace("/authors/", ""));

              // persist to cassandra
              authorRepository.save(author);
            } catch (JSONException e) {
              e.printStackTrace();
            }
          });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Bean
  public CqlSessionBuilderCustomizer sessionBuilderCustomizer(
      DataStaxAstraProperties astraProperties) {
    Path bundle = astraProperties.getSecureConnectBundle().toPath();
    return builder -> builder.withCloudSecureConnectBundle(bundle);
  }
}
