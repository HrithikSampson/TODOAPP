package com.bootcamp.library.service;

import com.bootcamp.library.dto.MemberRequest;
import com.bootcamp.library.dto.MemberResponse;
import com.bootcamp.library.exception.BusinessRuleException;
import com.bootcamp.library.exception.ResourceNotFoundException;
import com.bootcamp.library.model.Member;
import com.bootcamp.library.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<MemberResponse> findAll() {
        return memberRepository.findAll().stream()
                .map(MemberResponse::from)
                .toList();
    }

    public MemberResponse findById(Long id) {
        return memberRepository.findById(id)
                .map(MemberResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + id));
    }

    @Transactional
    public MemberResponse create(MemberRequest request) {
        memberRepository.findByEmail(request.email()).ifPresent(member -> {
            throw new BusinessRuleException("Email already registered: " + request.email());
        });

        Member member = new Member(request.name(), request.email());
        return MemberResponse.from(memberRepository.save(member));
    }

    Member getMemberEntity(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + id));
    }
}
