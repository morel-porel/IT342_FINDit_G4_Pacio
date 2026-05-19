package com.example.findit.feature.claim.dto;

public class ClaimRequest {
    public Long itemId;
    public String proofDescription;
    // proofImageUrl is set after file upload; optional
    public String proofImageUrl;
}
