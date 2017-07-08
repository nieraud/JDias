package com.sgkhmjaes.jdias.repository;

import com.sgkhmjaes.jdias.domain.Tag;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * Spring Data JPA repository for the Tag entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TagRepository extends JpaRepository<Tag,Long> {
    
    @Query("select distinct tag from Tag tag left join fetch tag.tagposts left join fetch tag.tagpeople")
    List<Tag> findAllWithEagerRelationships();

    @Query("select tag from Tag tag left join fetch tag.tagposts left join fetch tag.tagpeople where tag.id =:id")
    Tag findOneWithEagerRelationships(@Param("id") Long id);
	
	@Query("select distinct tag from Tag")
    List<Tag> findAllTags();

	@Query("select distinct tag from tag.tagposts where post.id =:id )")
    List<Tag> findAllByPost (@Param("id") Long id);
	
	@Query("delete tag from Tag where tag.id =:id )")
    List<Tag> deleteTag (@Param("id") Long id);
    
}
