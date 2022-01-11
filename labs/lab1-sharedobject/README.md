# Lab 1

In this lab, we will performs operations on a shared object. That is,
two or more users who are far apart want to edit a shared
document. All of the users would like to see the results of their own
updates right away, despite delays in the network. It's OK for users
to temporarily see inconsistent states of the document, owing to
network delay, so long as the inconsistencies are fairly quickly
corrected.

Our specification is as follows:

- Assumption: Ordered, reliable network channels (for example using TCP)
- Guarantee: Eventual consistency

## Part 1


In part 1, we will use a strategy called "chain replication" to
maintain a consistent ordering of operations on a shared text file.

Our application interface has the following commands:

- `Append(s)`: append string s to the file, returning void
- `Show()`: return the contents of the file

Server 0 is the "primary" replica. Clients send all update requests to
this server. After performing an update (`Append`) each server passes
the request on to the next host in the chain. On the other hand, to
distribute the load and reduce communication time, read-only queries
(`Show`) are performed on the client's local server.


Fill in the "TODO" regions in `ChainRepAppend.java` to complete the
program.

To test the implementation, use this command (in the main `dslabs`
directory:

   $ ./run-tests --lab 1 --part 1


Questions to think about:

- What determines the update order in this algorithm?
- When is it safe to reply to an append request? 
- Does this algorithm have disadvantages from a user point of view? 
- What assumptions on the network are needed? Why?

## Part 2

In part 2, we will use scalar clocks to maintain eventual
consistency. This time, our application is a key-value store (in other
words, a map from keys to values). Our application commands are:

- `Get(k)`: returns the value associated with key `k`, or `null` if
  there is no such key in the map.
- `Put(k,v)`: set the value of key `k` to value `v`.

Each client should perform all operations on its own local server. All
messages sent by the servers should contain a scalar clock (the
clients don't need to maintain clocks). Use scalar clocks, the ordered
property of the network and the fact that you only have to satisfy
*eventual* consistency to guarantee that each server sends only `2N`
messages per request, where `N` s the number of servers. In
particular, you should be able to perform all updates locally without
waiting on messages from any other servers.

Fill in the "TODO" regions in `ScalarClockKV.java` to complete the
program. To test it, use this command:

   $ ./run-tests --lab 1 --part 2

Question to think about:
- In what way is part 2 an improvement on part 1?

## Part 3

In part 3, we will use scalar clocks to maintain eventual
consistency for the Append/Show application from part 1. The
difference from part 2 is that now the final value depends on *all* of
the appends performed (whereas in the k/v store, only the *last* put
for a given key matters).

Use the scalar clocks to make sure that the Append operations are
performed in the same order on all of the servers. To make the
implementation of this a little simpler, you may assume there are
exactly two servers. Hint: this is very similar to Lamport's mutual
exclusion algorithm. You may find that the server needs to keep a
queue of incoming append operations sent by the other server, along
with their logical times.


Fill in the "TODO" regions in `ScalarClockAppend.java` to complete
the program. To test it, use this command:

   $ ./run-tests --lab 1 --part 3

Question to think about: Does part 3 have a higher message complexity
than part 2? Notice how the performance of replication can depend on
properties of the application.






