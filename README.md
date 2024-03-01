The goal is to design and implement a simple application layer protocol over TCP to
facilitate “Distributed Computing” (DC) and deploy a cloud data cluster. In AWS’s language, the
distributed computing is a method of making multiple computers work together to solve a larger
problem. This large problem could be computing intensive or storage intensive or a mix of both.

One way to implement DC is using a client server architecture. In this setup, assuming you have
n+1 nodes, one node acts as a server {S} and (n) nodes acts as clients {C1, C2, …Cn}. Each test
run involves a Job {J} and a computing program {P}. Assume that each Ci is preloaded with P.
When a J is submitted to S, S breaks J into smaller (approximately equal) sub-jobs {J1, J2, …Jm}
where m >= n. After this step, S distributes each Jk to a distinct Ci in such a way that the load
balancing is preserved. Following this, each Ci runs P on their respective Jk, yielding a result Ri.
Ci then sends their respective Ri back to the S. Finally, S accumulates all the Ri {R1, R2, R3…Rn}
to yield the final result R. Remember that J by definition is going to be very very large that R {J

Setup: (Assuming 3 members per team)
1. Your cluster needs at least 6 total nodes (One server + Five Clients). You may have 6
nodes by running 2 VMs on each laptop.
2. You will be provided with a sample J and P (in Java) in advance, it is left to your project
design on how you use or incorporate P into your implementation. If you choose to use
any other language, then it is up to you to translate P.
3. You must use TCP protocol for your network implementation. Here is a good tutorial:
https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
4. During the final demo, you will be provided with a test J and your project will be
evaluated on the basis of clarity of design, team dynamics, correctness of cluster
implementation and accuracy of your final results.
5. You will submit a project report and a zip of your entire source code. Your report must
contain the design of your server and client, your testing logs, and instructions to
install/run your code.
