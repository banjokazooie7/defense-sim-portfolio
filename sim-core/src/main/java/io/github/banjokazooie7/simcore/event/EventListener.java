package io.github.banjokazooie7.simcore.event;

@FunctionalInterface
public interface EventListener {
    void onEvent(SimEvent event, long simTime);
}
