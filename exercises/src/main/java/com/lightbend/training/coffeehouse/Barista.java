package com.lightbend.training.coffeehouse;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import scala.concurrent.duration.FiniteDuration;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public class Barista extends AbstractLoggingActor {

    private final FiniteDuration prepareCoffeeDuration;

    public Barista(FiniteDuration prepareCoffeeDuration) {
        this.prepareCoffeeDuration = prepareCoffeeDuration;
    }

    public static Props props(FiniteDuration prepareCoffeeDuration) {
        return Props.create(Barista.class, ()-> new Barista(prepareCoffeeDuration));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(PrepareCoffee.class, prepareCoffee -> {
                    Busy.busy(this.prepareCoffeeDuration);
                    sender().tell(new CoffeePrepared(prepareCoffee.coffeeToPrepare, prepareCoffee.customer), self());
                }).build();
    }


    // implement message protocol
    public static final class PrepareCoffee {
        public final Coffee coffeeToPrepare;
        public final ActorRef customer;

        public PrepareCoffee(Coffee coffeeToPrepare, ActorRef customer) {
            checkNotNull(coffeeToPrepare, "Coffee cannot be null");
            checkNotNull(customer, "Guest (Customer) cannot be null");
            this.coffeeToPrepare = coffeeToPrepare;
            this.customer = customer;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PrepareCoffee that = (PrepareCoffee) o;
            return Objects.equals(coffeeToPrepare, that.coffeeToPrepare) && Objects.equals(customer, that.customer);
        }

        @Override
        public int hashCode() {
            return Objects.hash(coffeeToPrepare, customer);
        }

        @Override
        public String toString() {
            return "PrepareCoffee{" +
                    "coffeeToPrepare=" + coffeeToPrepare +
                    ", customer=" + customer +
                    '}';
        }
    }

    public static final class CoffeePrepared {
        public final Coffee coffeePrepared;
        public final ActorRef customer;

        public CoffeePrepared(Coffee coffeePrepared, ActorRef customer) {
            checkNotNull(coffeePrepared, "Coffee cannot be null");
            checkNotNull(customer, "Guest (Customer) cannot be null");
            this.coffeePrepared = coffeePrepared;
            this.customer = customer;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CoffeePrepared that = (CoffeePrepared) o;
            return Objects.equals(coffeePrepared, that.coffeePrepared) && Objects.equals(customer, that.customer);
        }

        @Override
        public int hashCode() {
            return Objects.hash(coffeePrepared, customer);
        }

        @Override
        public String toString() {
            return "CoffeePrepared{" +
                    "coffeePrepared=" + coffeePrepared +
                    ", customer=" + customer +
                    '}';
        }
    }
}
