package com.example.findit.dto;

import java.time.LocalDate;

public class ItemRequest {
    public String type;          // "LOST" or "FOUND"
    public String name;
    public String category;
    public String description;
    public LocalDate dateLostFound;
    public String location;
    public String imageUrl;      // optional
}