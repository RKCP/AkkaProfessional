package com.lightbend.training.coffeehouse;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

public class CoffeeHouse extends AbstractLoggingActor {

    public CoffeeHouse() {
        log().debug("CoffeeHouse Open");
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(CreateGuest.class, createGuest -> createGuest())
                .build();
    }

    protected void createGuest() {
        context().actorOf(Guest.props()); // creates a child actor instead of a top level actor.
    }

    public static Props props() {
        return Props.create(CoffeeHouse.class, CoffeeHouse::new);
    }

    public static final class CreateGuest {

        public static final CreateGuest Instance = new CreateGuest(); // cant create instances since constructor is private. Can only CreateGuest.Instance.

        private CreateGuest() {} // private constructor to not ruin the singleton pattern
    }
}
