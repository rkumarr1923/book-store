package com.bookstore.mapper;

import com.bookstore.domain.Publisher;
import com.bookstore.dto.response.PublisherResponse;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for {@link Publisher} entity → {@link PublisherResponse}.
 * Publisher is a read-only reference entity with no inbound create/update API in Phase 1.
 */
@Mapper(componentModel = "spring")
public interface PublisherMapper {

    PublisherResponse toResponse(Publisher publisher);
}
