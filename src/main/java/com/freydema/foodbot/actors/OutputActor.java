package com.freydema.foodbot.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.freydema.foodbot.BotApiClient;
import lombok.Value;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;


public class OutputActor extends AbstractActor {

    public static Props props(BotApiClient botApiClient){
        return Props.create(OutputActor.class, botApiClient);
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final BotApiClient botApiClient;

    public OutputActor(BotApiClient botApiClient) {
        this.botApiClient = botApiClient;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SendMessageAction.class, this::processAction)
                .matchAny(o -> log.warning("Received unsupported message: {}", o))
                .build();
    }

    private void processAction(SendMessageAction action){
        SendMessage sendMessageRequest = new SendMessage(action.getChatId(), action.getText());
        botApiClient.post(sendMessageRequest);
        log.info("Sent: {}", action);
    }

    //
    // Message definitions
    //

    @Value
    public static class SendMessageAction{
        private Long chatId;
        private String text;
    }


}
