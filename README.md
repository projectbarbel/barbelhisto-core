# barbelhisto
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://app.travis-ci.com/projectbarbel/barbelhisto-core.svg?branch=master)](https://app.travis-ci.com/projectbarbel/barbelhisto-core)
[![Maven Central](https://img.shields.io/maven-central/v/org.projectbarbel/barbelhisto.svg)](https://search.maven.org/search?q=barbelhisto)

A lightweiht easy to use Java library to store the history of changes of domain objects in bi-temporal format. 

`BarbelHisto` tracks two time dimensions for you: **record time** is when a change to a domain object was recorded in the system (created and inactivated) and **effective time** is when this change is opposed to become effective or valid from a business viewpoint.

The library implements Martin Fowlers Temporal Pattern that can be found here: https://martinfowler.com/eaaDev/timeNarrative.html

# Features

- bi-temporal (auditing-proof) data storage based on blazing fast [cqengine](https://github.com/npgall/cqengine) collections
- small easy-to-use API (as simple as bi-temporal data storage can get)
- backed by transactional, high performance collections
- works out of the box with default implementation and configuration
- manage different time zones of client and server
- persistence support for heap, off-heap, disk storage and custom data stores
- provides basic functionality, extendible for individual needs
- remember every change to the data, nothing is ever going to be deleted
- build-in thread safety by minimizing mutability and object locking
- pretty print function to learn about bitemporal data storage

# Get a version of BarbelHisto
Get a version on [Maven Central](https://search.maven.org/search?q=barbelhisto).

Download the [actual snapshot releases](https://github.com/projectbarbel/barbelhisto-core/releases). 

Or clone this git repository to try some of the features of `BarbelHisto`.

# Getting started with POJOs
- [Get started with Spring turorial](https://dzone.com/articles/integrating-bi-temporal-data-in-spring-boot-applic)
- [Get started with Mongo turorial](https://dzone.com/articles/setting-up-mongodb-for-bi-temporal-data-in-5-minut)

# Memory analysis
Memory analysis results. One hour run, 5000 Objects saved every 10 seconds. `OnHeapPersistence`. 
<br>Result details [here](https://github.com/projectbarbel/barbelhisto-core/blob/master/performance/BarbelCoreSaveMemoryTest.txt).
![Memory](https://user-images.githubusercontent.com/876604/53019849-fb084b00-3455-11e9-8625-6c2bf1be1576.png)
