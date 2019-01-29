# barbelhisto
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A lightweiht easy to use bullet proof Java library to store the history of changes of domain objects in bi-temporal format. 

barbelhisto tracks two time dimensions for you: firstly, when a change to a domain object was recorded in the system (record time) and secondly, when this change is opposed to become effective or valid from a business viewpoint (effective time).

The library implements Martin Fowlers Temporal Pattern that can be found here: https://martinfowler.com/eaaDev/timeNarrative.html

# Who needs barbelhisto?

<script src="https://gist.github.com/nisrulz/11c0d63428b108f10c83.js"></script>

When you use barbelhisto to store and read data you can find answers to these questions:

- What changes to domain objects were stored the last two weeks?
- When will the state of domain objects become effective?
- What will be the effective state of my domain objects in two weeks?

Even more complicated:

- Two month ago, what did we know about the state of domain objects six month ago?
- Two month ago, what did we know about the state of domain objects in four months?

This library enables you to store your data in a format that enables you to answer these questions.

# Features

- bi-temporal data storage for No SQL and SQL data stores
- small easy-to-use API (as simple as bi-temporal data storage can get)
- works out of the box with default implementation and configuration
- manage different time zones of client and server
- build-in database support for different data stores
- provides basic functionality, extendible for individual needs
- remember every change to the data, nothing is ever going to be deleted
- build-in thread safety by minimizing mutability
- implemented in plain Java with no dependencies
- highly configurable to adopt to specific needs
- pretty print function to learn about bitemporal data storage

# Build-in backends

In general barbelhisto is implemented idepentently of the targeted data store. Build in data stores already implemented are:

- mongoDB
- Redis
- Cassandra

You can easily implement your own DAO object to integrate any data store you want to use.

# Getting started

Maven Central link
Example code using the API
Demo App
