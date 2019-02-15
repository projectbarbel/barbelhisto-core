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

- bi-temporal (auditing-proof) data storage based on blazing fast [cqengine](https://github.com/npgall/cqengine) collections
- small easy-to-use API (as simple as bi-temporal data storage can get)
- backed by transactional, high performance colections
- works out of the box with default implementation and configuration
- manage different time zones of client and server
- persistence support for heap, off-heap, disk storage and custom data stores
- provides basic functionality, extendible for individual needs
- remember every change to the data, nothing is ever going to be deleted
- build-in thread safety by minimizing mutability and object locking
- pretty print function to learn about bitemporal data storage

# Get started
Download the [actual snapshot releases](https://github.com/projectbarbel/barbelhisto-core/releases). Maven will follow as soon Version 1.0 is released.

Or clone this git repository to try some of the features of `BarbelHisto`.

# Two minutes tutorial
[See this test case](https://github.com/projectbarbel/barbelhisto-core/blob/master/src/test/java/org/projectbarbel/histo/BarbelHistoCore_StdPojoUsage_Test.java) to get the complete code for this tutorial.

## Create an instance of BarbelHisto
Create an instance of `BarbelHisto`.
```java
BarbelHisto<Employee> core = BarbelHistoBuilder.barbel().build();
```
Notice that, when you're using Pojos, they need to be regular [JavaBeans](https://docs.oracle.com/javase/8/docs/technotes/guides/beans/index.html). Behind the scenes `BarbelHisto` will create proxies for Pojos and make copies to protect the state of your object.

## Store and retrieve a version
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
Let's look at the pretty print of the journal. The pretty print shows what `BarbelHisto` knows about your employee.
```java
System.out.println(core.prettyPrintJournal(employee.getId()));
```
That prints the following to your console.

**NOTE: when I wrote this Readme.md it was 2019 February the 15th**

```
Document-ID: somePersonelNumber

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
Done. `BarbelHisto` now knows about that change. If you retrieve versions now, you may become different states of the employee, since you've recorded a change in near future.
```java
effectiveNowOptional = core.retrieveOne(BarbelQueries.effectiveNow(employee.getId()));
effectiveIn10DaysOptional = core.retrieveOne(BarbelQueries.effectiveAt(employee.personnelNumber, LocalDate.now().plusDays(10)));

effectiveEmployeeVersion = effectiveNowOptional.get();
Employee effectiveIn10DaysVersion = effectiveIn10DaysOptional.get();
        
assertTrue(effectiveEmployeeVersion.getLastname().equals("Schlimm"));
assertTrue(effectiveIn10DaysVersion.getLastname().equals("changedLastName"));
```
Let's also look at the pretty print of that journal. Again call:
```java
System.out.println(core.prettyPrintJournal(employee.getId()));
```
That should return the following journal now.
```
Document-ID: somePersonelNumber

|Version-ID                              |Effective-From |Effective-Until |State   |Created-By           |Created-At                                   |Inactivated-By       |Inactivated-At                               |Data                           |
|----------------------------------------|---------------|----------------|--------|---------------------|---------------------------------------------|---------------------|---------------------------------------------|-------------------------------|
|226ab05c-7c2d-4746-8861-18dc85a0188e    |2019-02-15     |999999999-12-31 |INACTIVE|SYSTEM               |2019-02-15T08:46:56.495+01:00[Europe/Berlin] |SYSTEM               |2019-02-15T08:46:56.546+01:00[Europe/Berlin] |EffectivePeriod [from=2019-02- |
|c2d8a5b8-a8cf-4f19-aeb4-4ca61b4f8f70    |2019-02-15     |2019-02-25      |ACTIVE  |SYSTEM               |2019-02-15T08:46:56.541+01:00[Europe/Berlin] |NOBODY               |2199-12-31T23:59:00Z                         |EffectivePeriod [from=2019-02- |
|c9302f79-9c7b-4b4a-b011-8bb6177278af    |2019-02-25     |999999999-12-31 |ACTIVE  |SYSTEM               |2019-02-15T08:46:56.536+01:00[Europe/Berlin] |NOBODY               |2199-12-31T23:59:00Z                         |EffectivePeriod [from=2019-02- |
```
As you may recognise the journal of that employee not contains some versions of the employee. Two versions with `ACTIVE` state and one with `INACTIVE` state. The active versions are effective from today (2019-02-15) and another one effective from in 10 days, which is 2019-02-25. There is one inactivated version, the one you've stored in the beginning, effective from now until infinite. `BarbelHisto` manages two time dimensions, one reflects the effective time, and another one, redord time, reflects when a change was made. For that reason, **nothing will ever be deleted**. There are **only inserts** to `BarbelHiso` backbone collections, **never deletions**.
