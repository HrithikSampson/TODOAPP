package com.bootcamp.library.dto;

import com.bootcamp.library.model.Book;
import com.bootcamp.library.model.BookStatus;

public record BookResponse(
        Long id,
        String title,
        String author,
        String isbn,
        BookStatus status
) {
    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getStatus()
        );
    }
}
