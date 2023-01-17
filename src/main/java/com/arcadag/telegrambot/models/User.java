package com.arcadag.telegrambot.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity(name = "tg_data")
public class User {
    @Id
    private Long id;
    private String name;
    private int msg_numb;
}
