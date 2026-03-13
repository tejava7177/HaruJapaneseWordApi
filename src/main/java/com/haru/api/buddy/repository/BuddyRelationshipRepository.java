package com.haru.api.buddy.repository;

import com.haru.api.buddy.domain.BuddyRelationship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuddyRelationshipRepository extends JpaRepository<BuddyRelationship, Long> {
}
