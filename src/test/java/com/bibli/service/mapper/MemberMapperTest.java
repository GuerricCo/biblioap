package com.bibli.service.mapper;

import static com.bibli.domain.MemberAsserts.*;
import static com.bibli.domain.MemberTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MemberMapperTest {

    private MemberMapper memberMapper;

    @BeforeEach
    void setUp() {
        memberMapper = new MemberMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getMemberSample1();
        var actual = memberMapper.toEntity(memberMapper.toDto(expected));
        assertMemberAllPropertiesEquals(expected, actual);
    }
}
