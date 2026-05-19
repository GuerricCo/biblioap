package com.bibli.service.mapper;

import com.bibli.domain.Book;
import com.bibli.domain.Library;
import com.bibli.domain.Loan;
import com.bibli.domain.Member;
import com.bibli.service.dto.BookDTO;
import com.bibli.service.dto.LibraryDTO;
import com.bibli.service.dto.LoanDTO;
import com.bibli.service.dto.MemberDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Loan} and its DTO {@link LoanDTO}.
 */
@Mapper(componentModel = "spring")
public interface LoanMapper extends EntityMapper<LoanDTO, Loan> {
    @Mapping(target = "library", source = "library", qualifiedByName = "libraryName")
    @Mapping(target = "book", source = "book", qualifiedByName = "bookTitle")
    @Mapping(target = "member", source = "member", qualifiedByName = "memberEmail")
    LoanDTO toDto(Loan s);

    @Named("libraryName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    LibraryDTO toDtoLibraryName(Library library);

    @Named("bookTitle")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    BookDTO toDtoBookTitle(Book book);

    @Named("memberEmail")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    MemberDTO toDtoMemberEmail(Member member);
}
