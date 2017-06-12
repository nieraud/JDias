package com.sgkhmjaes.jdias.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A Aspect.
 */
@Entity
@Table(name = "aspect")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "aspect")
public class Aspect implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Column(name = "contact_visible")
    private Boolean contactVisible;

    @Column(name = "chat_enabled")
    private Boolean chatEnabled;

    @Column(name = "post_default")
    private Boolean postDefault;

    @OneToMany(mappedBy = "aspect")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<AspectMembership> aspectMemberships = new HashSet<>();

    @OneToMany(mappedBy = "aspect")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<AspectVisiblity> aspectVisibilities = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Aspect name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public Aspect createdAt(LocalDate createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public Aspect updatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean isContactVisible() {
        return contactVisible;
    }

    public Aspect contactVisible(Boolean contactVisible) {
        this.contactVisible = contactVisible;
        return this;
    }

    public void setContactVisible(Boolean contactVisible) {
        this.contactVisible = contactVisible;
    }

    public Boolean isChatEnabled() {
        return chatEnabled;
    }

    public Aspect chatEnabled(Boolean chatEnabled) {
        this.chatEnabled = chatEnabled;
        return this;
    }

    public void setChatEnabled(Boolean chatEnabled) {
        this.chatEnabled = chatEnabled;
    }

    public Boolean isPostDefault() {
        return postDefault;
    }

    public Aspect postDefault(Boolean postDefault) {
        this.postDefault = postDefault;
        return this;
    }

    public void setPostDefault(Boolean postDefault) {
        this.postDefault = postDefault;
    }

    public Set<AspectMembership> getAspectMemberships() {
        return aspectMemberships;
    }

    public Aspect aspectMemberships(Set<AspectMembership> aspectMemberships) {
        this.aspectMemberships = aspectMemberships;
        return this;
    }

    public Aspect addAspectMemberships(AspectMembership aspectMembership) {
        this.aspectMemberships.add(aspectMembership);
        aspectMembership.setAspect(this);
        return this;
    }

    public Aspect removeAspectMemberships(AspectMembership aspectMembership) {
        this.aspectMemberships.remove(aspectMembership);
        aspectMembership.setAspect(null);
        return this;
    }

    public void setAspectMemberships(Set<AspectMembership> aspectMemberships) {
        this.aspectMemberships = aspectMemberships;
    }

    public Set<AspectVisiblity> getAspectVisibilities() {
        return aspectVisibilities;
    }

    public Aspect aspectVisibilities(Set<AspectVisiblity> aspectVisiblities) {
        this.aspectVisibilities = aspectVisiblities;
        return this;
    }

    public Aspect addAspectVisibilities(AspectVisiblity aspectVisiblity) {
        this.aspectVisibilities.add(aspectVisiblity);
        aspectVisiblity.setAspect(this);
        return this;
    }

    public Aspect removeAspectVisibilities(AspectVisiblity aspectVisiblity) {
        this.aspectVisibilities.remove(aspectVisiblity);
        aspectVisiblity.setAspect(null);
        return this;
    }

    public void setAspectVisibilities(Set<AspectVisiblity> aspectVisiblities) {
        this.aspectVisibilities = aspectVisiblities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Aspect aspect = (Aspect) o;
        if (aspect.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), aspect.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Aspect{"
                + "id=" + getId()
                + ", name='" + getName() + "'"
                + ", createdAt='" + getCreatedAt() + "'"
                + ", updatedAt='" + getUpdatedAt() + "'"
                + ", contactVisible='" + isContactVisible() + "'"
                + ", chatEnabled='" + isChatEnabled() + "'"
                + ", postDefault='" + isPostDefault() + "'"
                + "}";
    }
}
