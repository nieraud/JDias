package com.sgkhmjaes.jdias.service;

import com.sgkhmjaes.jdias.domain.Aspect;
import com.sgkhmjaes.jdias.domain.Contact;

import java.util.Set;

/**
 * Service Interface for managing Contact.
 */
public interface ContactService {

    /**
     * Save a contact.
     *
     * @param contact the entity to save
     * @return the persisted entity
     */
    Contact save(Contact contact);

    /**
     *  Get all the contacts.
     *
     *  @return the list of entities
     */
    Set<Contact> findAll();

    /**
     *  Get the "id" contact.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    Contact findOne(Long id);

    /**
     *  Get all the contacts by aspect.
     *
     *  @return the list of entities
     */
    Set<Contact> findAllContactsByAspect(Aspect aspect);

    /**
     *  Delete the "id" contact.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Search for the contact corresponding to the query.
     *
     *  @param query the query of the search
     *
     *  @return the list of entities
     */
    Set<Contact> search(String query);
}
