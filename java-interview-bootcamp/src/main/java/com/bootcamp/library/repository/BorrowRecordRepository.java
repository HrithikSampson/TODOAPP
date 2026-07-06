package com.bootcamp.library.repository;

import com.bootcamp.library.model.BorrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    List<BorrowRecord> findByMemberIdAndReturnedAtIsNull(Long memberId);

    boolean existsByBookIdAndReturnedAtIsNull(Long bookId);
}
