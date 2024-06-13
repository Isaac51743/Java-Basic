### rate limiter

##### Background:

> prevent Dos(Denial of Service) attack -> resource lack
>
> reduce cost. Less duplicate req -> less server number -> more resource for high priority api.
>
> prevent server overloaded.

##### scenario              

1. **client-side or server-side?**    -> server-side(client is unreliable) 

2. **limit by IP/user_id/other properties?***    -> should be flexible, consider all of them

3. **scale: for startup or big company?** -> big company, able to handle large num request

4. **in distributed system?**

5. **a separate service or integrated in application?** -> a separate service

6. **let user know his req is limited?**.

7. rate limiter should be:

   > Accurate rate limiting -> limit request at any timeframe
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

   2. buckets number = api num * user nums + global bucket num

      * different apis -> different buckets
      * different limit properties(like user_id) -> different buckets
      * a global bucket to control all access

   3. Pro: 

      > easy implement, 
      >
      > memory efficient(only maintain little variable): last refill time, current token num (for each bucket)
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
      > Stable outflow rate to process request, (like database write, a burst of writes will affect DB performance)
      
      Cons: 
      
      > can't handle burst of traffic(limited by process rate), 
      >
      > hard to adjust **queue size** and **outflow rate**

5. Fixed window counter

   1. Rule:

      > fix-sized time windows with different counters

   2. Pro:

      > memory efficient: start time of next window, current counter
      >
      > for use case to reset something at start/end of window, like user login

      Cons: burst traffic at the edges of a window -> extra reqs passed

6. **Sliding window log**

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

![Screenshot 2024-06-01 at 2.09.05 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-01 at 2.09.05 PM.png)

##### Dive deep

1. request record in Redis: 

   â	event(e.g. "Login" or api path) + feature(e.g. ip address/user_id) + timestamp : count

2. Rules are generally written in configuration files and saved on disk. a separate process periodically pull rules from dist to memory

3. rate limited -> APIs return a **429** HTTP response -> may keep them is a queue to process later(retry)

4. Keep info in 429 **response header**:

   > X-Ratelimit-Remaining: remaining number of allowed req within the window. 
   >
   > X-Ratelimit-Limit: num of calls the client can make per time window.
   >
   > X-Ratelimit-Retry-After: The number of seconds to wait until you can make a request again

5. Diagram

![Screenshot 2024-06-01 at 2.33.17 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-01 at 2.33.17 PM.png)

6. in distributed system

   1. Consider: Race condition, Synchronization issue

   1. highly concurrent environment -> data race of req num within window(from Redis) -> solution: Locks -> slow down the system -> Redis sorted list, search O(logN). 

   3. mutiple rate limiters -> reqA may be limited by limiterA but not limited by limiterB -> solution: centralized data stores like Redis.

      ![Screenshot 2024-06-03 at 8.51.11 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-03 at 8.51.11 PM.png)

7. optimize performance:
   1. **multi-data center** setup(rather than use 1 centralized redis) -> traffic is automatically routed to the closest edge server -> closest data center(redis) ->  reduce latency
   2. synchronize data of multi-data center with an **eventual consistency model???** 

8. Monitor

   1. if algorithm is effective?

      > if current algorithm can't handle burst traffic ->  change algorithm temporarily, use token bucket

   2. if the rules are effective?

      > if current rule too restrictive  ->many valid req are abandoned -> loose algorithm

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

   ![Screenshot 2024-06-01 at 9.20.18 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-01 at 9.20.18 PM.png)

4. add a server

   ![Screenshot 2024-06-01 at 9.22.26 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-01 at 9.22.26 PM.png)



5. remove a server

   ![Screenshot 2024-06-01 at 9.23.29 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-01 at 9.23.29 PM.png)

6. problems
   1. it is impossible to keep the same size of partitions on the ring for al servers 
   2. it is possible to have a uneven key distribution on the ring.
   3. solutions: virtual nodes or replicas

##### Virtual nodes

1. 1 real server has multiple virtual nodes randomly distributed on the ring

2. Request/data -> hash code -> put on the ring -> going clockwise look up the first virtual node

   ![Screenshot 2024-06-01 at 9.38.33 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-01 at 9.38.33 PM.png)

##### advantages

