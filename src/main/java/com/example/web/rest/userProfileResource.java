package com.example.web.rest;

import com.example.service.dto.userProfileDTO;
import com.example.service.userProfileService;
import com.example.repository.userProfileRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import tech.jhipster.web.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.util.List;

@RestController
@RequestMapping("/api/userProfile")
public class userProfileResource {

    private static final Logger LOG = LoggerFactory.getLogger(userProfileResource.class);
    private static final String ENTITY_NAME = "userProfile";

  @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final userProfileService userProfileService;
    private final userProfileRepository userProfileRepository;
    public userProfileResource(userProfileRepository userProfileRepository, userProfileService userProfileService) {
        this.userProfileRepository = userProfileRepository;
        this.userProfileService = userProfileService;
    }

   /**
     * {@code POST  /userProfile} : Create a new userProfile.
     *
     * @param userProfileDTO the userProfileDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new userProfileDTO, or with status {@code 400 (Bad Request)} if the userProfile has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */

    @PostMapping("")
    public ResponseEntity<userProfileDTO> create(@RequestBody userProfileDTO userProfileDTO) throws URISyntaxException {
        LOG.debug("REST request to save userProfile : {}", userProfileDTO);
        if (userProfileDTO.getId() != null) {
            throw new BadRequestAlertException("A new userProfile cannot already have an ID", ENTITY_NAME, "idexists");
        }
        userProfileDTO = userProfileService.save(userProfileDTO);
        return ResponseEntity.created(new URI("/api/userProfiles/" + userProfileDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, userProfileDTO.getId().toString()))
            .body(userProfileDTO);
    }

   /**
     * {@code PUT  /userProfile/:id} : Updates an existing userProfile.
     *
     * @param id the id of the userProfileDTO to save.
     * @param userProfileDTO the userProfileDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated userProfileDTO,
     * or with status {@code 400 (Bad Request)} if the userProfileDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the userProfileDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<userProfileDTO> updateuserProfile(@PathVariable(value = "id", required = false) final Long id, @RequestBody userProfileDTO userProfileDTO) {

        LOG.debug("REST request to update userProfile : {}, {}", id, userProfileDTO);

        if (userProfileDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, userProfileDTO.getId())) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idinvalid");
        }
        if (!userProfileRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity Not Found", ENTITY_NAME, "idnotfound");
        }

        userProfileDTO result = userProfileService.update(userProfileDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, userProfileDTO.getId().toString()))
            .body(result);
    }

   /**
     * {@code PATCH  /userProfile/:id} : Partial updates given userProfile of an existing userProfile, userProfile will ignore if it is null
     *
     * @param id the id of the userProfileDTO to save.
     * @param userProfileDTO the userProfileDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated userProfileDTO,
     * or with status {@code 400 (Bad Request)} if the userProfileDTO is not valid,
     * or with status {@code 404 (Not Found)} if the userProfileDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the userProfileDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */

    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<userProfileDTO> partialUpdateuserProfile(
    @PathVariable(value = "id", required = false) final Long id,
    @RequestBody userProfileDTO userProfileDTO)    throws URISyntaxException {

        LOG.debug("REST request to partially update userProfile : {}, {}", id, userProfileDTO);

        if (userProfileDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, userProfileDTO.getId())) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idinvalid");
        }
        if (!userProfileRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity Not Found", ENTITY_NAME, "idnotfound");
        }

        Optional<userProfileDTO> result = userProfileService.partialUpdate(userProfileDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, userProfileDTO.getId().toString())
        );
    }

/**
     * {@code GET  /userProfiles} : get all the userProfiles.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of userProfile in body.
     */
    @GetMapping("")
    public ResponseEntity<List<userProfileDTO>> getAlluserProfiles(Pageable pageable) {

        LOG.debug("REST request to get all userProfiles");

        Page<userProfileDTO> page = userProfileService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
   /**
     * {@code GET  /userProfile/:id} : get the "id" userProfile.
     *
     * @param id the id of the userProfileDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the userProfileDTO, or with status {@code 404 (Not Found)}.
     */

    @GetMapping("/{id}")
    public ResponseEntity<userProfileDTO> getuserProfile(@PathVariable("id") Long id) {

        LOG.debug("REST request to get userProfile : {}", id);

        Optional<userProfileDTO> userProfileDTO = userProfileService.findOne(id);

        return ResponseUtil.wrapOrNotFound(userProfileDTO);
    }

   /**
     * {@code DELETE  /" + lowerFirstChar(tableName) + "/:id} : delete the "id" " + lowerFirstChar(tableName) + ".
     *
     * @param id the id of the " + lowerFirstChar(tableName) + "DTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteuserProfile(@PathVariable("id") Long id) {

        LOG.debug("REST request to delete userProfile : {}", id);

        userProfileService.delete(id);

        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
