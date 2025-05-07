package com.lightbend.training.coffeehouse;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import scala.concurrent.duration.FiniteDuration;

import java.util.Objects;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import static com.google.common.base.Preconditions.checkNotNull;

public class CoffeeHouse extends AbstractLoggingActor {

    private final ActorRef waiter = createWaiter();

    private final FiniteDuration coffeeFinishedDuration = FiniteDuration.create(
            context().system().settings().config().getDuration(
                    "coffee-house.guest.finish-coffee-duration",
                    MILLISECONDS), MILLISECONDS
            );

    public CoffeeHouse() {
        log().debug("CoffeeHouse Open");
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(CreateGuest.class, createGuest -> createGuest(createGuest.favouriteCoffee))
                .build();
    }

    protected void createGuest(Coffee favouriteCoffee) {
        context().actorOf(Guest.props(waiter, favouriteCoffee, coffeeFinishedDuration)); // creates a child actor instead of a top level actor. (due to using context())
    }

    public static Props props() {
        return Props.create(CoffeeHouse.class, CoffeeHouse::new);
    }

    protected ActorRef createWaiter() {
        return getContext().actorOf(Waiter.props(), "waiter");
    }

    public static final class CreateGuest {

//        public static final CreateGuest Instance = new CreateGuest(); // cant create instances since constructor is private. Can only CreateGuest.Instance.
        public final Coffee favouriteCoffee;

        public CreateGuest(Coffee favouriteCoffee) {
            checkNotNull(favouriteCoffee, "Favourite coffee cannot be null");
            this.favouriteCoffee = favouriteCoffee;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CreateGuest that = (CreateGuest) o;
            return Objects.equals(favouriteCoffee, that.favouriteCoffee);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(favouriteCoffee);
        }

        @Override
        public String toString() {
            return "CreateGuest{" +
                    "favouriteCofee=" + favouriteCoffee +
                    '}';
        }
    }
}
