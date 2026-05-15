package com.example.findit.feature.claim;

import com.example.findit.feature.claim.dto.ClaimRequest;
import com.example.findit.feature.claim.dto.ClaimResponse;
import com.example.findit.feature.claim.entity.Claim;
import com.example.findit.feature.item.ItemRepository;
import com.example.findit.feature.item.entity.Item;
import com.example.findit.feature.user.User;
import com.example.findit.shared.email.EmailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ItemRepository itemRepository;
    private final EmailService emailService;

    public ClaimService(ClaimRepository claimRepository,
                        ItemRepository itemRepository,
                        EmailService emailService) {
        this.claimRepository = claimRepository;
        this.itemRepository = itemRepository;
        this.emailService = emailService;
    }

    /**
     * POST /api/claims
     * AC-6: Only OPEN items can be claimed. Only one active (PENDING) claim per item.
     * Reporter cannot claim their own item.
     */
    @Transactional
    public ClaimResponse submitClaim(ClaimRequest request, User claimant) {
        Item item = itemRepository.findById(request.itemId)
                .orElseThrow(() -> new RuntimeException("CLAIM-001: Item not found"));

        // CLAIM-002: Item must be OPEN
        if (!"OPEN".equals(item.getStatus())) {
            throw new RuntimeException("CLAIM-002: Item is not claimable. Current status: " + item.getStatus());
        }

        // CLAIM-002: No existing PENDING claim
        if (claimRepository.existsByItem_IdAndStatus(item.getId(), "PENDING")) {
            throw new RuntimeException("CLAIM-002: A claim is already pending for this item");
        }

        // CLAIM-003: Reporter cannot claim their own item
        if (item.getReporter().getId().equals(claimant.getId())) {
            throw new RuntimeException("CLAIM-003: You cannot claim your own item");
        }

        if (request.proofDescription == null || request.proofDescription.isBlank()) {
            throw new RuntimeException("Proof description is required");
        }

        Claim claim = new Claim();
        claim.setItem(item);
        claim.setClaimant(claimant);
        claim.setProofDescription(request.proofDescription.trim());
        claim.setProofImageUrl(request.proofImageUrl);
        claim.setStatus("PENDING");

        // Set item to PENDING — locked from further new claims
        item.setStatus("PENDING");
        itemRepository.save(item);

        Claim saved = claimRepository.save(claim);
        return ClaimResponse.from(saved);
    }

    /**
     * GET /api/claims
     * USER: own claims only. ADMIN: all claims.
     */
    public List<ClaimResponse> getClaims(User requester) {
        List<Claim> claims;
        if ("ADMIN".equals(requester.getRole())) {
            claims = claimRepository.findAllByOrderByCreatedAtDesc();
        } else {
            claims = claimRepository.findByClaimant_IdOrderByCreatedAtDesc(requester.getId());
        }
        return claims.stream().map(ClaimResponse::from).collect(Collectors.toList());
    }

    /**
     * GET /api/claims/{id}
     * USER can only view their own; ADMIN sees all.
     */
    public ClaimResponse getClaimById(Long id, User requester) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CLAIM-001: Claim not found"));
        if (!"ADMIN".equals(requester.getRole()) && !claim.getClaimant().getId().equals(requester.getId())) {
            throw new RuntimeException("AUTH-003: Insufficient permissions");
        }
        return ClaimResponse.from(claim);
    }

    /**
     * PUT /api/claims/{id}/approve — ADMIN only
     * AC-7: claim → APPROVED, item → RESOLVED, send email
     */
    @Transactional
    public ClaimResponse approveClaim(Long id, User admin) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CLAIM-001: Claim not found"));

        claim.setStatus("APPROVED");
        claim.setReviewedBy(admin);
        claim.setReviewedAt(LocalDateTime.now());

        Item item = claim.getItem();
        item.setStatus("RESOLVED");
        itemRepository.save(item);

        Claim saved = claimRepository.save(claim);

        // AC-7: Send claim approved email to claimant
        emailService.sendClaimApprovedEmail(claim.getClaimant(), item);

        return ClaimResponse.from(saved);
    }

    /**
     * PUT /api/claims/{id}/reject — ADMIN only
     * AC-8: claim → REJECTED, item → OPEN, send email
     */
    @Transactional
    public ClaimResponse rejectClaim(Long id, User admin) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CLAIM-001: Claim not found"));

        claim.setStatus("REJECTED");
        claim.setReviewedBy(admin);
        claim.setReviewedAt(LocalDateTime.now());

        Item item = claim.getItem();
        item.setStatus("OPEN");
        itemRepository.save(item);

        Claim saved = claimRepository.save(claim);

        // AC-8: Send claim rejected email to claimant
        emailService.sendClaimRejectedEmail(claim.getClaimant(), item);

        return ClaimResponse.from(saved);
    }
}