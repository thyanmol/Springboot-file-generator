package com.example.service.impl;

import com.example.repository.userProfileRepository;
import com.example.service.dto.userProfileDTO;
import com.example.service.userProfileService;
import com.example.mapper.userProfileMapper;
import com.example.domain.userProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
@Service
@Transactional
public class userProfileServiceImpl implements userProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(userProfileServiceImpl.class);
    private final userProfileRepository userProfileRepository;
    private final userProfileMapper userProfileMapper;

    public userProfileServiceImpl(userProfileRepository userProfileRepository, userProfileMapper userProfileMapper) {
        this.userProfileRepository = userProfileRepository;
        this.userProfileMapper = userProfileMapper;
    }

    @Override
    public userProfileDTO save(userProfileDTO userProfileDTO) {
        LOG.debug("Request to save userProfile : {}",userProfileDTO);
        userProfile userProfile = userProfileMapper.toEntity(userProfileDTO);
        userProfile = userProfileRepository.save(userProfile);
        return userProfileMapper.toDto(userProfile);
    }

    @Override
    public userProfileDTO update(userProfileDTO userProfileDTO) {
        LOG.debug("Request to update userProfile : {}",userProfileDTO);
        userProfile userProfile = userProfileMapper.toEntity(userProfileDTO);
        userProfile = userProfileRepository.save(userProfile);
        return userProfileMapper.toDto(userProfile);
    }

    @Override
    public Optional<userProfileDTO> partialUpdate(userProfileDTO userProfiledto) {
        LOG.debug("Request to partially update userProfile : {}",userProfiledto);
        return userProfileRepository
            .findById(userProfiledto.getId())
            .map(existinguserProfile -> {
                userProfileMapper.partialUpdate(existinguserProfile, userProfiledto);
                return existinguserProfile;
            })
            .map(userProfileRepository::save)
            .map(userProfileMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<userProfileDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all userProfiles");
        return userProfileRepository.findAll(pageable).map(userProfileMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<userProfileDTO> findOne(Long id) {
        LOG.debug("Request to get userProfile : {}", id);
        return userProfileRepository.findById(id).map(userProfileMapper::toDto);
    }
    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete userProfile : {}", id);
        userProfileRepository.deleteById(id);
    }
}
