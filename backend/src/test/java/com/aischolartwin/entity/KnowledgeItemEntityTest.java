package com.aischolartwin.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeItemEntityTest {

    @Test
    @DisplayName("Should initialize KnowledgeItem with default category and timestamps")
    void shouldInitializeWithDefaults() {
        User user = new User("Ada Lovelace", "ada@example.com", "hash", Role.USER);
        KnowledgeItem item = new KnowledgeItem(user, KnowledgeItemType.NOTE, "Babbage Engines", "Analytical Engine notes", null, null, null);
        item.onCreate();

        assertEquals(KnowledgeItemType.NOTE, item.getType());
        assertEquals("Babbage Engines", item.getTitle());
        assertEquals("General", item.getCategory());
        assertNotNull(item.getCreatedAt());
        assertNotNull(item.getUpdatedAt());
    }

    @Test
    @DisplayName("Updating item sets updatedAt timestamp and preserves category")
    void shouldUpdateTimestampsOnUpdate() {
        User user = new User("Alan Turing", "alan@example.com", "hash", Role.USER);
        KnowledgeItem item = new KnowledgeItem(user, KnowledgeItemType.LINK, "Turing Machine Paper", null, "https://turingarchive.org/paper.pdf", "Original manuscript", "Theoretical CS");
        item.onCreate();

        assertEquals("Theoretical CS", item.getCategory());
        item.onUpdate();
        assertNotNull(item.getUpdatedAt());
    }

    @Test
    @DisplayName("toString() does not log large content bodies")
    void toStringShouldNotDumpMassiveContent() {
        User user = new User("Grace Hopper", "grace@example.com", "hash", Role.USER);
        KnowledgeItem item = new KnowledgeItem(user, KnowledgeItemType.NOTE, "Compiler Notes", "Very long content...", null, null, "Compilers");
        item.setId(99L);

        String str = item.toString();
        assertTrue(str.contains("Compiler Notes"));
        assertTrue(str.contains("Compilers"));
        assertFalse(str.contains("Very long content"));
    }
}

