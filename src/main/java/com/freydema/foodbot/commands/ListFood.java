package com.freydema.foodbot.commands;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper=true)
public class ListFood extends Command {

    public ListFood(Long chatId) {
        super(chatId);
    }
}