package com.example.demo.pojo.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FavoritesEntityTest {

    @Test
    void favoritesTimeShouldMatchTimestampColumnType() throws NoSuchFieldException {
        Field field = Favorites.class.getDeclaredField("favoritesTime");

        assertEquals(Date.class, field.getType());
    }
}
