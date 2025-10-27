package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.CardRequest;
import com.innowise.userservice.dto.CardResponse;
import com.innowise.userservice.model.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CardMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "userId", qualifiedByName = "userIdToUser")
    Card toEntity(CardRequest cardRequest);

    @Mapping(target = "userId", source = "user.id")
    CardResponse toDto(Card cardInfo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "userId", qualifiedByName = "userIdToUser")
    void updateCardFromRequest(CardRequest cardRequest, @MappingTarget Card cardInfo);

    @Named("userIdToUser")
    default User userIdToUser(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = new User();
        user.setId(userId);
        return user;
    }
}
