package com.lightbend.training.coffeehouse;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public class Waiter extends AbstractLoggingActor {

    private ActorRef coffeeHouse;

    public Waiter(ActorRef coffeeHouse) {
        this.coffeeHouse = coffeeHouse;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ServeCoffee.class, serveCoffee -> // match on ServeCoffee.class. Whenever we receive a ServeCoffee, we will execute code in functional method.
                    this.coffeeHouse.tell(new CoffeeHouse.ApproveCoffee(serveCoffee.coffee, sender()),self())
//                    sender().tell(new CoffeeServed(serveCoffee.coffee), self()); // send new CoffeeServed msg, and the Coffee that was served, with self() as the sender (this actor))
                ).match(Barista.CoffeePrepared.class, coffeePrepared ->
                    coffeePrepared.customer.tell(new CoffeeServed(coffeePrepared.coffeePrepared), self()))
                .build();
    }

    public static Props props(ActorRef coffeeHouse) {
        return Props.create(Waiter.class, () -> new Waiter(coffeeHouse));
    }


    public static final class ServeCoffee {

        public final Coffee coffee; // can be public because actor can only be accessed via ActorRef, and its final.

        public ServeCoffee(Coffee coffee) {
            checkNotNull(coffee, "Coffee cannot be null!");
            this.coffee = coffee;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ServeCoffee that = (ServeCoffee) o;
            return Objects.equals(coffee, that.coffee);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(coffee);
        }

        @Override
        public String toString() {
            return "ServeCoffee{" +
                    "coffee=" + coffee +
                    '}';
        }
    }

    public static final class CoffeeServed {

        public final Coffee coffee;

        public CoffeeServed(Coffee coffee) {
            checkNotNull(coffee, "Coffee cannot be null!");
            this.coffee = coffee;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CoffeeServed that = (CoffeeServed) o;
            return Objects.equals(coffee, that.coffee);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(coffee);
        }

        @Override
        public String toString() {
            return "CoffeeServed{" +
                    "coffee=" + coffee +
                    '}';
        }
    }
}
