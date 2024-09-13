package com.example.mapper;

import com.example.domain.userProfile;
import com.example.service.dto.userProfileDTO;
import com.example.service.mapper.EntityMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface userProfileMapper extends EntityMapper<userProfileDTO, userProfile> {}