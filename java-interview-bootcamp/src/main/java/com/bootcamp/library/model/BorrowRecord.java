package com.bootcamp.library.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "borrow_records")
public class BorrowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDate borrowedAt;
    private LocalDate dueDate;
    private LocalDate returnedAt;

    protected BorrowRecord() {
    }

    public BorrowRecord(Book book, Member member, LocalDate borrowedAt, LocalDate dueDate) {
        this.book = book;
        this.member = member;
        this.borrowedAt = borrowedAt;
        this.dueDate = dueDate;
    }

    public Long getId() {
        return id;
    }

    public Book getBook() {
        return book;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getBorrowedAt() {
        return borrowedAt;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnedAt() {
        return returnedAt;
    }

    public void markReturned(LocalDate returnedAt) {
        this.returnedAt = returnedAt;
    }

    public boolean isActive() {
        return returnedAt == null;
    }
}
