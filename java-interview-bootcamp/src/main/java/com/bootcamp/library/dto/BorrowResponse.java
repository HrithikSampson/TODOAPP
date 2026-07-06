package com.bootcamp.library.dto;

import com.bootcamp.library.model.BorrowRecord;

import java.time.LocalDate;

public record BorrowResponse(
        Long id,
        Long bookId,
        String bookTitle,
        Long memberId,
        String memberName,
        LocalDate borrowedAt,
        LocalDate dueDate,
        LocalDate returnedAt
) {
    public static BorrowResponse from(BorrowRecord record) {
        return new BorrowResponse(
                record.getId(),
                record.getBook().getId(),
                record.getBook().getTitle(),
                record.getMember().getId(),
                record.getMember().getName(),
                record.getBorrowedAt(),
                record.getDueDate(),
                record.getReturnedAt()
        );
    }
}
