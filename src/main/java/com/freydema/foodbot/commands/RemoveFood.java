package com.freydema.foodbot.commands;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class RemoveFood extends Command {

    int foodIndex;

    public RemoveFood(Long chatId, int foodIndex) {
        super(chatId);
        this.foodIndex = foodIndex;
    }
}