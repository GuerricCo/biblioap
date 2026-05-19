package com.bibli.service.mapper;

import com.bibli.domain.Book;
import com.bibli.domain.Library;
import com.bibli.domain.Member;
import com.bibli.domain.Review;
import com.bibli.service.dto.BookDTO;
import com.bibli.service.dto.LibraryDTO;
import com.bibli.service.dto.MemberDTO;
import com.bibli.service.dto.ReviewDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Review} and its DTO {@link ReviewDTO}.
 */
@Mapper(componentModel = "spring")
public interface ReviewMapper extends EntityMapper<ReviewDTO, Review> {
    @Mapping(target = "library", source = "library", qualifiedByName = "libraryName")
    @Mapping(target = "book", source = "book", qualifiedByName = "bookTitle")
    @Mapping(target = "member", source = "member", qualifiedByName = "memberEmail")
    ReviewDTO toDto(Review s);

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
