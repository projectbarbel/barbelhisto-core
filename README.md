# barbelhisto
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://travis-ci.org/projectbarbel/barbelhisto-core.svg?branch=master)](https://travis-ci.org/projectbarbel/barbelhisto-core)

A lightweiht easy to use bullet proof Java library to store the history of changes of domain objects in bi-temporal format. 

barbelhisto tracks two time dimensions for you: firstly, when a change to a domain object was recorded in the system (record time) and secondly, when this change is opposed to become effective or valid from a business viewpoint (effective time).

The library implements Martin Fowlers Temporal Pattern that can be found here: https://martinfowler.com/eaaDev/timeNarrative.html

# Who needs barbelhisto?

When you use barbelhisto to store and read data you can find answers to these questions:

- What changes to domain objects were stored the last two weeks?
- When will the state of domain objects become effective?
- What will be the effective state of my domain objects in two weeks?

Even more complicated:

- Two month ago, what did we know about the state of domain objects six month ago?
- Two month ago, what did we know about the state of domain objects in four months?

This library enables you to store your data in a format that enables you to answer these questions.

# Features (in development)

- bi-temporal data storage based on blazing fast [cqengine](https://github.com/npgall/cqengine) collections
- small easy-to-use API (as simple as bi-temporal data storage can get)
- backed by transactional, high performance colections
- works out of the box with default implementation and configuration
- manage different time zones of client and server
- persistence support for heap, off-heap, disk storage and custom data stores
- provides basic functionality, extendible for individual needs
- remember every change to the data, nothing is ever going to be deleted
- build-in thread safety by minimizing mutability and object locking
- pretty print function to learn about bitemporal data storage

# Getting started

COMING SOON !!

Maven Central link
Example code using the API
Demo App
