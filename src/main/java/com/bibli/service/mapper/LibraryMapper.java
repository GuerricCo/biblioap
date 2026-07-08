package com.bibli.service.mapper;

import com.bibli.domain.Library;
import com.bibli.domain.User;
import com.bibli.service.dto.LibraryDTO;
import com.bibli.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Library} and its DTO {@link LibraryDTO}.
 */
@Mapper(componentModel = "spring")
public interface LibraryMapper extends EntityMapper<LibraryDTO, Library> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    LibraryDTO toDto(Library s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
