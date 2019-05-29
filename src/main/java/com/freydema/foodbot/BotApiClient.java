package com.freydema.foodbot;

import akka.http.javadsl.Http;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.HttpRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;


public class BotApiClient {


    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final String baseUrl;
    private Http http;

    public BotApiClient(String botToken, Http http) {
        this.http = http;
        baseUrl ="https://api.telegram.org/bot" + botToken;

    }

    public void post(SendMessage sendMessageAction){
        try {
            String url = baseUrl+ "/" + sendMessageAction.getMethod();
            String payload = jsonMapper.writeValueAsString(sendMessageAction);
//            System.out.println("URL = " + url);
//            System.out.println("PAYLOAD = " + payload);
            HttpRequest request = HttpRequest.POST(url).withEntity(HttpEntities.create(ContentTypes.APPLICATION_JSON, payload));
            http.singleRequest(request);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
