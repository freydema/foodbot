package com.freydema.foodbot.commands;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class AddFood extends Command {

    String description;

    public AddFood(Long chatId, String description) {
        super(chatId);
        this.description = description;
    }
}