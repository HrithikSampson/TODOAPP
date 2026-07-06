package com.bootcamp.library.service;

import com.bootcamp.library.dto.BorrowRequest;
import com.bootcamp.library.dto.BorrowResponse;
import com.bootcamp.library.exception.BusinessRuleException;
import com.bootcamp.library.exception.ResourceNotFoundException;
import com.bootcamp.library.model.Book;
import com.bootcamp.library.model.BorrowRecord;
import com.bootcamp.library.model.Member;
import com.bootcamp.library.repository.BorrowRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BorrowService {

    private static final int LOAN_DAYS = 14;

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookService bookService;
    private final MemberService memberService;

    public BorrowService(
            BorrowRecordRepository borrowRecordRepository,
            BookService bookService,
            MemberService memberService
    ) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookService = bookService;
        this.memberService = memberService;
    }

    public List<BorrowResponse> activeLoansForMember(Long memberId) {
        memberService.findById(memberId);
        return borrowRecordRepository.findByMemberIdAndReturnedAtIsNull(memberId).stream()
                .map(BorrowResponse::from)
                .toList();
    }

    @Transactional
    public BorrowResponse borrowBook(BorrowRequest request) {
        Book book = bookService.getBookEntity(request.bookId());
        Member member = memberService.getMemberEntity(request.memberId());

        if (borrowRecordRepository.existsByBookIdAndReturnedAtIsNull(book.getId())) {
            throw new BusinessRuleException("Book already on loan: " + book.getId());
        }

        bookService.markBorrowed(book);

        LocalDate today = LocalDate.now();
        BorrowRecord record = new BorrowRecord(book, member, today, today.plusDays(LOAN_DAYS));
        return BorrowResponse.from(borrowRecordRepository.save(record));
    }

    @Transactional
    public BorrowResponse returnBook(Long borrowId) {
        BorrowRecord record = borrowRecordRepository.findById(borrowId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow record not found: " + borrowId));

        if (!record.isActive()) {
            throw new BusinessRuleException("Book already returned for record: " + borrowId);
        }

        record.markReturned(LocalDate.now());
        bookService.markReturned(record.getBook());
        return BorrowResponse.from(record);
    }
}
