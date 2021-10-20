package com.nadarzy.springcassandrabetterreadsdata.author;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

/** @author Julian Nadarzy on 20/10/2021 */
@Repository
public interface AuthorRepository extends CassandraRepository<Author, String> {}
