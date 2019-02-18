# barbelhisto
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://travis-ci.org/projectbarbel/barbelhisto-core.svg?branch=master)](https://travis-ci.org/projectbarbel/barbelhisto-core)

A lightweiht easy to use Java library to store the history of changes of domain objects in bi-temporal format. 

`BarbelHisto` tracks two time dimensions for you: **record time** is when a change to a domain object was recorded in the system (created and inactivated) and **effective time** is when this change is opposed to become effective or valid from a business viewpoint.

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
- backed by transactional, high performance collections
- works out of the box with default implementation and configuration
- manage different time zones of client and server
- persistence support for heap, off-heap, disk storage and custom data stores
- provides basic functionality, extendible for individual needs
- remember every change to the data, nothing is ever going to be deleted
- build-in thread safety by minimizing mutability and object locking
- pretty print function to learn about bitemporal data storage

# Get started
Download the [actual snapshot releases](https://github.com/projectbarbel/barbelhisto-core/releases). 

Or clone this git repository to try some of the features of `BarbelHisto`.

# Getting started with POJOs
[See this test case](https://github.com/projectbarbel/barbelhisto-core/blob/master/src/test/java/org/projectbarbel/histo/BarbelHistoCore_StdPojoUsage_Test.java) to get the complete code for this tutorial.
> **NOTE**: in this turorial we use `BarbelHisto`s default processing mode, which is `BarbelMode.POJO`. There is another mode called `BarbelMode.BITEMPORAL` if proxying does not work out on your business classes. 
## Create an instance of BarbelHisto
Create an instance of `BarbelHisto`.
```java
BarbelHisto<Employee> core = BarbelHistoBuilder.barbel().build();
```

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
That query retrieves the `Employee` version effective in ten days. It will return one, cause you've stored the `Employee` version to be effective from now to infinite (`LocalDate.MAX`). If you retrieve the `Employee` effective yesterday you'll receive an empty `Optional` instead.
```java
Optional<Employee> effectiveYesterdayOptional = core.retrieveOne(BarbelQueries.effectiveAt(employee.personnelNumber, LocalDate.now().minusDays(1)));
assertFalse(effectiveYesterdayOptional.isPresent());
```
## Accessing bitemporal version metadata
Whenever you receive data from `BarbelHisto` with `retrieve`-methods all the objects carry a `BitemporalStamp` as version stamp. This stamp contains all the version data for that object. You can receive that as follows:
```java
Optional effectiveNowOptional = core.retrieveOne(BarbelQueries.effectiveNow(employee.getId()));
BitemporalStamp versionData = ((Bitemporal)effectiveNowOptional.get()).getBitemporalStamp();
```
That `BitemporalStamp` contains the effective time and record time data for that given object.
## Printing pretty journals
Let's look at a pretty print of a document journal. The pretty print shows what `BarbelHisto` knows about your data. It prints out the version data of each object in a table format. 
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
You can change the pretty printer and write your own. Look into `TableJournalPrettyPrinter` to see how to write an individual printer. You can register that printer with `BarbelHistoBuilder`.
## Store and retrieve two versions
So far you know how to store POJOs to `BarbelHisto`. The real power of `BarbelHisto` is, however, to store changes to your `Employee` that become effective in the future (or became effective in the past). Here is how that works.
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
## Timeshifts 
One of `BarbelHisto`s core functionality is doing timeshifts. With timeshifts you can look at past data as if it were still active. Let's suppose you did not make the updates from our previous example above at the same day like we just did in our turorial. Let's suppose we've created the `Employee` from our previous example on Feb 1st, 2019 and then made some changes today (here 2019, February 18th) that should become effective in the future. The journal of such a scenario looks like this:
```
Document-ID: somePersonelNumber

|Version-ID                              |Effective-From |Effective-Until |State   |Created-By           |Created-At                                   |Inactivated-By       |Inactivated-At                               |Data                           |
|----------------------------------------|---------------|----------------|--------|---------------------|---------------------------------------------|---------------------|---------------------------------------------|-------------------------------|
|5aec57db-0e4e-49b0-b79a-adf2affa8e13    |2019-02-18     |999999999-12-31 |INACTIVE|SYSTEM               |2019-02-01T00:00:00+01:00[Europe/Berlin]     |SYSTEM               |2019-02-18T11:57:24.738+01:00[Europe/Berlin] |EffectivePeriod [from=2019-02- |
|25b5e11c-70ce-4fc5-9b7a-ff9dd92d0dd2    |2019-02-18     |2019-02-28      |ACTIVE  |SYSTEM               |2019-02-18T11:57:24.738+01:00[Europe/Berlin] |NOBODY               |2199-12-31T23:59:00Z                         |EffectivePeriod [from=2019-02- |
|6fc067c7-57bc-4b69-9238-e19bd2269e0b    |2019-02-28     |999999999-12-31 |ACTIVE  |SYSTEM               |2019-02-18T11:57:24.738+01:00[Europe/Berlin] |NOBODY               |2199-12-31T23:59:00Z                         |EffectivePeriod [from=2019-02- |
``` 
When you want to see the journal that was active **before** you've made that second change on February 18th (see CreatedAt), you just receive that journal by using `BarbelHisto`s timeshift function:
```java
DocumentJournal journal = core.timeshift("somePersonelNumber", LocalDate.of(2019,2,17)); // yesterday in our scenarion was 2019, Febuary 17th
```
The journal you receive in the `DocumentJournal` looks as follows:
``` 
Document-ID: somePersonelNumber

|Version-ID                              |Effective-From |Effective-Until |State   |Created-By           |Created-At                                   |Inactivated-By       |Inactivated-At                               |Data                           |
|----------------------------------------|---------------|----------------|--------|---------------------|---------------------------------------------|---------------------|---------------------------------------------|-------------------------------|
|ab4abf42-2561-4a6e-afe7-5c8938736080    |2019-02-18     |999999999-12-31 |ACTIVE  |SYSTEM               |2019-02-01T00:00:00+01:00[Europe/Berlin]     |NOBODY               |2199-12-31T23:59:00Z                         |EffectivePeriod [from=2019-02- |
```
It's exactly that journal that was active before you've made your second update. These kind of scenarios can get much more complex as you continuously change employee records or othe bitemporal data. `BarbelHisto` considerably reduces the amount of complexity for developers dealing with such requirements.
# Adding persistence
In `BarbelHisto` there are two flavors of persistence, you can choose the cqengine persistence options or you choose a custom persistence.
## cqengine built-in persistence
`BarbelHisto` is based on [cqengine collections](https://github.com/npgall/cqengine) so clients can use any persistence options currently available in cqengine. The default persistence of `BarbelHisto` is `OnHeapPersistence`. To change that you add a custom backbone collection. If you want to add `DiskPersistence` you'd need to define the primary key attribute as known from cqengine:
```java
final SimpleAttribute<PrimitivePrivatePojo, String> PRIMARY_KEY = new SimpleAttribute<PrimitivePrivatePojo, String>("documentId") {
    public String getValue(PrimitivePrivatePojo object, QueryOptions queryOptions) {
        return (String) ((Bitemporal)object).getBitemporalStamp().getVersionId();
    }
};
```
POJOs get passed as `Bitemporal` proxies, that's why you can cast them to `Bitemporal` to get access to the Version information via the `BitemporalStamp`. In the example we use a standard POJO `PrimitivePrivatePojo` annotated with the `@DocumentId` annotation decribed previously. The field definition `PRIMARY_KEY` returns the version id as primary key for the persistence index. Now, create a persistent collection with `BarbelHisto` using the standard cqengine API and `BarbelHistoBuilder`: 
```java
BarbelHisto<PrimitivePrivatePojo> core = BarbelHistoBuilder.barbel() .withBackboneSupplier(
                  ()-> new ConcurrentIndexedCollection<PrimitivePrivatePojo>(DiskPersistence.
                       onPrimaryKeyInFile(PRIMARY_KEY, new File(FILENAME))))
                  .build();
```
See the [cqengine documentation](https://github.com/npgall/cqengine) on all the options you can choose. 
## Custom persistence
Whe you use custom persistence then `BarbelHisto` can export the backbone version data by calling the `BarbelHisto.dump()` and `BarbelHisto.populate()` methods. Let's say you've created the data with `BarbelHisto`, then you export the data like so:
```java
Coillection<Bitemporal> versionData = core.dump(DumpMode.CLEARCOLLECTION);
```
The CLEARCOLLECTION option clears the underlying backbone. You don't want that to happen in many cases, so you can also call that method using READONLY. You can now store the version data to a data store of your choice. Make sure you import the complete journal later to restore the `BarbelHisto` instance.
```java
Collection<Bitemporal> versionData = // ... some custom persistence service here that draws 
                                     //     the data from the data store!
histo.populate(versionData);
```
The `BarbelHisto` instance must be empty to use populate, otherwise you receive errors.
# Adding indexes 
See the [cqengine documentation](https://github.com/npgall/cqengine) for adding indexes and then, again, add your custom collection as backbone collection as described previously using the `BarbelHistoBuilder` class. Here is an example of adding an indexed collection as backbone. First define the index field.
```java
public static final SimpleAttribute<Object, String> VERSION_ID_PK = new SimpleAttribute<Object, String>(
        "documentId") {
    public String getValue(Object object, QueryOptions queryOptions) {
        return (String) ((Bitemporal)object).getBitemporalStamp().getVersionId();
    }
};
```
POJOs get passed as `Bitemporal` proxies, thats why you can cast them to `Bitemporal` to get access to the Version information via the `BitemporalStamp` 
Then add the backbone collection to `BarbelHisto`.
```java
BarbelHisto<T> core = BarbelHistoBuilder.barbel().withBackboneSupplier(()->{
                    IndexedCollection<T> backbone = new ConcurrentIndexedCollection<T>();
                    backbone.addIndex((Index<T>) NavigableIndex.onAttribute(VERSION_ID_PK));
                    return backbone;
                    }).build();
```
# Custom features
`BarbelHisto` offers lots of custom options so you can adopt it to the requirements of your project. See `BarbelHistoBuilder` for details.
