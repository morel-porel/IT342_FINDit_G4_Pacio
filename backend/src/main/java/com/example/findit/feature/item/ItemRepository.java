package com.example.findit.feature.item;

import com.example.findit.feature.item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOrderByCreatedAtDesc();
    List<Item> findByReporter_IdOrderByCreatedAtDesc(Long reporterId);
    List<Item> findByTypeOrderByCreatedAtDesc(String type);
    List<Item> findByStatusOrderByCreatedAtDesc(String status);
}