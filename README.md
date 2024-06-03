
### rate limiter

##### Background:

> prevent Dos(Denial of Service) attack -> resource lack
>
> reduce cost. Less duplicate req -> less server number -> more resource for high priority api.
>
> prevent server overloaded.

##### scenario              

1. **client-side or server-side.**    -> server-side(client is unreliable) 

2. **limit by IP/user_id/other properties.**    -> should be flexible, consider all of them

3. **scale: for startup or big company** -> big company, able to handle large num request

4. **in distributed system**

5. **a separate service or integrated in application** -> a separate service

6. **let user know his req is limited**.

7. rate limiter should be:

   > Accurate rate limiting
   >
   > use as little memory as possible, 
   >
   > Low latency. rate limiter not slow down response time.
   >
   > rate limiter is shared across multiple servers 
   >
   >  High fault tolerance. Rate limiter problem won't affect the entire system.
   >
   > has Exception handling

##### service                       

1. Client -> api gateway -> api servers

2. api gateway : rate limiting, SSL termination, authentication, IP whitelisting, servicing static content

   > * self created rate limiter: when u have special rate limiting rule -> full control of algorithm
   > * 3rd party rate limiter: when no enough developing resource
   >

3. Token bucket

   1. rule:

      > a fixed size bucket
      >
      > Tokens are put in the bucket at a preset rate
      >
      > 1 req take 1 token
      >

   2. buckets number = api num * user nums + 1 global bucket

      * different apis -> different buckets
      * different limit properties(like user_id) -> different buckets
      * a global bucket to control all access

   3. Pro: 

      > easy implement, 
      >
      > memory efficient(only maintain little variable): last refill time, current token num
      >
      > can handle a burst of traffic as long as has token

      Cons: hard to adjust **bucket size** and **refill rate**

4. Leaking bucket

   1. Rule:

      > a fixed size queue
      >
      > Request in queue will be processed at a fixed rate
      >

   2. Pro: 

      > memory efficient: maintain a queue
      >
      > Stable outflow rate to process request, (like database write, a burst of writes will affect performance)
      
      Cons: can't handle burst of traffic, hard to adjust **queue size** and **outflow rate**

5. Fixed window counter

   1. Rule:

      > fix-sized time windows with different counters

   2. Pro:

      > memory efficient: start time of next window, current counter
      >
      > for use case to reset something at start/end of window, like user login

      Cons: burst traffic at the edges of a window -> extra reqs passed

6. Sliding window log

   1. Rule:

      > sliding time window with fixed size
      >
      > a cache(Redis) to keep request timestamps
      >
      > abandon req if req num of [cur timestamp - window size, cur timestamp) > fixed size

   2. Pro: accurate rate limiting for any sliding time window

      Cons: not memory efficient, keep all req(passed & rejected) timestamps

7. Sliding window counter
8. Diagram of Sliding window log algorithm

![Screenshot 2024-06-01 at 2.09.05 PM](/Users/isaac/Desktop/Screenshot 2024-06-01 at 2.09.05 PM.png)

##### Dive deep

1. request record in Redis: event(e.g. Login) + feature(e.g. ip address/user_id) + timestamp + count

2. Rules are generally written in configuration files and saved on disk. a separate process periodically pull rules from dist to memory

3. rate limited -> APIs return a **429** HTTP response -> may keep them is a queue to process later

4. Keep info in 429 **response header**:

   > X-Ratelimit-Remaining: remaining number of allowed req within the window. 
   >
   > X-Ratelimit-Limit: num of calls the client can make per time window.
   >
   > X-Ratelimit-Retry-After: The number of seconds to wait until you can make a request again

4. 

![Screenshot 2024-06-01 at 2.33.17 PM](/Users/isaac/Desktop/Screenshot 2024-06-01 at 2.33.17 PM.png)

5. in distributed system

   1. Consider: Race condition, Synchronization issue
   2. highly concurrent environment -> data race of req num within window(from Redis) -> solution: Locks -> slow down the system. 
   3. mutiple rate limiters -> reqA may be limited by limiterA but not limited by limiterB -> solution: centralized data stores like Redis.

