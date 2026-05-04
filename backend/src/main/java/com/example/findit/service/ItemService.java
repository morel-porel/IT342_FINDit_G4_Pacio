package com.example.findit.service;

import com.example.findit.dto.ItemRequest;
import com.example.findit.dto.ItemResponse;
import com.example.findit.entity.Item;
import com.example.findit.entity.User;
import com.example.findit.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public ItemResponse reportItem(ItemRequest request, User reporter) {
        if (request.type == null || (!request.type.equals("LOST") && !request.type.equals("FOUND"))) {
            throw new RuntimeException("Type must be LOST or FOUND");
        }
        if (request.name == null || request.name.isBlank()) {
            throw new RuntimeException("Item name is required");
        }
        if (request.category == null || request.category.isBlank()) {
            throw new RuntimeException("Category is required");
        }
        if (request.location == null || request.location.isBlank()) {
            throw new RuntimeException("Location is required");
        }
        if (request.dateLostFound == null) {
            throw new RuntimeException("Date is required");
        }

        Item item = new Item();
        item.setReporter(reporter);
        item.setType(request.type);
        item.setName(request.name.trim());
        item.setCategory(request.category.trim());
        item.setDescription(request.description != null ? request.description.trim() : null);
        item.setDateLostFound(request.dateLostFound);
        item.setLocation(request.location.trim());
        item.setImageUrl(request.imageUrl);

        Item saved = itemRepository.save(item);
        return ItemResponse.from(saved);
    }

    public List<ItemResponse> getAllItems() {
        return itemRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ItemResponse::from)
                .collect(Collectors.toList());
    }

    public ItemResponse getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        return ItemResponse.from(item);
    }

    public List<ItemResponse> getMyItems(Long userId) {
        return itemRepository.findByReporter_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(ItemResponse::from)
                .collect(Collectors.toList());
    }
}