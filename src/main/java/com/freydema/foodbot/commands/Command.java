package com.freydema.foodbot.commands;

import lombok.Data;

import java.io.Serializable;

@Data
public abstract class Command implements Serializable {
    private final Long chatId;
}