6. optimize performance:

   1. **multi-data center** setup -> reduce latency
   2. synchronize data of multi-data center with an **eventual consistency model???** 

7. Monitor

   1. if algorithm is effective?

      > if current algorithm to restrictive -> many valid req are abandoned -> loose algorithm

   2. if the rules are effective?

      > if current rule can't handle burst traffic -> change rule temporarily, use token bucket

##### others

1. Rate limiting at different levels. 

   >  limit by account ID -> application layer(Http layer)
   >
   > limit by IP -> IP layer

2. Add retry logic at client.
3. client cache ->  avoid making frequent API calls.





### Consistent Hashing

##### Background

1. Horizontal scaling -> distribute requests/data to different server/DB

2. serverindex = hash(key) % N, N is size of server pool

3. Problem: when (new servers are added/existing servers are removed) -> most keys are redistributed.

4. solution: consistent hashing -> only k/n keys need to be remapped on average, 

   > k is the number of keys, n is the number of slots/servers.

5. consistent hashing: no % operation

##### Hash ring 

1. put server on the ring with server's hash code

2. Request/data -> hash code -> put on the ring -> going clockwise look up its server/DB

   > use a TreeMap(balanced BST) to store server's position, O(logN) to look up

3. 

   ![Screenshot 2024-06-01 at 9.20.18 PM](/Users/isaac/Desktop/Screenshot 2024-06-01 at 9.20.18 PM.png)

4. add a server

   ![Screenshot 2024-06-01 at 9.22.26 PM](/Users/isaac/Desktop/Screenshot 2024-06-01 at 9.22.26 PM.png)



5. remove a server

   ![Screenshot 2024-06-01 at 9.23.29 PM](/Users/isaac/Desktop/Screenshot 2024-06-01 at 9.23.29 PM.png)

6. problems
   1. it is impossible to keep the same size of partitions on the ring for al servers 
   2. it is possible to have a uneven key distribution on the ring.
   3. solutions: virtual nodes or replicas

##### Virtual nodes

1. 1 real server has multiple virtual nodes randomly distributed on the ring

2. Request/data -> hash code -> put on the ring -> going clockwise look up the first virtual node

   ![Screenshot 2024-06-01 at 9.38.33 PM](/Users/isaac/Desktop/Screenshot 2024-06-01 at 9.38.33 PM.png)

##### advantages

1. Automatic scaling: servers could be added and removed automatically depending on the load.
2. Heterogeneity: servers with higher capacity ->  more virtual nodes.





### A Key-value Store

##### background

1. Keys can be plain text("last_logged_in_at") or hashed values(253DDEC4).

   value can be strings, lists, objects, etc.

3.  put(key, value) // insert “value” associated with “key”

   get(key) // get “value” 

##### scenario

1. The size of a key-value pair is small: less than 10 KB.
2. Able to store big data.
3. High availability: The system responds quickly, even during failures.
4. High scalability: The system can be scaled to support large data set.
5. Automatic scaling: The addition/deletion of servers should be automatic based on traffic.
6. Tunable consistency.
7. Low latency.

##### service

1. Single server key-value store

   1. store key-value pairs in a hash table in memory.

   2. Pros: memory access is fast

      Cons: 1 memory space is small -> impossible to put all data

   3. Solution: 

      > Data compression
      >
      > Store only frequently used data in memory and the rest on disk
      >
      > Eventually needs **Distributed key-value store**

2. CAP (consistency, availability, and partition tolerance)

   > Consistency: all clients see the same data at the same time no matter which node they connect
   >
   > to.
   >
   > Availability: any client requests gets a response even if some of the nodes are down.
   >
   > Partition Tolerance: the system continues to operate despite network break between two nodes.

	* distrobuted system must be partition tolerance -> CA system not exist in real world
	
	* CP system: if nodeA is down -> must block all write operations to other nodes -> avoid data inconsistency 
	
	* AP system: if nodeA is down -> other nodes keep accepting read & writes -> data will be synced to nodeA when the network partition is resolved.

3. Data partition -> consistent hashing

