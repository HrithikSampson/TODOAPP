package com.bootcamp.library.service;

import com.bootcamp.library.dto.BookRequest;
import com.bootcamp.library.dto.BookResponse;
import com.bootcamp.library.exception.BusinessRuleException;
import com.bootcamp.library.exception.ResourceNotFoundException;
import com.bootcamp.library.model.Book;
import com.bootcamp.library.model.BookStatus;
import com.bootcamp.library.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<BookResponse> findAll() {
        return bookRepository.findAll().stream()
                .map(BookResponse::from)
                .toList();
    }

    public BookResponse findById(Long id) {
        return bookRepository.findById(id)
                .map(BookResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));
    }

    public List<BookResponse> findAvailableBooks() {
        return bookRepository.findByStatus(BookStatus.AVAILABLE).stream()
                .map(BookResponse::from)
                .toList();
    }

    public List<BookResponse> searchByAuthor(String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author).stream()
                .map(BookResponse::from)
                .toList();
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        bookRepository.findByIsbn(request.isbn()).ifPresent(book -> {
            throw new BusinessRuleException("ISBN already exists: " + request.isbn());
        });

        Book book = new Book(request.title(), request.author(), request.isbn());
        return BookResponse.from(bookRepository.save(book));
    }

    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));

        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setIsbn(request.isbn());
        return BookResponse.from(book);
    }

    @Transactional
    public void delete(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book not found: " + id);
        }
        bookRepository.deleteById(id);
    }

    Book getBookEntity(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));
    }

    @Transactional
    void markBorrowed(Book book) {
        if (!book.isAvailable()) {
            throw new BusinessRuleException("Book is not available: " + book.getId());
        }
        book.setStatus(BookStatus.BORROWED);
    }

    @Transactional
    void markReturned(Book book) {
        book.setStatus(BookStatus.AVAILABLE);
    }
}
