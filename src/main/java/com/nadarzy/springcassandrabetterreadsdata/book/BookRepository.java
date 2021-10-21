package com.nadarzy.springcassandrabetterreadsdata.book;

import org.springframework.data.cassandra.repository.CassandraRepository;

/** @author Julian Nadarzy on 21/10/2021 */
public interface BookRepository extends CassandraRepository<Book, String> {}
