package com.bookstore.mapper;

import com.bookstore.domain.User;
import com.bookstore.dto.request.RegisterRequest;
import com.bookstore.dto.request.UpdateProfileRequest;
import com.bookstore.dto.response.UserProfileResponse;
import org.mapstruct.*;

/**
 * MapStruct mapper for {@link User} entity ↔ user-related DTOs.
 *
 * <p>Note: {@code passwordHash} is intentionally excluded from the
 * entity → response mapping. The service layer hashes the raw password
 * from {@link RegisterRequest} before persisting, so no mapping is provided
 * for that field here.
 *
 * <p>{@code unmappedTargetPolicy = IGNORE} suppresses warnings for
 * {@code id}, {@code createdAt}, and {@code updatedAt} on the builder-based
 * {@code toEntity} method — those fields are inherited from
 * {@link com.bookstore.domain.BaseEntity} and cannot be set via the Lombok
 * builder, nor do the request DTOs carry them.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    // ── Entity → Response ─────────────────────────────────────────────────────

    UserProfileResponse toProfileResponse(User user);

    // ── Request → Entity ──────────────────────────────────────────────────────

    /**
     * Create a new {@link User} from a registration request.
     * Persistence-managed fields ({@code id}, {@code createdAt}, {@code updatedAt})
     * and security fields ({@code passwordHash}, {@code role}, {@code active})
     * are ignored; the service layer sets them explicitly.
     *
     * <p>Note: Lombok generates the boolean accessor for {@code isActive} as {@code isActive()},
     * but the builder setter is named {@code isActive}. MapStruct resolves the target
     * property name from the setter, which Lombok generates as {@code active} for boolean
     * fields prefixed with {@code is}. We therefore use {@code "active"} as the target name.
     */
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role",         ignore = true)
    @Mapping(target = "isActive",     ignore = true)
    @Mapping(target = "addresses",    ignore = true)
    @Mapping(target = "cart",         ignore = true)
    @Mapping(target = "wishlist",     ignore = true)
    @Mapping(target = "reviews",      ignore = true)
    @Mapping(target = "orders",       ignore = true)
    User toEntity(RegisterRequest request);

    /**
     * Apply profile update fields to an existing {@link User} entity.
     * Only {@code firstName}, {@code lastName}, and {@code phoneNumber} are updated.
     */
    @Mapping(target = "email",        ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role",         ignore = true)
    @Mapping(target = "active",       ignore = true)
    @Mapping(target = "addresses",    ignore = true)
    @Mapping(target = "cart",         ignore = true)
    @Mapping(target = "wishlist",     ignore = true)
    @Mapping(target = "reviews",      ignore = true)
    @Mapping(target = "orders",       ignore = true)
    void updateEntity(UpdateProfileRequest request, @MappingTarget User user);
}
