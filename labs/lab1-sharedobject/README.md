# Lab 1

In this lab, we will perform operations on a shared object. That is,
two or more users who are far apart want to edit a shared
database. All of the users would like to see the results of their own
updates right away, despite delays in the network. It's OK for users
to temporarily see inconsistent states of the document, owing to
network delay, so long as the inconsistencies are fairly quickly
corrected. Ideally, even if the network is partitioned, we would like
individual users to be able to keep working, though this is not always
possible.

Our specification is as follows:

- Assumption: Ordered, reliable network channels (for example using TCP)
- Guarantee: Eventual consistency

Our database will be a simple key/value store where the keys and
values are strings. The store provides the following commands:

- `Put(key,value)`:  replaces the value of `key` with `value`, returning `PutOk()`,

- `Get(key)`: returns `GetResult(value)` where `value` is the value of `key` or `KeyNotFound()` if key is not present,

- `Append(key,value)`: append `value` to the value of `key` (or the
  empty sting if `key` is note present, and return the value after
  appending.

The `Put` and `Appends` are updates to the database, while `Get` is a
read-only query. You can tell if a command is read-only using the
`readOnly()` property.

The key/value store is implemented in a subclass of  `Application` called `KVStore`. You can get `KVStore` by downloading `kvstore.tgz` from the Files page on Canvas and running this command in the handout directory (`dslabs`):

    $ tar xzmf kvstore.tgz

Notice the `m` option is needed to update the modification time so that `make` will recompile KVStore.java.
The implementation of `KVStore` isn't on github because it is a
solution to a lab assignment in the original version of DSLabs, but you're getting it for free!

## Part 1

In part 1, we will use a strategy called "chain replication" to
maintain a consistent ordering of operations on a shared database.

Each client has it's own local server. Server 0 is the "primary" replica. All servers forward update requests from clients to
the primary. After performing an update (`Append` or `Put`) each server passes
the request on to the next host in the chain. On the other hand, to
distribute the load and reduce communication time, read-only queries
(`Get`) are always performed on the client's local server.

The server is implemented in this file in the handout:

    labs/lab1-sharedobject/src/dslabs/sharedobject/ChainRepAppendServer.java

Fill in the "TODO" regions in this file to complete the
program. Our implementation required seven additional lines of code.

To test your implementation, use this command (in the main `dslabs`
directory:

   $ ./run-tests.py --lab 1 --part 1

This will check that:

- Your server is live (i.e., it completes all client requests, assuming a reliable ordered network)
- It satisfies eventual consistency
- `Get` operations are not forwarded
- The servers don't return more than one `Reply` for a given command.

Questions to think about:

- What determines the update order in this algorithm?
- When is it safe to reply to an append request? 
- Does this algorithm have disadvantages from a user point of view?
- Suppose the network is temporarily partitioned? Can users still work?
- What assumptions on the network are needed? Why?

## Part 2

In part 2, we will use scalar clocks to maintain eventual
consistency. This time, we will assume that the server has to handle only `Get` and `Put` commands, and not `Append` commands.

Each client should perform all operations on its own local server. All
messages sent by the servers should contain a scalar clock (the
clients don't need to maintain clocks). Use the scalar clocks, the ordered
property of the network and the fact that you only have to satisfy
*eventual* consistency to guarantee that each server sends only `2N`
messages per request, where `N` s the number of servers. In
particular, you should be able to perform all updates locally without
waiting on messages from any other servers.

Fill in the "TODO" regions in `ScalarClockPutServer.java` to complete the
program. To test it, use this command:

   $ ./run-tests.py --lab 1 --part 2

Question to think about:
- In what way is part 2 an improvement on part 1?
- Suppose the network is temporarily partitioned? Can users still work?

## Part 3

In part 3, we will use scalar clocks to maintain eventual
consistency for all the `KVStore` commands including `Append`. The
difference from part 2 is that now the final value depends on *all* of
the appends performed (whereas `Put` only, the *last* `Put`
determines the value of a key).

Use the scalar clocks to make sure that the Append operations are
performed in the same order on all of the servers without forwarding
all requests to the primary as in part 1. To make the implementation
of this a little simpler, you may assume there are exactly two
servers. Hint: this is very similar to Lamport's mutual exclusion
algorithm. You may find that the server needs to keep a queue of
incoming append operations sent by the other server, along with their
logical times.


Fill in the "TODO" regions in `ScalarClockAppendServer.java` to complete
the program. To test it, use this command:

   $ ./run-tests.py --lab 1 --part 3

Question to think about: Does part 3 have a higher message complexity
than part 2? Notice how the performance of replication can depend on
properties of the update commands supported by the application.






