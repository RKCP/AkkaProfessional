package com.lightbend.training.coffeehouse;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

public class Guest extends AbstractLoggingActor {


    @Override
    public Receive createReceive() {
        return emptyBehavior(); // a way for us to create an actor with a behavior, without fleshing it out for now
    }

    public static Props props() {
        return Props.create(Guest.class, Guest::new);
    }
}
