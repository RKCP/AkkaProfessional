package com.lightbend.training.coffeehouse;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public class Waiter extends AbstractLoggingActor {

    private ActorRef coffeeHouse;
    private ActorRef barista;
    private int maxComplaintCount;
    private int complaintCounter;

    public Waiter(ActorRef coffeeHouse, ActorRef barista, int maxComplaintCount) {
        this.coffeeHouse = coffeeHouse;
        this.barista = barista;
        this.maxComplaintCount = maxComplaintCount;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ServeCoffee.class, serveCoffee -> // match on ServeCoffee.class. Whenever we receive a ServeCoffee, we will execute code in functional method.
                    this.coffeeHouse.tell(
                            new CoffeeHouse.ApproveCoffee(serveCoffee.coffee, sender()),self())
//                    sender().tell(new CoffeeServed(serveCoffee.coffee), self()); // send new CoffeeServed msg, and the Coffee that was served, with self() as the sender (this actor))
                ).match(Barista.CoffeePrepared.class, coffeePrepared ->
                    coffeePrepared.customer.tell(
                            new CoffeeServed(coffeePrepared.coffeePrepared), self()))
                .match(Complaint.class, complaint -> complaintCounter == this.maxComplaintCount, complaint -> { // complaintCounter == is the predicate
                    throw new FrustratedException();
                }) // if we receive a complaint, and we are at our max complaint count, then. do what it is lambda
                .match(Complaint.class, complaint -> {
                    complaintCounter++;
                    this.barista.tell(
                            new Barista.PrepareCoffee(complaint.coffee, sender()), self()); // sender() is the original guest who sent the req
                })
                .build();
    }

    public static Props props(ActorRef coffeeHouse, ActorRef barista, int maxComplaintCount) {
        return Props.create(Waiter.class, () -> new Waiter(coffeeHouse, barista, maxComplaintCount));
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

    public static final class Complaint {

        public final Coffee coffee;

        public Complaint(Coffee coffee) {
            checkNotNull(coffee, "Coffee cannot be null");
            this.coffee = coffee;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Complaint complaint = (Complaint) o;
            return Objects.equals(coffee, complaint.coffee);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(coffee);
        }

        @Override
        public String toString() {
            return "Complaint{" +
                    "coffee=" + coffee +
                    '}';
        }
    }

    public static final class FrustratedException extends IllegalStateException {

        static final long serialVersionUID = 1;

        public FrustratedException() {
            super("Too many complaints");
        }
    }
}
