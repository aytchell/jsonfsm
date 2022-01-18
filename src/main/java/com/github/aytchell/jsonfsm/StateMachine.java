package com.github.aytchell.jsonfsm;

import java.util.Set;

/**
 * A state machine closely related to DFA and UML state chart diagrams.
 * <p>
 * The state machine will accept (a given set of) events and change it's
 * internal state accordingly. It can return if one of several final states
 * is reached.
 * <p>
 * Beneath that it can execute arbitrary commands when a state is entered or exited
 * as well as during traversal of a transition.
 */
public interface StateMachine {
    /**
     * Injects an event into the state machine.
     * <p>
     * If the event is accepted by the state machine (i.e. valid for the current state
     * according to the json-encoded state machine) the state machine will execute
     * (if present) onExit behaviors, transition events and onEntry behaviors (in that order)
     * and finally end up in a new state.
     * <p>
     * Note that all these actions are done synchronously. Note also that single actions might
     * fail (with a {@code RuntimeException}) which will then be logged; but there's no negative
     * impact on the other actions nor the state machine.
     *
     * @param eventSourceId ID of the source where this event comes from
     * @param eventPayload  payload of the event
     * @return {@code true} if a final state has been reached; {@code false} otherwise
     */
    boolean injectEvent(int eventSourceId, String eventPayload);

    /**
     * Set of device IDs which will be used by this state machine.
     * <p>
     * This is basically the same information as returned by
     * {@link StateMachineCompiler#getRequiredDevices}.
     *
     * @return a set of used device IDs
     */
    Set<Integer> getControlledDeviceIds();

    /**
     * Set of IDs of event sources which are accepted by this instance.
     * <p>
     * This is basically the same information as returned by
     * {@link StateMachineCompiler#getAcceptedEventSources}. Note that for each event
     * source ID there is only a limited set of accepted "event payloads". These payloads
     * where defined in the json-encoded state machine given to {@link StateMachineParser#parse}.
     *
     * @return a set of event source IDs which are accepted by this state machine instance
     */
    Set<Integer> getHandledEventSourceIds();

    /**
     * Returns the name of the current state the machine is in.
     *
     * @return Name of the current state
     */
    String getCurrentState();

    /**
     * Says whether the current state is a final state or not.
     *
     * @return {@code true} if the current state is final; {@code false} otherwise
     */
    boolean isCurrentStateFinal();
}
