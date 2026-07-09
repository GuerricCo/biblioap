package com.bibli.service.mapper;

import com.bibli.domain.Category;
import com.bibli.domain.Library;
import com.bibli.service.dto.CategoryDTO;
import com.bibli.service.dto.LibraryDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Category} and its DTO {@link CategoryDTO}.
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper extends EntityMapper<CategoryDTO, Category> {
    @Mapping(target = "library", source = "library", qualifiedByName = "libraryName")
    CategoryDTO toDto(Category s);

    @Named("libraryName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    LibraryDTO toDtoLibraryName(Library library);
}
