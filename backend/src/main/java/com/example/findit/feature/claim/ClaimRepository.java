package com.example.findit.feature.claim;

import com.example.findit.feature.claim.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByClaimant_IdOrderByCreatedAtDesc(Long claimantId);
    List<Claim> findAllByOrderByCreatedAtDesc();
    Optional<Claim> findByItem_IdAndStatus(Long itemId, String status);
    boolean existsByItem_IdAndStatus(Long itemId, String status);
}
