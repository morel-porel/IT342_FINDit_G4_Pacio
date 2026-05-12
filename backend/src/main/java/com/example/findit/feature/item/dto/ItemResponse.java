package com.example.findit.feature.item.dto;

import com.example.findit.feature.item.entity.Item;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ItemResponse {
    public Long id;
    public String type;
    public String name;
    public String category;
    // description intentionally omitted from public listing per SDD:
    // "Item description is hidden from regular users — only admin sees it during claim review"
    public LocalDate dateLostFound;
    public String location;
    public String imageUrl;
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
        r.dateLostFound = item.getDateLostFound();
        r.location = item.getLocation();
        r.imageUrl = item.getImageUrl();
        r.status = item.getStatus();
        r.createdAt = item.getCreatedAt();
        r.reporter = new ReporterInfo();
        r.reporter.id = item.getReporter().getId();
        r.reporter.fullName = item.getReporter().getFullName();
        return r;
    }
}