# barbelhisto

A lightweiht easy to use bullet proof library to store the history of changes of domain objects in two dimensional format.

The library implements Martin Fowlers Temporal Pattern that can be found here: https://martinfowler.com/eaaDev/timeNarrative.html

# Who needs barbelhisto?

Whe you use barbelhisto to store an read data you can find answers to these questions:

- What was the effective state of my domain objects two weeks ago?
- What will be the effective state of my domain objects in two weeks?
- When were the changes to domain objects stored in the system?

Even more complicated:

- Two month ago, what did we know about the state of domain objects six month ago?
- Two month ago, what did we know about the state of domain objects in four months?

This library enables you to store your data in a format that enables you to answer these questions.

# Supported backends

In general barbelhisto is implemented idepentently of the targeted data store. Build in data stores supported are:

- mongoDB
- Redis
- Cassandra

You can easily implement your own dao object to integrate any data store you want to use.

# Getting started

