package com.bootcamp.library.service;

import com.bootcamp.library.dto.BookRequest;
import com.bootcamp.library.exception.BusinessRuleException;
import com.bootcamp.library.exception.ResourceNotFoundException;
import com.bootcamp.library.model.Book;
import com.bootcamp.library.model.BookStatus;
import com.bootcamp.library.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book sampleBook;

    @BeforeEach
    void setUp() {
        sampleBook = new Book("Clean Code", "Robert Martin", "978-0132350884");
    }

    @Test
    @DisplayName("findById returns book when present")
    void findById_success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        var response = bookService.findById(1L);

        assertThat(response.title()).isEqualTo("Clean Code");
        assertThat(response.author()).isEqualTo("Robert Martin");
    }

    @Test
    @DisplayName("findById throws when book missing")
    void findById_notFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("create rejects duplicate ISBN")
    void create_duplicateIsbn() {
        when(bookRepository.findByIsbn("978-0132350884")).thenReturn(Optional.of(sampleBook));

        var request = new BookRequest("Duplicate", "Author", "978-0132350884");

        assertThatThrownBy(() -> bookService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ISBN already exists");
    }

    @Test
    @DisplayName("findAvailableBooks maps only available status")
    void findAvailableBooks() {
        Book borrowed = new Book("Borrowed", "Author", "111");
        borrowed.setStatus(BookStatus.BORROWED);

        when(bookRepository.findByStatus(BookStatus.AVAILABLE)).thenReturn(List.of(sampleBook));

        var books = bookService.findAvailableBooks();

        assertThat(books).hasSize(1);
        assertThat(books.getFirst().status()).isEqualTo(BookStatus.AVAILABLE);
    }

    @Test
    @DisplayName("markBorrowed updates status when available")
    void markBorrowed_success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        bookService.markBorrowed(bookService.getBookEntity(1L));

        assertThat(sampleBook.getStatus()).isEqualTo(BookStatus.BORROWED);
    }

    @Test
    @DisplayName("create saves new book")
    void create_success() {
        when(bookRepository.findByIsbn(any())).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var request = new BookRequest("Effective Java", "Joshua Bloch", "978-0134685991");
        var response = bookService.create(request);

        assertThat(response.title()).isEqualTo("Effective Java");
        verify(bookRepository).save(any(Book.class));
    }
}
