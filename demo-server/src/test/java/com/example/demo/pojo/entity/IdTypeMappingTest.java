package com.example.demo.pojo.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IdTypeMappingTest {

    @Test
    void followsIdShouldUseLongForBigintColumn() throws NoSuchFieldException {
        Field field = Follows.class.getDeclaredField("id");

        assertEquals(Long.class, field.getType());
    }

    @Test
    void thumbsUpIdShouldUseLongForBigintColumn() throws NoSuchFieldException {
        Field field = Thumbs_up.class.getDeclaredField("id");

        assertEquals(Long.class, field.getType());
    }
}
