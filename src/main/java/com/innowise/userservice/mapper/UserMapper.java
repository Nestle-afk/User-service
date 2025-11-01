package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.UserRequest;
import com.innowise.userservice.dto.UserResponse;
import com.innowise.userservice.model.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {CardMapper.class})
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cards", ignore = true)
    User toEntity(UserRequest userRequest);

    @Mapping(target = "cards", source = "cards")
    UserResponse toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cards", ignore = true)
    void updateUserFromRequest(UserRequest userRequest, @MappingTarget User user);

    @Mapping(target = "cards", ignore = true)
    UserResponse toDtoWithoutCards(User user);
}