4. Data replication -> high availability and reliability

   1. data walk clockwise on the hash ring from its position-> choose the first N unique servers on the ring to store data copies.

   2. attributes to adjust for high availability(latency) and consistency

      > N: The number of replicas
      >
      > W: a write operation to be considered as successful if it's acknowledged from >= W replicas.
      >
      > R:  a read operation to be considered as successful if it gets responses from >= R replicas.

      * W =N or R = 1: for fast read system
      * W = 1 or R = N: for fast write system
      * W + R > N: strong consistency guaranteed

5. Consistency model
   1. strong consistency: any read operation returns the latest updated data. A client never sees out-of-date data.
   2. Weak consistency: read operations may not see the most updated value.
   3. **Eventual consistency**: this is a specific form of weak consistency. Given enough time, all updates are propagated, and all replicas are consistent.

6. Inconsistency solution: versioning
   1. vector clock is a [server, version] pair associated with a data item.
   2. a vector clock is represented by Data1([S1, v1], [S2, v2], ., [Sn, vn])

7. Handling failures

   1. gossip protocol:
      - Each node maintains a node membership list including itself, which in formation (member ID : heartbeat counter).
      - Each node periodically increments its own heartbeat counter.
      - Each node periodically sends its membership list to a set of random nodes.
      - Once nodes receive heartbeats list, membership list is updated to the latest info.
      - If the heartbeat has not increased for more than predefined periods, the member is considered as offline.
   2. the system chooses the first W **healthy** servers for writes and first R **healthy** servers for reads on the hash ring. Offline servers are ignored.

8. diagram

   ![Screenshot 2024-06-02 at 1.54.47 PM](/Users/isaac/Desktop/Screenshot 2024-06-02 at 1.54.47 PM.png)

   * A coordinator is a node that acts as a proxy between the client and the key-value store.

   * Nodes are distributed on a ring using consistent hashing.

   * The system is completely decentralized so adding and moving nodes can be automatic.

   * Data is replicated at multiple nodes.

   * There is no single point of failure

##### others

1. write path

   ![Screenshot 2024-06-02 at 1.58.58 PM](/Users/isaac/Desktop/Screenshot 2024-06-02 at 1.58.58 PM.png)

2. Read path

   ![Screenshot 2024-06-02 at 1.59.48 PM](/Users/isaac/Desktop/Screenshot 2024-06-02 at 1.59.48 PM.png)





### Unique ID Generator In Distributed Systems

##### scenario

1. What are the characteristics of unique IDs    ->      must be unique and sortable.

2. ID increment by 1? -> The ID increments by time but not necessarily only increments by 1. 

   ID created in the evening > ID created in the morning on the same day.

3. ID only contains numerical values? -> Yes

4. ID length requirement -> IDs should fit into 64-bit.

5. the scale of the system?  ->  should be able to generate 10,000 IDs per second.

##### service

1. Multi-master replication

   > uses the databases’ auto_increment feature. 
   >
   > DB increase ID by k. k is the number of databases in use. 

   ![Screenshot 2024-06-02 at 7.26.37 PM](/Users/isaac/Desktop/Screenshot 2024-06-02 at 7.26.37 PM.png)

   * Shortage:
     * Hard to scale with multiple data centers -> more DB are needed for more servers.
     * IDs do not go up with time across multiple servers.
     * does not scale well when a DB is added/removed. -> add/remove BD to 1 server -> k need to change

2. UUID

   1. a 128-bit number. like 09c9-3e62-50b4-468d-bf8a-c07e-1040-bfb2 (hexadecimal/base 32)

   2. very low probability of getting collusion -> can be generated independently without sync up between servers.

   3. Pros:

      1. each generator is independent -> no need synchronization
      2. easy to scale -> 1 generator with 1 web server.

      Cons:

      1. IDs are 128 bits long, but our requirement is 64 bits.
      2. IDs do not go up with time.
      3. IDs could be non-numeric.