1. Automatic scaling: servers could be added and removed automatically depending on the average load.
2. Heterogeneity: servers with higher capacity ->  more virtual nodes.





### A Key-value Store

##### background

1. Keys can be plain text("last_logged_in_at") or hashed values(253DDEC4).

   value can be strings, lists, objects, etc.

3.  put(key, value) // insert âvalueâ associated with âkeyâ

   get(key) // get âvalueâ 

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

   1. data walk clockwise on the hash ring from its position-> choose the first N unique servers/DB on the ring to store data copies.

   2. attributes to adjust for high availability(latency) and consistency

      > N: The number of replicas
      >
      > W: a write operation to be considered as successful if it's acknowledged from >= W replicas.
      >
      > R:  a read operation to be considered as successful if it gets responses from >= R replicas.

      * W =N or R = 1: for fast read system
      
      * W = 1 or R = N: for fast write system
      
      * W + R > N: strong consistency guaranteed -> at least one overlapping node that has the
      
        latest data

5. Consistency model
   1. strong consistency: any read operation returns the latest updated data. A client never sees out-of-date data.
   2. Weak consistency: read operations may not see the most updated value.
   3. **Eventual consistency**: this is a specific form of weak consistency. Given enough time, all updates are propagated, and all replicas are consistent.

6. Inconsistency solution to reach Eventual consistency: versioning
   1. vector clock is [server, version] pairs associated with a data item.
   
   2. a vector clock is represented by Data1([S1, v1], [S2, v2], ., [Sn, vn])
   
   3. once a server update a data item, it will send (updated data item + vertor clock) to other servers which has this data item. -> other server can ignore/update/merge this message
   
   4. Exp: 
   
      > Server1 update Data1 -> Data1([S1, 1], [S2, 0], ., [Sn, 0])
      >
      > Server1 sync up with other servers which has replica of Data1
      >
      > Server2 get sync up message 
      >
      > â	if Server2: all versions in vector clock < all versions in sync up message -> update to Data1([S1, 1], [S2, 0], ., [Sn, 0])  -> make [S2, 1]. ->  sent sync up message to other servers
      >
      > â	if Server2 has Data2([S1, 1], [S2, 1], ., [Sn, 0]) -> ignore
      >
      > â	if Server2 has Data2([S1, 0], [S2, 1], ., [Sn, 0]) -> merge by their timestamps
   
7. Handling failures

   1. gossip protocol:
      - Each node maintains a node membership list including itself, which in format (member ID : heartbeat counter).
      - Each node periodically increments its own heartbeat counter.
      - Each node periodically sends its membership list to a set of random nodes.
      - Once nodes receive heartbeats list, membership list is updated to the latest info.
      - If the heartbeat has not increased for more than predefined periods, the member is considered as offline.
   2. the system chooses the first W **healthy** servers for writes and first R **healthy** servers for reads on the hash ring. Offline servers are ignored.

8. diagram

   ![Screenshot 2024-06-02 at 1.54.47 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-02 at 1.54.47 PM.png)

   * A coordinator is a node that acts as a proxy between the client and the key-value store.

   * Nodes are distributed on a ring using consistent hashing.

   * The system is completely decentralized so adding and moving nodes can be automatic.

   * Data is replicated at multiple nodes.

   * There is no single point of failure

##### others

1. write path

   ![Screenshot 2024-06-02 at 1.58.58 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-02 at 1.58.58 PM.png)

2. Read path

   ![Screenshot 2024-06-02 at 1.59.48 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-02 at 1.59.48 PM.png)





### Unique ID Generator

##### scenario

1. What are the characteristics of unique IDs    ->      must be unique and sortable.

2. ID increment by 1? -> The ID increments by time but not necessarily only increments by 1. 

   ID created in the evening > ID created in the morning on the same day.

3. ID only contains numerical values? -> Yes

4. ID length requirement -> IDs should fit into 64-bit.

5. the scale of the system?  ->  should be able to generate 10,000 IDs per second.

##### service

