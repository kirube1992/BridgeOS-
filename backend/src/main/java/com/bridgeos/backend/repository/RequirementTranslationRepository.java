package com.bridgeos.backend.repository;

import com.bridgeos.backend.entity.RequirementTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequirementTranslationRepository extends JpaRepository<RequirementTranslation, Long> {
    List<RequirementTranslation> findByCreatedByIdOrderByCreatedAtDesc(Long userId);
}
