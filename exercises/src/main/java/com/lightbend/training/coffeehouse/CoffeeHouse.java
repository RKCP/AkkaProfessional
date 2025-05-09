package com.lightbend.training.coffeehouse;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import scala.concurrent.duration.FiniteDuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import static com.google.common.base.Preconditions.checkNotNull;

public class CoffeeHouse extends AbstractLoggingActor {

    private final FiniteDuration prepareCoffeeDuration = FiniteDuration.create(
            context().system().settings().config().getDuration(
                    "coffee-house.barista.prepare-coffee-duration",
                    MILLISECONDS), MILLISECONDS
    );
    private final FiniteDuration coffeeFinishedDuration = FiniteDuration.create(
            context().system().settings().config().getDuration(
                    "coffee-house.guest.finish-coffee-duration",
                    MILLISECONDS), MILLISECONDS
            );
    // depend on finiteDurations to pull from config, so put after. waiter relies on barista, hence its order
    private final ActorRef barista = createBarista();
    private final ActorRef waiter = createWaiter();
    private final int caffineLimit;
    private final Map<ActorRef, Integer> guestbook = new HashMap<>();

    public CoffeeHouse(int caffineLimit) {
        log().debug("CoffeeHouse Open");
        this.caffineLimit = caffineLimit;
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(CreateGuest.class, createGuest -> {
                    final ActorRef guest = createGuest(createGuest.favouriteCoffee);
                    addNewGuestToGuestbook(guest);
                })
                .match(ApproveCoffee.class, this::coffeeApproved, approveCoffee -> {
                    barista.forward(new Barista.PrepareCoffee(approveCoffee.coffee, approveCoffee.guest), context());
                })
                .match(ApproveCoffee.class, approveCoffee -> { // will fall through to this block if coffee wasn't approved
                    log().info("Sorry, {} is at the daily coffee limit.", approveCoffee.guest);
                    context().stop(approveCoffee.guest);
                })
                .build();
    }

    private boolean coffeeApproved(ApproveCoffee approveCoffee) {
        ActorRef guest = approveCoffee.guest;
        final int guestCoffeeCount = guestbook.get(guest);
        if (guestCoffeeCount < caffineLimit) {
            guestbook.put(guest, guestCoffeeCount + 1);
            log().info("Guest {} coffee count increased.", guest);
            return true;
        }
        return false;
    }

    private void addNewGuestToGuestbook(ActorRef guest) {
        guestbook.put(guest, 0);
        log().debug("Guest {} was added to guestbook", guest);
    }

    protected ActorRef createGuest(Coffee favouriteCoffee) {
        return context().actorOf(Guest.props(waiter, favouriteCoffee, coffeeFinishedDuration)); // creates a child actor instead of a top level actor. (due to using context())
    }

    public static Props props(int caffineLimit) {
        return Props.create(CoffeeHouse.class, () -> new CoffeeHouse(caffineLimit));
    }

    protected ActorRef createBarista() {
        return getContext().actorOf(Barista.props(prepareCoffeeDuration), "barista");
    }

    protected ActorRef createWaiter() {
        return getContext().actorOf(Waiter.props(self()), "waiter");
    } // waiter checks coffee house limit when requesting a coffee...

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

    public static final class ApproveCoffee {
        public final Coffee coffee;
        public final ActorRef guest;

        public ApproveCoffee(Coffee coffee, ActorRef guest) {
            checkNotNull(coffee, "Coffee cannot be null!");
            checkNotNull(guest, "Guest cannot be null!");
            this.coffee = coffee;
            this.guest = guest;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ApproveCoffee that = (ApproveCoffee) o;
            return Objects.equals(coffee, that.coffee) && Objects.equals(guest, that.guest);
        }

        @Override
        public int hashCode() {
            return Objects.hash(coffee, guest);
        }

        @Override
        public String toString() {
            return "ApproveCoffee{" +
                    "coffee=" + coffee +
                    ", guest=" + guest +
                    '}';
        }
    }
}
