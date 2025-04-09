package com.lightbend.training.coffeehouse;

public class CoffeeHouse extends AbstractLoggingActor {

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .matchAny(msg -> log().info("Coffee Elemental Brewing"))
                .build();
    }
}
