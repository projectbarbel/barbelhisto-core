# barbelhisto

A lightweiht easy to use bullet proof library to store the history of changes to domain objects in two dimensional format.

The library implements Martin Fowlers Temporal Pattern that can be found here: https://martinfowler.com/eaaDev/timeNarrative.html

This library will help you, if you want to record the history of changes to domain objects. Insurance businesses have this requirement typically when they manage their policies. They need to know the state of a client policy two month ago. Also they need to make updates to policies today like adress changes, but the new adress will be valid in the future, say in two or three month. Even more important to insurance businesses is the question what coverage a client had when an accident happened two weeks ago. Insurance businesses are legally obligated two store their policy data in a format that enables them to find correct answers to these questions. 

To achieve that this library enables you to store your domain objects with two time dimensions: the transaction time and the effective time. The transaction time will record when an object was created and deleted. The effective time stores the period when the correspoding domain object is valid. A change to a domain object may be stored today, but may get effective in two weeks. Imagine a client calls the call center to inform them about an adress change that will get effective in two weeks, because that is his date of his removal.

Lets make this concrete:

... table format examples ...
