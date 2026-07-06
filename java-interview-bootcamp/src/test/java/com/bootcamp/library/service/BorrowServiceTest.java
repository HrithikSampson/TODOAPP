package com.bootcamp.library.service;

import com.bootcamp.library.dto.BorrowRequest;
import com.bootcamp.library.exception.BusinessRuleException;
import com.bootcamp.library.model.Book;
import com.bootcamp.library.model.BorrowRecord;
import com.bootcamp.library.model.Member;
import com.bootcamp.library.repository.BorrowRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @Mock
    private BookService bookService;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private BorrowService borrowService;

    private Book book;
    private Member member;

    @BeforeEach
    void setUp() {
        book = new Book("Java Concurrency", "Goetz", "978-0321349606");
        member = new Member("Alice", "alice@example.com");
        org.springframework.test.util.ReflectionTestUtils.setField(book, "id", 1L);
        org.springframework.test.util.ReflectionTestUtils.setField(member, "id", 2L);
    }

    @Test
    @DisplayName("borrowBook fails when book already on loan")
    void borrowBook_alreadyOnLoan() {
        when(bookService.getBookEntity(1L)).thenReturn(book);
        when(memberService.getMemberEntity(2L)).thenReturn(member);
        when(borrowRecordRepository.existsByBookIdAndReturnedAtIsNull(anyLong())).thenReturn(true);

        assertThatThrownBy(() -> borrowService.borrowBook(new BorrowRequest(1L, 2L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already on loan");
    }

    @Test
    @DisplayName("returnBook fails when already returned")
    void returnBook_alreadyReturned() {
        BorrowRecord record = new BorrowRecord(book, member, LocalDate.now().minusDays(5), LocalDate.now().plusDays(9));
        record.markReturned(LocalDate.now());

        when(borrowRecordRepository.findById(10L)).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> borrowService.returnBook(10L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already returned");
    }

    @Test
    @DisplayName("borrowBook creates record when book is available")
    void borrowBook_success() {
        when(bookService.getBookEntity(1L)).thenReturn(book);
        when(memberService.getMemberEntity(2L)).thenReturn(member);
        when(borrowRecordRepository.existsByBookIdAndReturnedAtIsNull(anyLong())).thenReturn(false);
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        borrowService.borrowBook(new BorrowRequest(1L, 2L));

        verify(bookService).markBorrowed(book);
        verify(borrowRecordRepository).save(any(BorrowRecord.class));
    }
}
