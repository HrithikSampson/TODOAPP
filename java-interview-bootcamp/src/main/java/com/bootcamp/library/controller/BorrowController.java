package com.bootcamp.library.controller;

import com.bootcamp.library.dto.BorrowRequest;
import com.bootcamp.library.dto.BorrowResponse;
import com.bootcamp.library.service.BorrowService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/borrows")
public class BorrowController {

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @GetMapping("/member/{memberId}")
    public List<BorrowResponse> activeLoans(@PathVariable Long memberId) {
        return borrowService.activeLoansForMember(memberId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BorrowResponse borrowBook(@Valid @RequestBody BorrowRequest request) {
        return borrowService.borrowBook(request);
    }

    @PostMapping("/{id}/return")
    public BorrowResponse returnBook(@PathVariable Long id) {
        return borrowService.returnBook(id);
    }
}
