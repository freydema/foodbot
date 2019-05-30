package com.freydema.foodbot.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.freydema.foodbot.commands.*;

import java.util.HashMap;
import java.util.Map;


public class FoodSupervisor extends AbstractActor {

    public static Props props(ActorRef outputActor) {
        return Props.create(FoodSupervisor.class, outputActor);
    }

    private LoggingAdapter log = Logging.getLogger(context().system(), this);
    private ActorRef outputActor;
    private Map<Long, ActorRef> foodActorsByChatId;


    public FoodSupervisor(ActorRef outputActor) {
        this.outputActor = outputActor;
        foodActorsByChatId = new HashMap<>();
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Start.class, this::forwardCommand)
                .match(AddFood.class, this::forwardCommand)
                .match(RemoveFood.class, this::forwardCommand)
                .match(ListFood.class, this::forwardCommand)
                .matchAny(o -> log.warning("Received unsupported message: {}", o))
                .build();
    }

    private void forwardCommand(Command command) {
        Long chatId = command.getChatId();
        if (!foodActorsByChatId.containsKey(chatId)) {
            ActorRef foodActor = context().actorOf(FoodActor.props(chatId, outputActor), "foodActor-" + chatId);
            foodActorsByChatId.put(chatId, foodActor);
        }
        ActorRef actor = foodActorsByChatId.get(chatId);
        log.info("Forwarding {} to {}", command, actor);
        actor.forward(command, context());

    }
}
