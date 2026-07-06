package com.bootcamp.library.repository;

import com.bootcamp.library.model.Book;
import com.bootcamp.library.model.BookStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    List<Book> findByAuthorContainingIgnoreCase(String author);

    List<Book> findByStatus(BookStatus status);
}
