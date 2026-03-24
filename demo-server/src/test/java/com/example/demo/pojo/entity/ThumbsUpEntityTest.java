package com.example.demo.pojo.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThumbsUpEntityTest {

    @Test
    void thumbsUpTimeShouldMatchTimestampColumnType() throws NoSuchFieldException {
        Field field = Thumbs_up.class.getDeclaredField("thumbsUpTime");

        assertEquals(Date.class, field.getType());
    }
}
