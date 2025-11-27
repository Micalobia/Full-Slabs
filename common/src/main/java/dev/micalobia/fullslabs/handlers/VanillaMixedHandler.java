package dev.micalobia.fullslabs.handlers;

public class VanillaMixedHandler implements MixedHandler {
    final boolean valid;

    public static final VanillaMixedHandler INSTANCE = new VanillaMixedHandler(true);
    public static final VanillaMixedHandler INVALID = new VanillaMixedHandler(false);

    private VanillaMixedHandler(boolean valid) {this.valid = valid;}
}
