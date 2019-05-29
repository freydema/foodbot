package com.freydema.foodbot.actors;

import akka.actor.AbstractActor;
import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

public class InputActor extends AbstractActor {

    public static Props props(ActorRef foodAdvisorActor, ActorRef outputActor) {
        return Props.create(InputActor.class, foodAdvisorActor, outputActor);
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final ActorRef foodAdvisorActor;
    private final ActorRef outputActor;

    public InputActor(ActorRef foodAdvisorActor, ActorRef outputActor) {
        this.foodAdvisorActor = foodAdvisorActor;
        this.outputActor = outputActor;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Update.class, this::processUpdate)
                .matchAny(o -> log.warning("Received unsupported message: {}", o))
                .build();
    }


    private void processUpdate(Update u) {
        log.info("Processing update: {}", u);
        Long chatId = u.getMessage().getChat().getId();
        String text = u.getMessage().getText();
        if (text == null || text.trim().length() == 0) {
            return;
        }
        if (text.startsWith("/")) {
            // CASE 1: user sent a command
            String[] tokens = text.split(" ");
            String commandName = tokens[0];
            List<String> parameters = new ArrayList<>();
            if(tokens.length > 1) {
                for (int i = 1; i < tokens.length; i++) {
                    parameters.add(tokens[i]);
                }
            }
            handleCommand(chatId, commandName, parameters);
        } else {
            // CASE 2: user sent free text
            handleDefault(chatId, text);
        }
    }

    private void handleCommand(Long chatId, String commandName, List<String> parameters) {
        switch (commandName) {
            case "/add":
                handleAddFoodCommand(chatId, commandName, parameters);
                break;
            case "/list":
                handleListFoodCommand(chatId, commandName, parameters);
                break;
            default:
                handleUnknownCommand(chatId, commandName, parameters);
                break;
        }
    }


    private void handleAddFoodCommand(Long chatId, String commandName, List<String> parameters) {
        String description = String.join(" ", parameters);
        foodAdvisorActor.tell(new FoodAdvisorActor.AddFoodCmd(chatId, description), this.sender());
    }

    private void handleListFoodCommand(Long chatId, String commandName, List<String> parameters) {
        foodAdvisorActor.tell(new FoodAdvisorActor.ListFoodCmd(chatId), this.sender());
    }

    private void handleUnknownCommand(Long chatId, String commandName, List<String> parameters) {
        OutputActor.SendMessageAction action = new OutputActor.SendMessageAction(chatId, "Unsupported command: " + commandName);
        outputActor.tell(action, getSender());
    }

    private void handleDefault(Long chatId, String text) {
        OutputActor.SendMessageAction action = new OutputActor.SendMessageAction(chatId, "Not sure what you mean...");
        outputActor.tell(action, getSender());
    }


}
