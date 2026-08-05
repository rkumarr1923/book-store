package com.bookstore.mapper;

import com.bookstore.domain.UserFollowedAuthor;
import com.bookstore.dto.response.FollowedAuthorResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for {@link UserFollowedAuthor} → {@link FollowedAuthorResponse}.
 *
 * <p>Field-name notes:
 * <ul>
 *   <li>All author fields ({@code id}, {@code name}, {@code biography},
 *       {@code profileImageUrl}) are sourced from the nested {@code author}
 *       association.</li>
 *   <li>{@code followedAt} maps directly from the entity's own field.</li>
 *   <li>{@code recentBooks} requires a separate repository query (up to 3
 *       most recent books). It is ignored here; the service sets it.</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface UserFollowedAuthorMapper {

    @Mapping(target = "id",              source = "author.id")
    @Mapping(target = "name",            source = "author.name")
    @Mapping(target = "biography",       source = "author.biography")
    @Mapping(target = "profileImageUrl", source = "author.profileImageUrl")
    @Mapping(target = "recentBooks",     ignore = true)
    FollowedAuthorResponse toResponse(UserFollowedAuthor userFollowedAuthor);
}
