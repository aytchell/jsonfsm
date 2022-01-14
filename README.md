# A "json to statemachine" library

This library can take a json-encoded state machine and create a runnable state
machine from it. The supported state machine features and names are modeled
along the UML state chart diagram (which itself is based on finite automatas).

It allows not only to track a state but also to execute custom commands while
the states are traversed.

## Features

The describable state machines consist of

* a set of `states`
* one of them being the `initialState`
* a set of `finalStates` ("accepting states"); the state machine will report
    if one of them is reached; processing can nevertheless continue
* `transitions` defining when to change from state to state
* a set of `triggers`; they are matched against incoming events (the "input
    alphabet") and might trigger traversal of a transition
* a state can have one or more `onEntry` behaviors attached. These are
    commands (code given by the user of the library) that are executed when
    the state is entered via a transition.
* a state can also have one or more `onExit` behaviors attached which are
    executed when leaving the state
* a transition can have one or more `effects` behaviors attached which are
    executed when traversing the transition

## Examples

## Maven

```xml
    <dependency>
        <groupId>com.github.aytchell</groupId>
        <artifactId>json-statemachine</artifactId>
        <version>2.0.0</version>
    </dependency>
```

## State-machine json


## License

Apache 2.0 License

Created and maintained by [Hannes Lerchl](mailto:hannes.lerchl@googlemail.com)

Feel free to send in pull requests. Please also add unit tests and adapt the
README if appropriate.
