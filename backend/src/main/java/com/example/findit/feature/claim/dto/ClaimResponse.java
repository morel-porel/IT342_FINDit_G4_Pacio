package com.example.findit.feature.claim.dto;

import com.example.findit.feature.claim.entity.Claim;
import java.time.LocalDateTime;

public class ClaimResponse {
    public Long id;
    public Long itemId;
    public String itemName;
    public String itemLocation;
    public Long claimantId;
    public String claimantName;
    public String claimantEmail;
    public String proofDescription;
    public String proofImageUrl;
    public String status;
    public LocalDateTime createdAt;
    public LocalDateTime reviewedAt;

    public static ClaimResponse from(Claim claim) {
        ClaimResponse r = new ClaimResponse();
        r.id = claim.getId();
        r.itemId = claim.getItem().getId();
        r.itemName = claim.getItem().getName();
        r.itemLocation = claim.getItem().getLocation();
        r.claimantId = claim.getClaimant().getId();
        r.claimantName = claim.getClaimant().getFullName();
        r.claimantEmail = claim.getClaimant().getEmail();
        r.proofDescription = claim.getProofDescription();
        r.proofImageUrl = claim.getProofImageUrl();
        r.status = claim.getStatus();
        r.createdAt = claim.getCreatedAt();
        r.reviewedAt = claim.getReviewedAt();
        return r;
    }
}
