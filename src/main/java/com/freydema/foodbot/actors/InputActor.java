package com.freydema.foodbot.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.freydema.foodbot.commands.AddFood;
import com.freydema.foodbot.commands.ListFood;
import com.freydema.foodbot.commands.RemoveFood;
import com.freydema.foodbot.commands.Start;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

public class InputActor extends AbstractActor {

    public static Props props(ActorRef foodSupervisor, ActorRef outputActor) {
        return Props.create(InputActor.class, foodSupervisor, outputActor);
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final ActorRef foodSupervisor;
    private final ActorRef outputActor;

    public InputActor(ActorRef foodSupervisor, ActorRef outputActor) {
        this.foodSupervisor = foodSupervisor;
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
            case "/start":
                handleStartCommand(chatId);
                break;
            case "/add":
                handleAddFoodCommand(chatId, commandName, parameters);
                break;
            case "/remove":
                handleRemoveFoodCommand(chatId, commandName, parameters);
                break;
            case "/list":
                handleListFoodCommand(chatId, commandName, parameters);
                break;
            default:
                handleUnknownCommand(chatId, commandName, parameters);
                break;
        }
    }


    private void handleStartCommand(Long chatId) {
        foodSupervisor.tell(new Start(chatId), this.sender());
    }

    private void handleAddFoodCommand(Long chatId, String commandName, List<String> parameters) {
        String description = String.join(" ", parameters);
        foodSupervisor.tell(new AddFood(chatId, description), this.sender());
    }

    private void handleRemoveFoodCommand(Long chatId, String commandName, List<String> parameters) {
        int itemIndex = Integer.parseInt(parameters.get(0));
        foodSupervisor.tell(new RemoveFood(chatId, itemIndex), this.sender());
    }

    private void handleListFoodCommand(Long chatId, String commandName, List<String> parameters) {
        foodSupervisor.tell(new ListFood(chatId), this.sender());
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
