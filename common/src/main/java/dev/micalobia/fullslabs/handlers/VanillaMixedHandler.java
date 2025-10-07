package dev.micalobia.fullslabs.handlers;

public class VanillaMixedHandler implements MixedHandler {
    private static final Requirements REQUIREMENTS = Requirements.EMPTY;

    @Override
    public Requirements requirements() {
        return REQUIREMENTS;
    }
}
