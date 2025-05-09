package com.lightbend.training.coffeehouse;

import akka.actor.ActorRef;
import akka.actor.Props;
import scala.concurrent.duration.FiniteDuration;

public class Guest extends AbstractLoggingActorWithTimers {

    private final ActorRef waiter;
    private final Coffee favouriteCoffee;
    private int coffeeCount = 0;
    private final FiniteDuration coffeeFinishedDuration;
    private final int caffeineLimit;

    public Guest(ActorRef waiter, Coffee favouriteCoffee, FiniteDuration coffeeFinishedDuration, int caffeineLimit) {
        this.waiter = waiter;
        this.favouriteCoffee = favouriteCoffee;
        this.coffeeFinishedDuration = coffeeFinishedDuration;
        this.caffeineLimit = caffeineLimit;
        orderFavouriteCoffee(); // once a guest enters the coffee house, we will order a coffee
    }

    @Override
    public Receive createReceive() {
//        return emptyBehavior(); // a way for us to create an actor with a behavior, without fleshing it out for now
        return receiveBuilder()
                .match(Waiter.CoffeeServed.class, coffeeServed -> {
                    coffeeCount++;
                    log().info("Enjoying my {} yummy {}!", coffeeCount, coffeeServed.coffee);
                    scheduleCoffeeFinished();
                })
                .match(CoffeeFinished.class, coffeeFinished -> coffeeCount > this.caffeineLimit, coffeeFinished -> {
                    throw new CaffeineException(); // if the count of coffee's drunk is more than the given limit
                })
                .match(CoffeeFinished.class, coffeeFinished ->
                    orderFavouriteCoffee())
                .build();
    }

    @Override
    public void postStop() {
        log().info("Goodbye!");
    }

    private void orderFavouriteCoffee() {
        this.waiter.tell(new Waiter.ServeCoffee(this.favouriteCoffee), self());
    }

    public static Props props(final ActorRef waiter, final Coffee favouriteCoffee, final FiniteDuration coffeeFinishedDuration, final int caffeineLimit) {
        return Props.create(Guest.class, () -> new Guest(waiter, favouriteCoffee, coffeeFinishedDuration, caffeineLimit));
    }

    private void scheduleCoffeeFinished() {
        // leverage timers in akka to send ourselves coffee finished, whenever we receive a finished coffee message
        getTimers().startSingleTimer("coffee-finished-key", CoffeeFinished.Instance
        , coffeeFinishedDuration);
    }

    public static final class CoffeeFinished {
        public static final CoffeeFinished Instance = new CoffeeFinished();

        private CoffeeFinished() {

        }
    }

    public static final class CaffeineException extends IllegalStateException {
        static final long serialVersionUID = 1;

        public CaffeineException() {
            super("Too much caffeiene!");
        }
    }
}
