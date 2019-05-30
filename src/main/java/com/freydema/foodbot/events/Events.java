package com.freydema.foodbot.events;

import lombok.Value;



public class Events {

    @Value
    public static class FoodAdded implements Event {
        private Long chatId;
        private String description;
    }

    @Value
    public static class FoodRemoved implements Event {
        private Long chatId;
        private int itemIndex;
    }
}