1. Multi-master replication

   > uses the databasesâ auto_increment feature. 
   >
   > DB increase ID by k. k is the number of databases in use. 

   ![Screenshot 2024-06-02 at 7.26.37 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-02 at 7.26.37 PM.png)

   * Shortage:
     * Hard to scale with multiple data centers -> add/remove a new data center needs to reset all data centers(reset k) -> unique id doesn't contain any data center specific info.
     * IDs do not go up with time across multiple servers.
     * does not scale well when a server/DB is added/removed  -> k need to change (like server is down, or adding new server/DB to improve performance)

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
      > easy to implement, and it works for small to medium-scale applications.

      Cons: Single point of failure. ticket server goes down -> affect all servers. 

      -> solution: set up multiple ticket servers.   ->  introduce new challenges such as data synchronization.

4. **Twitter snowflake approach** (Twitter recommended)

   1. ID format

      ![Screenshot 2024-06-02 at 7.48.45 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-02 at 7.48.45 PM.png)

      - Sign bit: 1 bit. It will always be 0. -> reserved for future uses -> can be used to distinguish between signed and unsigned numbers.
      - Timestamp: 41 bits. Milliseconds since the epoch. Like 1,288,834,974,657 == Nov 04, 2010, 01:42:54 UTC. -> can be sorted by time
      - Datacenter ID: 5 bits, provides 2**5 = 32 data centers.
      - Machine ID: 5 bits, provides 2**5 = 32 machines per datacenter.
      - Sequence number: 12 bits. every ID generated on that server, the sequence number is incremented by 1.reset to 0 every millisecond.



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
   > Assuming the URL shortener service will run for 10 years -> support 100 million * 365 * 10 = 365 billion records.
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

   ![Screenshot 2024-06-02 at 8.16.14 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-02 at 8.16.14 PM.png)

   1. 301 status: permanently redirected -> the browser caches the response

      subsequent requests for the same URL **not ->** URL shortening service.

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

1. memory resource is limited -> store hash map in a relational database.**(id | short url/hash value | long url)**

2. Hash value length n -> should satisfy base 62( = 0-9 + a-z + A-Z) ** n >= 3650 billion -> n = 7

3. Base 62 conversion: for each longUrl, generator a unique ID with **unique ID generator** -> convert ID to base 62 as its short url/hash value

   ![Screenshot 2024-06-02 at 9.40.40 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-02 at 9.40.40 PM.png)

4. use distributed unique ID generator 

5. more read than writes -> cache to improve performance.

   ![Screenshot 2024-06-02 at 9.55.43 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-02 at 9.55.43 PM.png)

##### others

1. Rate limiter: A potential security problem is that malicious users send an overwhelmingly large

   number of URL shortening requests. -> filter by ip address

2. Database replication and sharding 





### Web Crawler

##### background

1. Purpose:

   > Search engine indexing: for search engines like Google
   >
   > Web archiving: online library
   >
   > Web mining: explore useful knowledge
   >
   > Web monitoring: monitor copyright or other illegal web content.

##### scenario

1. Steps:
   1. Given a set of URLs, download al the web pages addressed by the URLs.
   2. Extract URLs from these web pages
   3. Add new URLs to the list of URLs to be downloaded. Repeat these 3 steps.

2. questions

   1. purpose of the crawler? for search engine indexing, data mining, or something else? -> Search engine indexing.

   2. How many web pages does the web crawler collect per month?  -> 1 billion pages.

   3. content types included? HTML only or PDFs or images?  -> HTML only.

   4. consider newly added or edited web pages?   ->  Yes

   5. need to store HTML pages crawled from the web?   ->  Yes, up to 5 years

   6. handle web pages with duplicate content?  ->   should be ignored.

   7. summary

      > Scalability: The web has billions of web pages -> crawler should be extremely efficient using parallelization.
      >
      > Robustness: able to handle traps. (Bad HTML, unresponsive servers, crashes, malicious links)
      >
      > Politeness: not make too many requests to a website within a short time interval.
      >
      > Extensibility: The system is flexible -> minimal changes are needed to support crawling new content types.

      > crawling QPS = 1,000,000,000 / 30 days / 24 hours / 3600 seconds = ~400 pages per second.
      >
      > -> Peak QPS = 2 *QPS = 800
      >
      > Assume the average web page size is 500k.
      >
      > -> 1 billion * 500k = 500 TB storage per month.
      >
      > -> 500 TB * 12 months * 5 years = 30 PB. A 30 PB storage is needed to store five-year content.

