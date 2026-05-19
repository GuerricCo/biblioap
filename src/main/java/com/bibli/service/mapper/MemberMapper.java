package com.bibli.service.mapper;

import com.bibli.domain.Library;
import com.bibli.domain.Member;
import com.bibli.service.dto.LibraryDTO;
import com.bibli.service.dto.MemberDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Member} and its DTO {@link MemberDTO}.
 */
@Mapper(componentModel = "spring")
public interface MemberMapper extends EntityMapper<MemberDTO, Member> {
    @Mapping(target = "library", source = "library", qualifiedByName = "libraryName")
    MemberDTO toDto(Member s);

    @Named("libraryName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    LibraryDTO toDtoLibraryName(Library library);
}
