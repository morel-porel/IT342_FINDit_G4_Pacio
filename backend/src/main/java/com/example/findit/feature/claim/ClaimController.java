package com.example.findit.feature.claim;

import com.example.findit.feature.claim.dto.ClaimRequest;
import com.example.findit.feature.claim.dto.ClaimResponse;
import com.example.findit.feature.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
@CrossOrigin(origins = "http://localhost:5173")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    /**
     * POST /api/claims — authenticated USER submits a claim
     */
    @PostMapping(consumes = {"multipart/form-data", "application/json", "application/x-www-form-urlencoded"})
    public ResponseEntity<?> submitClaim(
            @RequestParam("itemId") Long itemId,
            @RequestParam("proofDescription") String proofDescription,
            @RequestParam(value = "proofImage", required = false) MultipartFile proofImage,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            ClaimRequest request = new ClaimRequest();
            request.itemId = itemId;
            request.proofDescription = proofDescription;

            ClaimResponse response = claimService.submitClaim(request, proofImage, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET /api/claims — USER: own claims; ADMIN: all claims
     */
    @GetMapping
    public ResponseEntity<List<ClaimResponse>> getClaims(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(claimService.getClaims(currentUser));
    }

    /**
     * GET /api/claims/{id} — USER: own only; ADMIN: any
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getClaimById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            return ResponseEntity.ok(claimService.getClaimById(id, currentUser));
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    /**
     * PUT /api/claims/{id}/approve — ADMIN only
     * AC-7: Approve claim → claim APPROVED, item RESOLVED, email sent
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveClaim(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            return ResponseEntity.ok(claimService.approveClaim(id, currentUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * PUT /api/claims/{id}/reject — ADMIN only
     * AC-8: Reject claim → claim REJECTED, item OPEN, email sent
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectClaim(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            return ResponseEntity.ok(claimService.rejectClaim(id, currentUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
