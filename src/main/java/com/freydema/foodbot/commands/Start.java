package com.freydema.foodbot.commands;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper=true)
public class Start extends Command {

    public Start(Long chatId) {
        super(chatId);
    }
}