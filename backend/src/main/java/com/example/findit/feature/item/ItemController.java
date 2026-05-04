package com.example.findit.feature.item;

import com.example.findit.feature.item.dto.ItemRequest;
import com.example.findit.feature.item.dto.ItemResponse;
import com.example.findit.feature.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "http://localhost:5173")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    // POST /api/items — report a lost or found item (authenticated)
    @PostMapping
    public ResponseEntity<?> reportItem(
            @RequestBody ItemRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            ItemResponse response = itemService.reportItem(request, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET /api/items — public item feed
    @GetMapping
    public ResponseEntity<List<ItemResponse>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
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
}