##### service

![Screenshot 2024-06-03 at 9.27.35 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-03 at 9.27.35 PM.png)

1. Seed URLs

   strategy:

   1. based on location: e.g. each country pick n popular urls
   2. based on topics: shopping, sports and healthcare, each topic pick n popular urls

2. URL Frontier: the queue to traverse web pages

3. HTML downloader: downloads web pages from the internet.

4. content parser: parse and validate page content

5. Content Seen?  compare the hash values of the two web page contents

6. Content Storage: choose different DB considering data type, data size, access frequency, etc

   > Most of the content is stored on disk because the data set is too big to fit in memory. 
   >
   > Popular content's hash code is kept in memory to reduce latency.

7.  URL filter: excludes certain url types, like file extensions, error links and URLs in blacklist.

8. URL Seen? filtered urls visited before or already in the Frontier.

9. URL Storage: put url to URL storage when it's about to add to Frontier.

##### Dive deep

1. URL frontier

   1. DFS or BFS?

      1. DFS is usually not a good choice because the depth of DFS can be very deep.

      2. BFS problems 

         > Most links from the same web page are linked back to the same host. -> crawl in parallel -> that host will be flooded by our crawling requests. 

         > not take the priority of a URL into consideration. -> prioritize URLs according to their page ranks, web traffic, update frequency(from third party app)

   2. Politeness

      > 1 host -> 1 thread -> 1 queue
      >
      > Mapping table: <host name : queue>
      >
      > Queue selector: hold a <queue : thread> map, terminate thread and remove entry in <queue : thread> map if its queue is empty for a predefined time period
      >
      > interval can be added between urls in the same queue

      ![Screenshot 2024-06-03 at 10.42.26 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-03 at 10.42.26 PM.png)

   3. Priority

      > Prioritizer: It takes URLs as input and computes the priorities.
      >
      > Method1: Queue f1 to fn: Each queue has an assigned priority. Queues with high priority are selected by Queue selector with higher probability.
      >
      > Method2: use a priority queue

      ![Screenshot 2024-06-04 at 8.06.06 AM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-04 at 8.06.06 AM.png)

   4. refresh crawled page

      set a scheduler

      > strategy1: Recrawl based on web pagesâ update history.
      >
      > strategy2: Prioritize URLs and recrawl important pages first and more frequently.

   5. Buffer for URL Frontier

      urls num exponentially increased -> memory space not enough -> most URLs are stored on disk, maintain buffers in memory for enqueue/dequeue operations.

2. HTML downloader

   1. Robots.txt: specifies which pages can be crawled and which are not allowed

      > Scheduler: periodically load and update Robots.txt to memory

   2. Distributed crawl

      ![Screenshot 2024-06-04 at 9.24.12 AM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-04 at 9.24.12 AM.png)

   3. cache for DNS

      > most DNS api is synchronous.  -> 1 thread call DNS will block other threads
      >
      > DNS api response time is around 10ms to 200ms.
      >
      > Solution: cache a map<domain name : IP address> and update map periodically 

   4. Other considerations

      > Locality: servers are closer to website hosts -> download faster -> assgin urls to HTML downloader server considering locality.

      > some web page's respond slowly or not respond -> Set maximal wait time to download a page.

      > Consistent hashing to assign urls to download servers in 1 server center.

      > Save crawl states and data/URL/IP for each page in case crawl is disrupted
      >
      > 



##### others

1. Extend to other data types

   >  PNG Downloader: download PNG files.
   >  Web Monitor :monitor the web and prevent copyright or other illegal content

   ![Screenshot 2024-06-04 at 9.51.43 AM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-04 at 9.51.43 AM.png)

2. illegal content

   1. Spider traps: page content causes a crawler in an infinite loop. 

      For instance, an infinite deep directory structure as follows: http://www.spidertrapexample.com/foo/bar/foo/bar/foo/bar/. ..

      Solution: setting amaximal length for URLs. and apply some customized URL filters for some illegal hosts.

   2. Data noise: Some of the contents meaningless, such as advertisements, code snippets, spam URLs, etc. -> added toblack list





### Notification System

##### scenario

