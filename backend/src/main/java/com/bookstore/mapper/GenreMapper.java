package com.bookstore.mapper;

import com.bookstore.domain.Genre;
import com.bookstore.dto.response.GenreResponse;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for {@link Genre} entity → {@link GenreResponse}.
 * Genre is a read-only reference entity with no inbound create/update API.
 */
@Mapper(componentModel = "spring")
public interface GenreMapper {

    GenreResponse toResponse(Genre genre);
}
