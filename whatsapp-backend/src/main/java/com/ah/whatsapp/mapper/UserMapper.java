package com.ah.whatsapp.mapper;

import com.ah.whatsapp.dto.UserSignupDto;
import com.ah.whatsapp.entity.UserEntity;
import com.ah.whatsapp.model.User;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public UserEntity toEntity(User model) {
    UserEntity entity = new UserEntity();
    BeanUtils.copyProperties(model, entity);
    return entity;
  }

  public User toModel(UserEntity entity) {
    User model = new User();
    BeanUtils.copyProperties(entity, model);
    return model;
  }

  public User toModel(UserSignupDto dto) {
    User model = new User();
    BeanUtils.copyProperties(dto, model);
    return model;
  }
}
