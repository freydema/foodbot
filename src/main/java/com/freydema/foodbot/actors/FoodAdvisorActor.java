package com.freydema.foodbot.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.persistence.AbstractPersistentActor;
import com.freydema.foodbot.Command;
import com.freydema.foodbot.Event;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

public class FoodAdvisorActor extends AbstractPersistentActor {


    public static Props props(ActorRef outputActor){
        return Props.create(FoodAdvisorActor.class, outputActor);
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final ActorRef outputActor;
    private List<Food> foodList = new ArrayList<>();


    public FoodAdvisorActor(ActorRef outputActor) {
        this.outputActor = outputActor;
    }

    @Override
    public String persistenceId() {
        return "food-advisor-1";
    }

    @Override
    public Receive createReceiveRecover() {
        // TODO handle snapshots
        return receiveBuilder()
                .match(Event.class, this::updateState)
                .build();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(AddFoodCmd.class, this::processCommand)
                .match(ListFoodCmd.class, this::processCommand)
                .matchAny(o -> log.warning("Received unsupported message: {}", o))
                .build();
    }

    private void processCommand(AddFoodCmd cmd){
        FoodAddedEvt event = new FoodAddedEvt(cmd.getChatId(), cmd.getDescription());
        persist(event, (Event e) -> {
            updateState(e);
            getContext().getSystem().getEventStream().publish(e);
            // TODO create snapshots
        });
        String feedback = "Added to the list: " + event.description;
        outputActor.tell(new OutputActor.SendMessageAction(cmd.getChatId(), feedback), this.sender());
    }

    private void processCommand(ListFoodCmd cmd){
        StringBuilder feedback = new StringBuilder();
        feedback.append("Here is your food list:");
        int index = 1;
        for(Food food : foodList){
            feedback.append("\n").append(index++).append(" - ").append(food.getDescription());
        }
        outputActor.tell(new OutputActor.SendMessageAction(cmd.getChatId(), feedback.toString()), this.sender());
    }

    private void updateState(Event event) {
        if(event instanceof FoodAddedEvt) {
            FoodAddedEvt e = (FoodAddedEvt) event;
            Food food = new Food(e.getDescription());
            foodList.add(food);
            return;

        } else {
            log.warning("Unsupported event {}", event);
        }

    }



    //
    // Message definitions
    //

    @Value
    public static class Food {
        private String description;
    }

    @Value
    public static class AddFoodCmd implements Command {
        private Long chatId;
        private String description;
    }

    @Value
    public static class FoodAddedEvt implements Event {
        private Long chatId;
        private String description;
    }

    @Value
    public static class ListFoodCmd implements Command {
        private Long chatId;
    }



}
