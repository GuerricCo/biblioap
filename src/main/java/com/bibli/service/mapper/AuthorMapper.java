package com.bibli.service.mapper;

import com.bibli.domain.Author;
import com.bibli.domain.Book;
import com.bibli.domain.Library;
import com.bibli.service.dto.AuthorDTO;
import com.bibli.service.dto.BookDTO;
import com.bibli.service.dto.LibraryDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Author} and its DTO {@link AuthorDTO}.
 */
@Mapper(componentModel = "spring")
public interface AuthorMapper extends EntityMapper<AuthorDTO, Author> {
    @Mapping(target = "bookses", source = "bookses", qualifiedByName = "bookTitleSet")
    @Mapping(target = "library", source = "library", qualifiedByName = "libraryName")
    AuthorDTO toDto(Author s);

    @Mapping(target = "bookses", ignore = true)
    @Mapping(target = "removeBooks", ignore = true)
    Author toEntity(AuthorDTO authorDTO);

    @Named("bookTitle")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    BookDTO toDtoBookTitle(Book book);

    @Named("bookTitleSet")
    default Set<BookDTO> toDtoBookTitleSet(Set<Book> book) {
        return book.stream().map(this::toDtoBookTitle).collect(Collectors.toSet());
    }

    @Named("libraryName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    LibraryDTO toDtoLibraryName(Library library);
}
