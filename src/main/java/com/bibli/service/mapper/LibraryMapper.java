package com.bibli.service.mapper;

import com.bibli.domain.Library;
import com.bibli.service.dto.LibraryDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Library} and its DTO {@link LibraryDTO}.
 */
@Mapper(componentModel = "spring")
public interface LibraryMapper extends EntityMapper<LibraryDTO, Library> {}
