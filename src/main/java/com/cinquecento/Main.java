package com.cinquecento;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    /**
     * Logger instance for logging events and errors.
     */
    private final static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        KeyboardTracker keyboardTracker = new KeyboardTracker(new FileWriter("out.txt", 25L));

        keyboardTracker.getKeyEvents()
                .doOnNext(keyboardTracker::writeEvent)
                .doOnError(throwable -> {
                    logger.log(Level.WARNING, "An error occurred: %s".formatted(throwable.getLocalizedMessage()));
                })
                .doOnComplete(() -> {
                    logger.log(Level.INFO, "Input finished successfully!");
                })
                .subscribe();
        new Thread(keyboardTracker::startTracking).start();
    }
}
