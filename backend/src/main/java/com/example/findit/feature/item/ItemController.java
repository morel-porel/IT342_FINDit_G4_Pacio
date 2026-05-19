package com.example.findit.feature.item;

import com.example.findit.feature.item.dto.ItemRequest;
import com.example.findit.feature.item.dto.ItemResponse;
import com.example.findit.feature.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "http://localhost:5173")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    // POST /api/items — report a lost or found item (multipart, authenticated)
    @PostMapping(consumes = {"multipart/form-data", "application/json"})
    public ResponseEntity<?> reportItem(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "dateLostFound", required = false) String dateLostFound,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            ItemRequest request = new ItemRequest();
            request.type = type;
            request.name = name;
            request.category = category;
            request.description = description;
            request.location = location;
            if (dateLostFound != null && !dateLostFound.isBlank()) {
                request.dateLostFound = java.time.LocalDate.parse(dateLostFound);
            }

            ItemResponse response = itemService.reportItem(request, photo, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET /api/items — public item feed
    @GetMapping
    public ResponseEntity<List<ItemResponse>> getAllItems(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "category", required = false) String category
    ) {
        return ResponseEntity.ok(itemService.getAllItems(type, status, category));
    }

    // GET /api/items/{id} — single item detail
    @GetMapping("/{id}")
    public ResponseEntity<?> getItemById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(itemService.getItemById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    // GET /api/items/my — items reported by the logged-in user
    @GetMapping("/my")
    public ResponseEntity<List<ItemResponse>> getMyItems(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(itemService.getMyItems(currentUser.getId()));
    }

    // PUT /api/items/{id} — update item (owner or ADMIN)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(
            @PathVariable Long id,
            @RequestBody ItemRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            ItemResponse response = itemService.updateItem(id, request, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DELETE /api/items/{id} — delete item (owner or ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            itemService.deleteItem(id, currentUser);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PATCH /api/items/{id}/resolve — owner marks their lost item as RESOLVED
    @PatchMapping("/{id}/resolve")
    public ResponseEntity<?> resolveItem(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            ItemResponse response = itemService.resolveItem(id, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
