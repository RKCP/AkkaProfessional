package com.lightbend.training.coffeehouse;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class Guest extends AbstractLoggingActor {

    private final ActorRef waiter;
    private final Coffee favouriteCoffee;
    private int coffeeCount = 0;

    public Guest(ActorRef waiter, Coffee favouriteCoffee) {
        this.waiter = waiter;
        this.favouriteCoffee = favouriteCoffee;
    }

    @Override
    public Receive createReceive() {
//        return emptyBehavior(); // a way for us to create an actor with a behavior, without fleshing it out for now
        return receiveBuilder()
                .match(Waiter.CoffeeServed.class, coffeeServed -> {
                    coffeeCount++;
                    log().info("Enjoying my {} yummy {}!", coffeeCount, coffeeServed.coffee);
                })
                .match(CoffeeFinished.class, coffeeFinished -> {
                    this.waiter.tell(new Waiter.ServeCoffee(this.favouriteCoffee), self());
                })
                .build();
    }

    public static Props props(final ActorRef waiter, final Coffee favouriteCoffee) {
        return Props.create(Guest.class, () -> new Guest(waiter, favouriteCoffee));
    }

    public static final class CoffeeFinished {
        public static final CoffeeFinished Instance = new CoffeeFinished();

        private CoffeeFinished() {

        }
    }
}
