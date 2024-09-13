package com.example.service;

import com.example.service.dto.userProfileDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface userProfileService {
    userProfileDTO save(userProfileDTO userProfiledto);
    Optional<userProfileDTO> findOne(Long id);
    Page<userProfileDTO> findAll(Pageable pageable);
    userProfileDTO update(userProfileDTO userProfiledto);
    Optional<userProfileDTO> partialUpdate(userProfileDTO userProfiledto);
    void delete(Long id);
}
