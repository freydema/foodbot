package com.freydema.foodbot;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import com.freydema.foodbot.actors.InputActor;
import org.telegram.telegrambots.meta.api.objects.Update;

public class BotApiWebhook extends AllDirectives {

    private final LoggingAdapter log;
    private final ActorRef inputActor;
    private final String webhookName;

    public BotApiWebhook(String webhookName, ActorSystem system, ActorRef inputActor) {
        this.webhookName = webhookName;
        this.log = Logging.getLogger(system, this);
        this.inputActor = inputActor;
    }

    public Route createRoute() {
        return pathPrefix(webhookName, () ->
                pathEnd(() ->
                        post(() ->
                                entity(Jackson.unmarshaller(Update.class), u -> {
                                            log.info("Received {}", u);
//                                            Integer userId = u.getMessage().getFrom().getId();
//                                            String message = u.getMessage().getText();
                                            inputActor.tell(u, ActorRef.noSender());
                                            return complete(StatusCodes.OK);
                                        }
                                )))
        );

    }

}
