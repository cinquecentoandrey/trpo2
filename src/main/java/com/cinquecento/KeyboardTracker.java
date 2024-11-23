package com.cinquecento;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The KeyboardTracker class listens for keyboard events (key press and key release)
 * and logs these events to a file. It uses an observer pattern to publish key events.
 * The class tracks key events until the "Escape" key is pressed, at which point it stops tracking.
 * Key events are logged with a timestamp and the event type (key pressed or key released).
 */
public class KeyboardTracker {

    /**
     * DateTimeFormatter to format the timestamp of key events in the format "yyyy-MM-dd HH:mm:ss".
     */
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * A PublishSubject that publishes keyboard events to subscribers.
     */
    private final PublishSubject<NativeKeyEvent> keyEventPublicSubject = PublishSubject.create();

    /**
     * The key that ends the tracking (the "Escape" key).
     */
    private final String END_EVENT_CODE = "Escape";

    /**
     * A FileWriter instance responsible for writing key events to a file.
     */
    private final FileWriter fileWriter;

    /**
     * Constructor for initializing the KeyboardTracker with a FileWriter instance.
     *
     * @param fileWriter the FileWriter instance used to write key events to a file.
     */
    public KeyboardTracker(FileWriter fileWriter) {
        this.fileWriter = fileWriter;
    }

    /**
     * Returns an Observable that emits key events as they occur.
     *
     * @return an Observable of NativeKeyEvent representing key events.
     */
    public Observable<NativeKeyEvent> getKeyEvents() {
        return keyEventPublicSubject;
    }

    /**
     * Starts tracking keyboard events by registering a global native hook.
     * The method listens for key press and release events and publishes them through the PublishSubject.
     */
    public void startTracking() {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new NativeKeyListener() {

                /**
                 * Handles a key press event. If the "Escape" key is pressed, the tracking stops.
                 * Otherwise, the key press event is published.
                 *
                 * @param nativeEvent the key press event.
                 */
                @Override
                public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
                    if (NativeKeyEvent.getKeyText(nativeEvent.getKeyCode()).equals(END_EVENT_CODE)) {
                        fileWriter.close();
                        stopTracking();
                    } else {
                        keyEventPublicSubject.onNext(nativeEvent);
                    }
                }

                /**
                 * Handles a key release event and publishes it.
                 *
                 * @param nativeEvent the key release event.
                 */
                @Override
                public void nativeKeyReleased(NativeKeyEvent nativeEvent) {
                    keyEventPublicSubject.onNext(nativeEvent);
                }

            });
        } catch (Exception e) {
            keyEventPublicSubject.onError(new IOException(e.getLocalizedMessage()));
        }
    }

    /**
     * Stops the key event tracking and completes the PublishSubject.
     */
    public void stopTracking() {
        keyEventPublicSubject.onComplete();
    }

    /**
     * Writes a key event to the file, including the event type (key pressed or key released),
     * the key name, and the timestamp when the event occurred.
     *
     * @param nativeKeyEvent the key event to log.
     */
    public void writeEvent(NativeKeyEvent nativeKeyEvent) {
        String eventType = getEventType(nativeKeyEvent);
        fileWriter.append("%s: %s. Time: %s".formatted(eventType, NativeKeyEvent.getKeyText(nativeKeyEvent.getKeyCode()), LocalDateTime.now().format(formatter)));
    }

    /**
     * Determines the type of the event based on the key event ID.
     *
     * @param nativeKeyEvent the key event.
     * @return a string representing the type of the key event ("Key Pressed", "Key Released", etc.).
     */
    private String getEventType(NativeKeyEvent nativeKeyEvent) {
        return switch (nativeKeyEvent.getID()) {
            case 2401 -> "Key Pressed";
            case 2402 -> "Key Released";
            default -> "Unknown Key Event";
        };
    }
}