3. Ticket Server(check more reference)

   1. use a centralized auto_increment feature in a single database server (Ticket Server).

   2. pros

      > Numeric IDs.
      > easy to implement, and itworks for small to medium-scale applications.

      Cons: Single point of failure. ticket server goes down -> affect all servers. 

      -> solution: set up multiple ticket servers.   ->  introduce new challenges such as data synchronization.

4. **Twitter snowflake approach** (Twitter recommended)

   1. ID format

      ![Screenshot 2024-06-02 at 7.48.45 PM](/Users/isaac/Desktop/Screenshot 2024-06-02 at 7.48.45 PM.png)

      - Sign bit: 1 bit. It will always be 0. -> reserved for future uses -> can be used to distinguish between signed and unsigned numbers.
      - Timestamp: 41 bits. Milliseconds since the epoch. Like 1,288,834,974,657 == Nov 04, 2010, 01:42:54 UTC. -> can be sorted by time
      - Datacenter ID: 5 bits, provides 2**5 = 32 data centers.
      - Machine ID: 5 bits, provides 2**5 = 32 machines per datacenter.
      - Sequence number: 12 bits. every ID generated on that DB, the sequence number is incremented by 1.reset to 0 every millisecond.



### URL Shortener

##### scenario

1. Can you give an example of how a URL shortener work?

   https://www.systeminterview.com/g=chatsystem&c=loggedin&v=v3&l=long -> https://tinyurl.com/y7keocwj. click shorten url -> redirect to its long url

2. traffic volume? -> 100 million URLs are generated per day.
3. Length of the shortened URL? -> As short as possible.
4. characters are allowed in the shortened URL? -> numbers (0-9) and characters (a-z, A-Z).
5. shortened URLs be deleted or updated? -> no

6. summary

   > write operation: 100 million URLs are generated per day.
   >
   > Write QPS: 100 million / 24 /3600 = 1160
   >
   > Assuming read operation 10 times of write operation, read QPS: 11600
   >
   > Assuming the URL shortener service will run for 10 years -> support 100 million * 365 * 10 = 165 billion records.
   >
   > Assume average URL length is 100bytes -> Storage requirement 365 billion * 100 bytes = 36.5 TB

##### service

1. api

   1. URL shortening:

      POST api/vi/data/shorten

      request body: {longUrl: longURLString}

      return shortURL

   2. URL redirecting.:

      GET api/vi/shortUrl

      Return longURL for HTTP redirection

2. redirection diagram

   ![Screenshot 2024-06-02 at 8.16.14 PM](/Users/isaac/Desktop/Screenshot 2024-06-02 at 8.16.14 PM.png)

   1. 301 status: permanently redirected -> the browser caches the response

      subsequent requests for the same URL not -> URL shortening service.

      subsequent requests are redirected to the long URL server directly.

   2. 302 status: temporarily redirected

      subsequent requests for the same URL ->  URL shortening service first -> redirected to the long URL server.

   3. reduce the server load -> 301

      to analysis -> track click rate and source of the click -> 302

3. service can be implemented by a hash table<shortURL/hash value, longURL> 

   * hash function must satisfy the following requirements:

     > Each longURL must be hashed to one hashValue.
     >
     > Each hashValue can be mapped back to the longURL.

##### Dive deep

1. memory resource is limited -> store hash map in a relational database.(id | short url/hash value | long url)

2. Hash value length n -> should satisfy base 62( = 0-9 + a-z + A-Z) ** n >= 3650 billion -> n = 7

3. Base 62 conversion: for each longUrl, generator a unique ID with unique ID generator -> convert ID to base 62 as its short url/hash value

   ![Screenshot 2024-06-02 at 9.40.40 PM](/Users/isaac/Desktop/Screenshot 2024-06-02 at 9.40.40 PM.png)

4. use distributed unique ID generator 

5. more read than writes -> cache to improve performance.

   ![Screenshot 2024-06-02 at 9.55.43 PM](/Users/isaac/Desktop/Screenshot 2024-06-02 at 9.55.43 PM.png)

##### others

1. Rate limiter: A potential security problem is that malicious users send an overwhelmingly large

   number of URL shortening requests. -> filter by ip address

2. Database replication and sharding 