1. what types of notifications does the system support? -> Push notification, SMS message, and email.
2. a real-time system? -> receive notifications as soon as possible. delay is acceptable.
3. supported devices? -> iOS devices, android devices, and laptop/desktop.
4. What triggers notifications? ->. triggered by client applications. also able to be scheduled on the server-side.
5. users be able to opt-out? -> users who choose to opt-out will no longer receive notifications.
6. How many notifications are sent out each day? -> 10 million mobile push notifications + 1 million SMS messages + 5 million emails.

##### sevice

1. iOS push notification

   ![Screenshot 2024-06-05 at 8.00.13 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-05 at 8.00.13 PM.png)

   1. Provider: builds and sends notification requests to Apple Push Notification Service (APNS).

      * requests contains:

        >  device token: This is a unique identifier to identify a device which will get notification
        >
        > Payload:  a JSON
         > ``` json
         > {
         >   "aps":{ 
         >     "alert":{
         >       "title":"Game Request",
         >   		"body":"Bob wants to play chess",
         >   		"action-loc-key":"PLAY"
         >     },
         >     "badge": 5
         > }
         > 
         > ```

â	2. APNS: This is a remote service provided by Apple to propagate push notifications to iOS devices.

2. Android push notification

   ![Screenshot 2024-06-05 at 8.10.58 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-05 at 8.10.58 PM.png)



â	 Firebase Cloud Messaging: commonly used to send push notifications to android devices.

3. SMS message

   ![Screenshot 2024-06-05 at 8.11.15 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-05 at 8.11.15 PM.png)

4. Email

   ![Screenshot 2024-06-05 at 8.14.07 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-05 at 8.14.07 PM.png)

   email service can choose: 

   1. set up own email servers
   2. Use commercial email services. 



5. Contact info gathering (device token, phone number, email)

   ![Screenshot 2024-06-05 at 8.17.42 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-05 at 8.17.42 PM.png)

   1. database schema

      ![Screenshot 2024-06-05 at 8.19.31 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-05 at 8.19.31 PM.png)

      1 user -> multiple devices

6. diagram

   ![Screenshot 2024-06-05 at 8.21.24 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-05 at 8.21.24 PM.png)

   1. service can be a micro-service, a scheduler job, or a distributed system

   2. extensibility: a flexible system can easily plugging or unplugging of a third-party service.

   3. Problems:

      > single notification server -> single point failure
      >
      > The notification system on 1 physical server ->  challenging to scale databases, caches, and different notification processing components independently.
      >
      > Performance bottleneck: constructing HTML pages and waiting for responses from third party services could take time -> peak hours -> system overload,

   4. Solutions:

      > Move the database and cache out of the notification server.
      >
      > Add more notification servers and set up automatic horizontal scaling.
      >
      > Introduce message queues to decouple the system components.

      ![Screenshot 2024-06-05 at 8.35.57 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-05 at 8.35.57 PM.png)

      * notification server:

        >  Provide APIs for services to send notifications. accessible internally / by verified clients

        > Carry out basic validations to verify emails, phone numbers, etc.

        > Query the database or cache to fetch data needed to render a notification.

        > Put notification data to message queues for parallel processing.

      * api design to send email

        ![Screenshot 2024-06-05 at 8.43.18 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-05 at 8.43.18 PM.png)

      * message queue: handle high volumes of notifications 

##### Dive deep

1. Reliability

   1. Workers process cannot lose notification -> persist notification data in a database for a retry mechanism.

      ![Screenshot 2024-06-05 at 8.58.49 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-05 at 8.58.49 PM.png)

   2. distributed notification servers -> duplicate notification event (not always happen) -> when a notification event arrives, worker check if the event ID is seen before. -> If so, discard.

2. Notification template

   ![Screenshot 2024-06-05 at 9.12.10 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-05 at 9.12.10 PM.png)

3. Notification settings

   1. **Notification setting table** Schema 

      user_id: bigint

      channel: varchar # push notification, email or SMS

      opt_in: boolean # opt-in to receive notification

4. Rate limiting

   limit the number of notifications a user can receive.

5. retry mechanism

   When a third-party service fails to send a notification, the notification will be added to the message queue for retrying. If the problem still happens, an alert will be sent out to developers.

6. Monitor queued notifications

   too many queued notifications -> more workers process needs for 1 queue

