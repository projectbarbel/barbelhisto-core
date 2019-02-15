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

# Two minutes tutorial
## Create an instance of BarbelHisto
Create an instance of `BarbelHisto`.
```java
BarbelHisto<Employee> core = BarbelHistoBuilder.barbel().build();
```
Notice that, when you're using Pojos, they need to be regular [JavaBeans](https://docs.oracle.com/javase/8/docs/technotes/guides/beans/index.html). Behind the scenes `BarbelHisto` will create proxies for Pojos and make copies to protect the state of your object.

## Store and retrieve one versions
You're ready to store instances of `Employee` to your `BarbelHisto` instance.
```java
Employee employee = new Employee("somePersonelNumber", "Niklas", "Schlimm");
core.save(employee, LocalDate.now(), LocalDate.MAX);
```
In the `Employee.class` you need to specify the `@DocumentId` so that `BarbelHisto`can group versions to a document.
```java
@DocumentId
private String personnelNumber; 
```
If you want to retrieve your `Employee`, you can do that by calling `retrieveOne`on `BarbelHisto`.
```java
Optional<Employee> effectiveNowOptional = core.retrieveOne(BarbelQueries.effectiveNow(employee.getId()));
```
Notice that you need to tell `BarbelHisto` what effective date your looking for. In the query above you're looking for the `Employee` version effective today. You can also ask for a specific date in the past or in the future.
```java
Optional<Employee> effectiveIn10DaysOptional = core.retrieveOne(BarbelQueries.effectiveAt(employee.personnelNumber, LocalDate.now().plusDays(10)));
```
That query retrieves the `Employee` version effective in to days. It will return one, cause you've stored the `Employee` version to be effective from now to infinite (`LocalDate.MAX`). If you retrieve the `Employee` effective yesterday you'll receive an empty `Optional` instead.
```java
Optional<Employee> effectiveYesterdayOptional = core.retrieveOne(BarbelQueries.effectiveAt(employee.personnelNumber, LocalDate.now().minusDays(1)));
assertFalse(effectiveYesterdayOptional.isPresent());
```
Let's look at the pretty print of the journal. GThat shows what `BarbelHisto` knows about your version.
```
|Version-ID                              |Effective-From |Effective-Until |State   |Created-By           |Created-At                                   |Inactivated-By       |Inactivated-At                               |Data                           |
|----------------------------------------|---------------|----------------|--------|---------------------|---------------------------------------------|---------------------|---------------------------------------------|-------------------------------|
|226ab05c-7c2d-4746-8861-18dc85a0188e    |2019-02-15     |999999999-12-31 |ACTIVE  |SYSTEM               |2019-02-15T08:46:56.495+01:00[Europe/Berlin] |NOBODY               |2199-12-31T23:59:00Z                         |EffectivePeriod [from=2019-02- |
```

## Store and retrieve two versions
So far you know how to store pojos to `BarbelHisto`. The real power of `BarbelHisto` is, however, to store changes to your `Employee` that become effective in the future (or became effective in the past). Here is how that works.
Let's retrieve our current employee version again.
```java
Optional<Employee> effectiveNowOptional = core.retrieveOne(BarbelQueries.effectiveNow(employee.getId()));
```
Now suppose that employee marries in 10 days, and that is supposed to become the day when the last name has to change.
```java
Employee effectiveEmployeeVersion = effectiveNowOptional.get();
effectiveEmployeeVersion.setLastname("changedLastName");
```
And store that version into `BarbelHisto` to become effective in 10 days.
```java
core.save(effectiveEmployeeVersion, LocalDate.now().plusDays(10), LocalDate.MAX);
```
Done. `BarbelHisto` now knows aboiut that change.
```
```
