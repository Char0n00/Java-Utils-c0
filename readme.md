# C0 Java Utilities

This repository contains my implementations for certain utilities/services in Java that can be reuses across projects. Implementations I currently have:

* ## Logger
    [Logger](/logger/readme.md) : A singleton console and file logger. 
    It submits log writes to a separate thread so log calls don't effect performance of the app. When writing to files, it can "collapse" duplicate lines. TODO - implement compression of files 
    - For examples, see the [tests](/logger/src/test/java/c0/util/logger/).
* ## Object Pool
    [Object Pool](/objectpool/readme.md) : A singleton object pool with daemon services. (TODO, still not fully implemented)
    The object pool maintains a limited queue for each type of pooled object. Object lifetimes outside of the pool can be managed with reference counting for each object. A daemon can be run which averages incoming requests for object types and shrinks/expands their pools as needed, periodically clears the pools and calls for garbage collection from the JVM.
    - For examples, see the [tests](/objectpool/src/test/java/c0/util/objectpool/).
* ## Event Bus
    [Event Bus](/eventbus/readme.md) : An event bus that is meant to be used as a centralized service for sending events. 
    Included are some event wrappers that can be extended for custom events, with custom payloads defined as records.
    - For examples, see the [tests](/eventbus/src/test/java/c0/util/eventbus/).
* ## Observer System
    [Observer](/observer/readme.md) : An observer system that helps for easier management of observers and observables.
    It allows an observable class to instantiate a bus, and dynamically add as many notifiers to it as needed, which each maintain observers of a single type. Through custom interfaces, it allows each custom observer type to define multiple methods to be implemented that each then can be called through a simple handler with a lambda.
    - For examples, see the [tests](/ovserver/src/test/java/c0/util/observer/).

# Modules
I've separated these utilities into maven modules. Some of them have dependencies on each other, for example the event bus is dependendant on object pooling for helping with some of the overhead of frequent event instantiation. 
These are not hosted anywhere, so easiest way to install them is to clone the repository and run `mvn clean install` to install the modules and make them available locally or add compiled .jar files to your project.