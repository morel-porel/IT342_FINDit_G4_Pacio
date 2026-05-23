package com.example.findit.feature.item;

import com.example.findit.feature.item.dto.ItemRequest;
import com.example.findit.feature.item.dto.ItemResponse;
import com.example.findit.feature.item.entity.Item;
import com.example.findit.feature.user.User;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    @Value("${campus.lat:10.3157}")
    private String campusLat;

    @Value("${campus.lng:123.8854}")
    private String campusLng;

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    @Value("${SUPABASE_STORAGE_URL}")
    private String supabaseUrl;

    @Value("${SUPABASE_SERVICE_KEY}")
    private String supabaseServiceKey;
    @PostConstruct
    public void debugConfig() {
        System.out.println("[ItemService] Supabase URL: " + supabaseUrl);
        System.out.println("[ItemService] Service key set: " + (supabaseServiceKey != null && !supabaseServiceKey.isBlank()));
    }
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    // ─────────────────────────────────────────────────────────
    // POST /api/items — report a lost or found item
    // Handles multipart file upload and Open-Meteo weather fetch
    // ─────────────────────────────────────────────────────────
    public ItemResponse reportItem(ItemRequest request, MultipartFile photo, User reporter) {
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
        // SDD AC-5: photo required for found items
        if ("FOUND".equals(request.type) && (photo == null || photo.isEmpty())) {
            throw new RuntimeException("Photo is required for found items");
        }

        Item item = new Item();
        item.setReporter(reporter);
        item.setType(request.type);
        item.setName(request.name.trim());
        item.setCategory(request.category.trim());
        item.setDescription(request.description != null ? request.description.trim() : null);
        item.setDateLostFound(request.dateLostFound);
        item.setLocation(request.location.trim());

        // Handle image upload — store to /uploads/, save URL in record
        if (photo != null && !photo.isEmpty()) {
            String imageUrl = saveUploadedFile(photo);
            item.setImageUrl(imageUrl);
        }

        // SDD Feature: Open-Meteo weather integration
        // Fetch current weather at campus coordinates and store as context string
        String weatherContext = fetchWeatherContext();
        item.setWeatherContext(weatherContext);

        Item saved = itemRepository.save(item);
        return ItemResponse.from(saved);
    }

    // ─────────────────────────────────────────────────────────
    // Backward-compat overload for JSON body (no file)
    // ─────────────────────────────────────────────────────────
    public ItemResponse reportItem(ItemRequest request, User reporter) {
        return reportItem(request, null, reporter);
    }

    // ─────────────────────────────────────────────────────────
    // GET /api/items — public item feed with optional filters
    // ─────────────────────────────────────────────────────────
    public List<ItemResponse> getAllItems(String type, String status, String category) {
        List<Item> items = itemRepository.findAllByOrderByCreatedAtDesc();

        return items.stream()
                .filter(item -> type == null || type.isBlank() || item.getType().equalsIgnoreCase(type))
                .filter(item -> status == null || status.isBlank() || item.getStatus().equalsIgnoreCase(status))
                .filter(item -> category == null || category.isBlank() || item.getCategory().equalsIgnoreCase(category))
                .map(ItemResponse::from)
                .collect(Collectors.toList());
    }

    // Overload for backward compatibility
    public List<ItemResponse> getAllItems() {
        return getAllItems(null, null, null);
    }

    // ─────────────────────────────────────────────────────────
    // GET /api/items/{id}
    // ─────────────────────────────────────────────────────────
    public ItemResponse getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ITEM-001: Item not found"));
        return ItemResponse.from(item);
    }

    // ─────────────────────────────────────────────────────────
    // GET /api/items/my — logged-in user's own reports
    // ─────────────────────────────────────────────────────────
    public List<ItemResponse> getMyItems(Long userId) {
        return itemRepository.findByReporter_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(ItemResponse::from)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────
    // PUT /api/items/{id} — update item (owner or ADMIN)
    // SDD: Admin can update any item; User can only update their own OPEN items
    // ─────────────────────────────────────────────────────────
    public ItemResponse updateItem(Long id, ItemRequest request, User requester) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ITEM-001: Item not found"));

        boolean isAdmin = "ADMIN".equals(requester.getRole());
        boolean isOwner = item.getReporter().getId().equals(requester.getId());

        if (!isAdmin && !isOwner) {
            throw new RuntimeException("AUTH-003: You can only edit your own items");
        }
        if (!isAdmin && !"OPEN".equals(item.getStatus())) {
            throw new RuntimeException("ITEM-002: You can only edit items with OPEN status");
        }

        if (request.name != null && !request.name.isBlank()) item.setName(request.name.trim());
        if (request.category != null && !request.category.isBlank()) item.setCategory(request.category.trim());
        if (request.description != null) item.setDescription(request.description.trim());
        if (request.location != null && !request.location.isBlank()) item.setLocation(request.location.trim());
        if (request.dateLostFound != null) item.setDateLostFound(request.dateLostFound);
        if (request.type != null && !request.type.isBlank()) item.setType(request.type);

        Item saved = itemRepository.save(item);
        return ItemResponse.from(saved);
    }

    // ─────────────────────────────────────────────────────────
    // DELETE /api/items/{id} — delete item (owner or ADMIN)
    // ─────────────────────────────────────────────────────────
    public void deleteItem(Long id, User requester) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ITEM-001: Item not found"));

        boolean isAdmin = "ADMIN".equals(requester.getRole());
        boolean isOwner = item.getReporter().getId().equals(requester.getId());

        if (!isAdmin && !isOwner) {
            throw new RuntimeException("AUTH-003: You can only delete your own items");
        }
        if (!isAdmin && !"OPEN".equals(item.getStatus())) {
            throw new RuntimeException("ITEM-002: You can only delete items with OPEN status");
        }

        itemRepository.delete(item);
    }

    // ─────────────────────────────────────────────────────────
    // PATCH /api/items/{id}/resolve — owner marks item as RESOLVED
    // SDD Journey 3: User can mark lost item as RESOLVED if found
    // ─────────────────────────────────────────────────────────
    public ItemResponse resolveItem(Long id, User requester) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ITEM-001: Item not found"));

        if (!item.getReporter().getId().equals(requester.getId())) {
            throw new RuntimeException("AUTH-003: You can only resolve your own items");
        }

        item.setStatus("RESOLVED");
        Item saved = itemRepository.save(item);
        return ItemResponse.from(saved);
    }

    // ─────────────────────────────────────────────────────────
    // Open-Meteo weather fetch
    // SDD Feature: https://open-meteo.com — free, no API key required
    // Returns a human-readable string e.g. "Partly Cloudy, 29°C"
    // ─────────────────────────────────────────────────────────
    private String fetchWeatherContext() {
        try {
            String url = String.format(
                "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current_weather=true",
                campusLat, campusLng
            );

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            String body = resp.body();

            // Parse JSON manually to avoid adding Jackson dependency complexity
            // Extract: current_weather.temperature and current_weather.weathercode
            double temperature = extractDouble(body, "temperature");
            int weathercode = (int) extractDouble(body, "weathercode");

            String condition = mapWeatherCode(weathercode);
            return String.format("%s, %.0f°C", condition, temperature);

        } catch (Exception e) {
            System.err.println("[ItemService] Could not fetch weather from Open-Meteo: " + e.getMessage());
            return null; // weather is optional context — don't fail item creation
        }
    }

    private double extractDouble(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return 0;
        start += search.length();
        // skip whitespace
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-')) end++;
        try {
            return Double.parseDouble(json.substring(start, end));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * WMO Weather Code to human-readable description
     * https://open-meteo.com/en/docs — weather code table
     */
    private String mapWeatherCode(int code) {
        if (code == 0)            return "Clear Sky";
        if (code == 1)            return "Mainly Clear";
        if (code == 2)            return "Partly Cloudy";
        if (code == 3)            return "Overcast";
        if (code == 45 || code == 48) return "Foggy";
        if (code >= 51 && code <= 55) return "Drizzle";
        if (code >= 61 && code <= 65) return "Rain";
        if (code >= 71 && code <= 75) return "Snow";
        if (code == 80 || code == 81 || code == 82) return "Rain Showers";
        if (code >= 95)           return "Thunderstorm";
        return "Cloudy";
    }

    // ─────────────────────────────────────────────────────────
    // File upload — save to /uploads/ directory on the server
    // SDD AC-10: Images stored on server in /uploads directory
    // ─────────────────────────────────────────────────────────
    private String saveUploadedFile(MultipartFile file) {
        if (supabaseUrl == null || supabaseUrl.isBlank()) {
            throw new RuntimeException("SYS-001: Supabase is not configured. Set SUPABASE_URL and SUPABASE_SERVICE_KEY in your .env");
        }
        try {
            String contentType = file.getContentType();
            if (contentType == null ||
                    (!contentType.equals("image/jpeg") && !contentType.equals("image/png")
                            && !contentType.equals("image/webp"))) {
                throw new RuntimeException("FILE-002: Only JPG, PNG and WEBP files are accepted");
            }
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new RuntimeException("FILE-001: File size must not exceed 5MB");
            }

            String originalFilename = file.getOriginalFilename();
            String extension = (originalFilename != null && originalFilename.contains("."))
                    ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                    : ".jpg";
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // Upload to Supabase Storage via REST API
            String uploadUrl = supabaseUrl + "/storage/v1/object/item-photos/" + uniqueFilename;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uploadUrl))
                    .header("Authorization", "Bearer " + supabaseServiceKey)
                    .header("Content-Type", contentType)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200 && response.statusCode() != 201) {
                throw new RuntimeException("SYS-001: Supabase upload failed: " + response.body());
            }

            // Return the public URL
            return supabaseUrl + "/storage/v1/object/public/item-photos/" + uniqueFilename;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("SYS-001: Failed to store uploaded file: " + e.getMessage());
        }
    }
}
