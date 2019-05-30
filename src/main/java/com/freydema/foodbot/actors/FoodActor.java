package com.freydema.foodbot.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.persistence.AbstractPersistentActor;
import com.freydema.foodbot.commands.AddFood;
import com.freydema.foodbot.commands.ListFood;
import com.freydema.foodbot.commands.RemoveFood;
import com.freydema.foodbot.commands.Start;
import com.freydema.foodbot.events.Event;
import lombok.Value;
import java.util.ArrayList;
import java.util.List;

import static com.freydema.foodbot.events.Events.*;




public class FoodActor extends AbstractPersistentActor {


    public static Props props(Long chatId, ActorRef outputActor){
        return Props.create(FoodActor.class, chatId, outputActor);
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Long chatId;
    private final ActorRef outputActor;

    private List<Food> foodList = new ArrayList<>();


    public FoodActor(Long chatId, ActorRef outputActor) {
        this.chatId = chatId;
        this.outputActor = outputActor;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        log.info("Started!");
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        log.info("Stopped!");
    }

    @Override
    public String persistenceId() {
        return "foodActor_" + chatId;
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
                .match(Start.class, this::processCommand)
                .match(AddFood.class, this::processCommand)
                .match(RemoveFood.class, this::processCommand)
                .match(ListFood.class, this::processCommand)
                .matchAny(o -> log.warning("Received unsupported message: {}", o))
                .build();
    }

    private void processCommand(Start cmd){
        // do nothing
    }

    private void processCommand(AddFood cmd){
        FoodAdded event = new FoodAdded(cmd.getChatId(), cmd.getDescription());
        persist(event, (Event e) -> {
            updateState(e);
            getContext().getSystem().getEventStream().publish(e);
            // TODO create snapshots
        });
        String feedback = "Added to the list: " + event.getDescription();
        outputActor.tell(new OutputActor.SendMessageAction(cmd.getChatId(), feedback), this.sender());
    }

    private void processCommand(RemoveFood cmd){
        FoodRemoved event = new FoodRemoved(cmd.getChatId(), cmd.getFoodIndex());
        persist(event, e -> {
            Food removedFood = onFoodRemoved(e);
            getContext().getSystem().getEventStream().publish(e);
            String feedback = "Removed from the list: " + event.getItemIndex() + " - " + removedFood.description;
            outputActor.tell(new OutputActor.SendMessageAction(cmd.getChatId(), feedback), this.sender());
        });
    }

    private void processCommand(ListFood cmd){
        StringBuilder feedback = new StringBuilder();
        feedback.append("Here is your food list:");
        int index = 1;
        for(Food food : foodList){
            feedback.append("\n").append(index++).append(" - ").append(food.getDescription());
        }
        outputActor.tell(new OutputActor.SendMessageAction(cmd.getChatId(), feedback.toString()), this.sender());
    }


    private Food onFoodRemoved(FoodRemoved event) {
        return foodList.remove(event.getItemIndex() -1);
    }

    private void updateState(Event event) {
        if(event instanceof FoodAdded) {
            FoodAdded e = (FoodAdded) event;
            Food food = new Food(e.getDescription());
            foodList.add(food);
            return;
        } else if(event instanceof FoodRemoved) {
            onFoodRemoved((FoodRemoved) event);

        } else {
            log.warning("Unsupported event {}", event);
        }

    }

    //
    // Food class
    //

    @Value
    private static class Food {
        private String description;
    }







}
