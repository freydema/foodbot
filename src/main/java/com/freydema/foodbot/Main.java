package com.freydema.foodbot;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.CoordinatedShutdown;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.freydema.foodbot.actors.FoodActor;
import com.freydema.foodbot.actors.FoodSupervisor;
import com.freydema.foodbot.actors.InputActor;
import com.freydema.foodbot.actors.OutputActor;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.Await;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws Exception {
        new Main().start();
    }


    private void start() throws Exception {
        Config config = ConfigFactory.load();
        String botToken = config.getString("botToken");
        String webhookHost = config.getString("webhookHost");
        int webhookPort = config.getInt("webhookPort");
        String webhookName = config.getString("webhookName");

        ActorSystem system = ActorSystem.create("echobot");
        Http http = Http.get(system);
        BotApiClient botApiClient = new BotApiClient(botToken, http);
        ActorRef outputActor = system.actorOf(OutputActor.props(botApiClient), "output");
        ActorRef foodSupervisor = system.actorOf(FoodSupervisor.props(outputActor), "foodSupervisor");
        ActorRef inputActor = system.actorOf(InputActor.props(foodSupervisor, outputActor), "input");

        ActorMaterializer materializer = ActorMaterializer.create(system);
        BotApiWebhook webhook = new BotApiWebhook(webhookName, system, inputActor);
        Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = webhook.createRoute().flow(system, materializer);
        ServerBinding serverBinding = http
                .bindAndHandle(routeFlow, ConnectHttp.toHost(webhookHost, webhookPort), materializer)
                .toCompletableFuture().get(3, TimeUnit.SECONDS);
        System.out.println("Bot webhook started on " + webhookHost + ":" + webhookPort + "/" + webhookName);
        CoordinatedShutdown.get(system).addJvmShutdownHook(() -> shutdown(serverBinding, system));
    }


    private void shutdown(ServerBinding serverBinding, ActorSystem system) {
        System.out.println("SHUTDOWN REQUESTED");
        serverBinding
                .terminate(Duration.ofSeconds(3))
                .thenRun(() -> {
                            try {
                                Await.result(
                                        system.terminate(),
                                        scala.concurrent.duration.Duration.create(10, TimeUnit.SECONDS)
                                );
                                System.out.println("SYSTEM TERMINATED");
                                System.out.println("EXIT");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                );

    }


}
