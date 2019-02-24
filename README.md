# barbelhisto
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://travis-ci.org/projectbarbel/barbelhisto-core.svg?branch=master)](https://travis-ci.org/projectbarbel/barbelhisto-core)
![Maven Central](https://img.shields.io/maven-central/v/org.projectbarbel/barbelhisto.svg)
![Codecov](https://img.shields.io/codecov/c/github/projectbarbel/barbelhisto-core.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=org.projectbarbel%3Abarbelhisto&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.projectbarbel%3Abarbelhisto)

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
In the `Employee.class` you need to specify the `@DocumentId` so that `BarbelHisto`can group versions to a document. In the `Employee.class` the `personnelNumber` is the document ID. 
```java
public static class Employee {
   @DocumentId
   private String personnelNumber; 
   private String firstname; 
   private String lastname;
   private List<Adress> adresses = new ArrayList<>();
   ... constructor and accessor methods
}
```
The document ID must be unique for the document from a business viewpoint. An employee can be uniquely identified by his personnel number. <br>
If you want to retrieve your current `Employee` version, you can do that by calling `retrieveOne`on `BarbelHisto`.
```java
Employee effectiveEmployeeVersion = core.retrieveOne(BarbelQueries.effectiveNow(employee.getId()));
```
Notice that you need to tell `BarbelHisto` what effective date your looking for. In the query above you're looking for the `Employee` version effective now (today). You can also ask for a specific date in the past or in the future.
```java
Employee effectiveIn10Days = core.retrieveOne(BarbelQueries.effectiveAt(employee.personnelNumber, LocalDate.now().plusDays(10)));
```
That query retrieves the `Employee` version effective in ten days. It will return one, cause you've stored the `Employee` version to be effective from now to infinite (`LocalDate.MAX`). If you retrieve the `Employee` effective yesterday you'll receive an `IllegalStateException` claiming that no value can be found. This strict treatment is to avoid `NullPointerException` somewhere later in the process. However, this query throws an exception, cause nothing was effective yesterday:
```java
Employee effectiveYesterday = core.retrieveOne(BarbelQueries.effectiveAt(employee.personnelNumber, LocalDate.now().minusDays(1)));
```
## Accessing bitemporal version metadata
Whenever you receive data from `BarbelHisto` with `retrieve`-methods all the objects carry a `BitemporalStamp` as version stamp. This stamp contains all the version data for that object. You can receive that as follows:
```java
BitemporalStamp versionData = ((Bitemporal)effectiveEmployeeVersion).getBitemporalStamp();
```
That `BitemporalStamp` contains the effective time and record time data for that given object.
## Printing pretty journals
Let's look at a pretty print of a document journal for a document ID. The pretty print shows what `BarbelHisto` knows about your data. It prints out the version data of of the given document ID in a table format. 
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
## Make a bitemporal update
So far you know how to store POJOs to `BarbelHisto`. The real power of `BarbelHisto` is, however, to store changes to your `Employee` that become effective in the future (or became effective in the past). Here is how simple such an update works.
Let's retrieve a copy of our current employee version. (**clients only ever retrieve copies!**)
```java
Employee effectiveEmployeeVersion = core.retrieveOne(BarbelQueries.effectiveNow(employee.getId()));
```
Now suppose that the employee marries in 10 days, and that is supposed to become the day when the last name has to change.
```java
effectiveEmployeeVersion.setLastname("changedLastName");
core.save(effectiveEmployeeVersion, LocalDate.now().plusDays(10), LocalDate.MAX);
```
Done. `BarbelHisto` now knows about that change, it will make a snapshot and store that internally. You could safely continue to work with your employee version and save that again later. <br>
If you retrieve versions now, you may become different states of the employee, since you've recorded a change.
```java
effectiveEmployeeVersion = core.retrieveOne(BarbelQueries.effectiveNow(employee.getId()));
effectiveIn10Days = core.retrieveOne(BarbelQueries.effectiveAt(employee.personnelNumber, LocalDate.now().plusDays(10)));
assertTrue(effectiveEmployeeVersion.getLastname().equals("Schlimm"));
assertTrue(effectiveIn10Days.getLastname().equals("changedLastName"));
```
The `effectiveEmployeeVersion` is that version you've stored in the beginning of this tutorial, the `effectiveIn10Days` version will be the one with the changed last name. <br>
Let's also have a look at the pretty print of that journal now. Again call:
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
As you can see the journal of that employee now contains three versions. Two versions with `ACTIVE` state and one with `INACTIVE` state. The active versions are effective from 2019-02-15 (today) and effective from 2019-02-25. There is one inactivated version, the one you've stored in the beginning of that tutorial, effective from 2019-02-15 until `LocalDate.MAX`. `BarbelHisto` manages two time dimensions, one reflects the effective time, and another one, redord time, reflects when a change was made. For that reason, **nothing will ever be deleted**. There are **only inserts** to `BarbelHiso` backbone collections, **never deletions**. 
# Timeshifts 
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
Whe you use custom persistence then `BarbelHisto` can export the backbone version data of a given document ID by calling the `BarbelHisto.unload()` and `BarbelHisto.load()` methods. Let's say you've created the data with `BarbelHisto`, then you export the data like so:
```java
Collection<Bitemporal> unload = core.unload("somePersonelNumber");
```
Notice that an unload removes that versions from the backbone collection. You can now store the complete version data for that given document ID to a data store of your choice. Later you can `BarbelHisto.load()` that complete journal to restore the `BarbelHisto` instance if you want to process further updates for a given document ID. 
```java
Collection<Bitemporal> versionData = // ... some custom persistence service here that draws 
                                     //     the version data for 'somePersonelNumber' from the data store!
core.load(versionData);
```
The `BarbelHisto` instance must not contain any of the version data for the document IDs you try to load, otherwise you receive errors. This was made to ensure consistency of the version data.
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
# Memory analysis
Memory analysis results. One hour run, 5000 Objects saved every 10 seconds. `OnHeapPersistence`. 
<br>Result details [here](https://github.com/projectbarbel/barbelhisto-core/blob/master/performance/BarbelCoreSaveMemoryTest.txt).
![Memory](https://user-images.githubusercontent.com/876604/53019849-fb084b00-3455-11e9-8625-6c2bf1be1576.png)