7. Events tracking

   analyze users behavior

   ![Screenshot 2024-06-05 at 9.35.26 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-05 at 9.35.26 PM.png)

8. final design

![Screenshot 2024-06-05 at 9.28.43 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-05 at 9.28.43 PM.png)





### A News Feed System

##### scenario

1. a mobile app Or a web app? -> Both
2. features? -> publish a post & see his friendsâ posts on the news feed page.
3. news feed sorted by reverse chronological order / any particular order such as topic scores? -> reverse chronological order.
4. How many friends can a user have? -> 5000
5. traffic volume? -> 10 million DAU
6. Can feed contain images, videos, or just text? ->  contain media files, including both images and videos.

##### service

1. APIs:

   1.   News publishing: 

      > POST /v1/me/feed
      > Params: 
      >
      > * content: content is the text of the post. (req body)
      > * auth_token: it is used to authenticate API requests.(req parameter or req header)

   2. News retrieval:

      > GET /v1/me/feed
      >
      > Params:
      >
      > * auth_token: it is used to authenticate API requests.(req parameter or req header)

2. News publishing

   ![Screenshot 2024-06-06 at 8.32.26 AM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-06 at 8.32.26 AM.png)

   1. Web servers: web servers redirect traffic to different internal services.
   2. Fanout service: push new content to friends' news feed. Newsfeed data is stored in the cache for fast retrieval.
   3. Notification service: inform friends that new content is available and send out push notifications.

3. News retrieval

   ![Screenshot 2024-06-06 at 8.45.25 AM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-06 at 8.45.25 AM.png)

   1. Web servers: web servers route requests to newsfeed service.
   2. Newsfeed service: news feed service fetches news feed from the cache.
   3. Newsfeed cache: store news feed IDs needed to render the news feed.

##### Dive deep

1. News publishing

   ![Screenshot 2024-06-06 at 8.50.08 AM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-06 at 8.50.08 AM.png)

   1. web servers enforce authentication and rate-limiting. 

   2. Graph databases: managing friend relationship

   3. User Cache/DB: filter friends, based on user's setting. like follower's age

   4. fanout service -> friends list and new post ID -> message queue

   5. news feed cache only store <post_id, user_id>  -> save memory space

      > also we can set a configurable posts num limit for each user in cache. <- user is only  interested in latest posts.

   6. Fanout service

      1. Fanout on write

         > Pros:
         >
         > The news feed is generated in real-time and can be pushed to friends immediately.
         >
         > Fetching news feed is fast because the news feed is pre-computed during write time.

         > Cons:
         >
         > user has many friends -> fetching the friend list & generating news feeds -> slow & time consuming. -> can use Consistent Hashing for News Feed Cache but not enough.
         >
         > For inactive users or those rarely log in, pre-computing news feeds waste computing resources.

      2. Fanout on read

         > Pros:
         >
         > For inactive users or those who rarely log in, fanout on read works better because it will not waste computing resources on them.
         >
         > Data is not pushed to friends -> no "too many friends" problem.

         > Cons:
         >
         > Fetching the news feed is slow as the news feed is not pre-computed.

      3. Final hybrid approach

         * most of users -> Fanout on write. 

         * stars or users who have many friends/followers -> Fanout on read -> let followers pull news content on-demand to avoid system overload.

2. Newsfeed retrieval

   ![Screenshot 2024-06-06 at 8.23.08 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-06 at 8.23.08 PM.png)

   1. News feed service gets a list post IDs from the news feed cache.

   2. A user's news feed is not only a list of feed IDs.  

       -> contains username, profile picture, post content, post image, etc. 

       ->  fetches the complete user and post objects from caches (user cache and post cache) to construct the fully hydrated news feed.

   3. Improve by caches

      ![Screenshot 2024-06-06 at 8.39.02 PM](/Users/isaac/Desktop/Alex/Screenshot 2024-06-06 at 8.39.02 PM.png)

      > News Feed: It stores IDs of news feeds.
      >
      > Content: It stores every post data. Popular content is stored in hot cache.
      >
      > Social Graph: It stores user relationship data.
      >
      > Action: It stores info about whether a user liked a post, replied a post, or took other actions on a post. 
      >
      > Counters: Itstores counters for like, reply, follower, following, etc.
