package com.bibli.service.mapper;

import com.bibli.domain.Author;
import com.bibli.domain.Book;
import com.bibli.domain.Category;
import com.bibli.domain.Library;
import com.bibli.service.dto.AuthorDTO;
import com.bibli.service.dto.BookDTO;
import com.bibli.service.dto.CategoryDTO;
import com.bibli.service.dto.LibraryDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Book} and its DTO {@link BookDTO}.
 */
@Mapper(componentModel = "spring")
public interface BookMapper extends EntityMapper<BookDTO, Book> {
    @Mapping(target = "library", source = "library", qualifiedByName = "libraryName")
    @Mapping(target = "category", source = "category", qualifiedByName = "categoryName")
    @Mapping(target = "authorses", source = "authorses", qualifiedByName = "authorFirstNameSet")
    BookDTO toDto(Book s);

    @Mapping(target = "removeAuthors", ignore = true)
    Book toEntity(BookDTO bookDTO);

    @Named("libraryName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    LibraryDTO toDtoLibraryName(Library library);

    @Named("categoryName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    CategoryDTO toDtoCategoryName(Category category);

    @Named("authorFirstName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "firstName", source = "firstName")
    AuthorDTO toDtoAuthorFirstName(Author author);

    @Named("authorFirstNameSet")
    default Set<AuthorDTO> toDtoAuthorFirstNameSet(Set<Author> author) {
        return author.stream().map(this::toDtoAuthorFirstName).collect(Collectors.toSet());
    }
}
