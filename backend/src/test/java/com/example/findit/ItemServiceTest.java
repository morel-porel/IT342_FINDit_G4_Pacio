package com.example.findit;

import com.example.findit.feature.item.ItemRepository;
import com.example.findit.feature.item.dto.ItemRequest;
import com.example.findit.feature.item.ItemService;
import com.example.findit.feature.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock private ItemRepository itemRepository;
    @InjectMocks private ItemService itemService;

    @Test
    void reportItem_ShouldSaveCorrectly() {
        User reporter = new User();
        reporter.setId(1L);
        reporter.setFullName("User");

        ItemRequest req = new ItemRequest();
        req.type = "LOST";
        req.name = "iPhone";
        req.category = "Electronics";
        req.location = "Canteen";
        req.dateLostFound = LocalDate.now();

        when(itemRepository.save(any())).thenAnswer(i -> {
            var item = (com.example.findit.feature.item.entity.Item) i.getArguments()[0];
            item.setId(100L);
            return item;
        });

        var res = itemService.reportItem(req, reporter);

        assertEquals("OPEN", res.status);
        assertEquals("iPhone", res.name);
        verify(itemRepository).save(any());
    }

    @Test
    void reportItem_ShouldThrowOnBlankName() {
        ItemRequest req = new ItemRequest();
        req.name = ""; // Blank
        assertThrows(RuntimeException.class, () -> itemService.reportItem(req, new User()));
    }
}