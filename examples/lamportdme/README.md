Lamport's DME
-------------

This is an implementation of Lamport's DME algorithm using scalar
clocks.

You can visualize it like this:

    $ ./run-tests.py --example lamportdme --debug 3 3 a,r,a,r

Two things about this command line: (1) you have to use 3 servers and
three clients (this is built in to the tester) and (2) the command
names don't matter (the user can only alternate between Acquire and
Release). This command line allows you to animate three client/server
pairs, where each client does up to two Acquires and Releases.

Notice that you can cause the messages to be received in any order
you like, even out-of-order. Try this scenario that shows two clients
receiving AcquireReply at the same time:

1) Server 1 broadcasts a Request
2) Server 2 broadcasts a request
3) The Reply from Server 1 to Server 2 is delivered *before* the Request.
4) Server 2 gets all its Replies and sends AcquireReply (why?)
5) Server 1 gets all its Replies and sends AcquireReply (why?)


