package com.sgkhmjaes.jdias.service;

import com.sgkhmjaes.jdias.domain.Participation;
import java.util.List;

/**
 * Service Interface for managing Participation.
 */
public interface ParticipationService {

    /**
     * Save a participation.
     *
     * @param participation the entity to save
     * @return the persisted entity
     */
    Participation save(Participation participation);

    /**
     *  Get all the participations.
     *
     *  @return the list of entities
     */
    List<Participation> findAll();

    /**
     *  Get the "id" participation.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    Participation findOne(Long id);

    /**
     *  Delete the "id" participation.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the participation corresponding to the query.
     *
     *  @param query the query of the search
     *  
     *  @return the list of entities
     */
    List<Participation> search(String query);
}
