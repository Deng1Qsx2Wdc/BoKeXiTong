package com.example.demo.pojo.entity;

import lombok.Data;

import java.util.Date;

@Data
public class WebSocketMessage {
    private String type;

    private String message;
    private String fromId;
    private String toId;

    private Date timestamp;

    private Object payload;
}
