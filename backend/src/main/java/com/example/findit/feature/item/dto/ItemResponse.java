package com.example.findit.feature.item.dto;

import com.example.findit.feature.item.entity.Item;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ItemResponse {
    public Long id;
    public String type;
    public String name;
    public String category;
    /**
     * Description excluded from public item listings.
     * It IS included here so that:
     *   - AdminClaimsPage can show claim.item.description in the expanded detail panel
     *   - ItemDetailPage can guard with {item.description && ...} — undefined renders nothing
     * The description stays in the DB; it just won't be shown in public card grids.
     */
    public String description;
    public LocalDate dateLostFound;
    public String location;
    public String imageUrl;
    /**
     * SDD Feature: weather context from Open-Meteo, stored at item creation time.
     * Displayed on item detail page as a weather context card.
     */
    public String weatherContext;
    public String status;
    public LocalDateTime createdAt;
    public ReporterInfo reporter;

    public static class ReporterInfo {
        public Long id;
        public String fullName;
    }

    public static ItemResponse from(Item item) {
        ItemResponse r = new ItemResponse();
        r.id = item.getId();
        r.type = item.getType();
        r.name = item.getName();
        r.category = item.getCategory();
        r.description = item.getDescription();
        r.dateLostFound = item.getDateLostFound();
        r.location = item.getLocation();
        r.imageUrl = item.getImageUrl();
        r.weatherContext = item.getWeatherContext();
        r.status = item.getStatus();
        r.createdAt = item.getCreatedAt();
        r.reporter = new ReporterInfo();
        r.reporter.id = item.getReporter().getId();
        r.reporter.fullName = item.getReporter().getFullName();
        return r;
    }
}
