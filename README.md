
# My Jedis 🚀

A custom implementation of the Redis server built from scratch in **Java**. This project was a deep dive into the internals of Redis, covering its protocol (RESP), core data structures, concurrency model, and advanced features like Replication and Persistence.

[![Language](https://img.shields.io/badge/Language-Java-blue.svg)](https://www.java.com/en/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![GitHub stars](https://img.shields.io/github/stars/Kunals990/my-own-redis-java.svg?style=social&label=Star&maxAge=2592000)](https://github.com/Kunals990/my-own-redis-java)

---

##  Features

I've implemented a wide range of features, bringing this server close to the functionality of the real Redis.

### Implemented Modules

| Category | Description |
| :--- | :--- |
| **Core** | Basic key-value operations, concurrency, and key expiry. |
| **Lists** | Full implementation of list data structures and their commands. |
| **Streams** | Support for stream data types, including blocking reads. |
| **Transactions** | Atomic execution of command blocks using `MULTI` and `EXEC`. |
| **Replication** | Master-replica setup for high availability and data propagation. |
| **RDB Persistence** | Basic RDB file parsing to load data on startup. |
| **Pub/Sub** | Real-time messaging with channels for publishers and subscribers. |
| **Sorted Sets** | The powerful sorted set data type with score-based ordering. |

<br>

---

## 🛠️ Getting Started

Follow these instructions to get the server compiled and running on your local machine.

### Prerequisites

* **Java 24** (or newer)
* **Maven** (to build the project)
* **`redis-cli`** (to connect to the server)

### Installation & Running

1.  **Clone the repository:**
    ```sh
    git clone [https://github.com/Kunals990/my-own-redis-java.git](https://github.com/Kunals990/my-own-redis-java.git)
    cd my-own-redis-java
    ```

2.  **Build and Run the Server:**
    The project includes a shell script that handles both compiling and running the server.
    ```sh
    ./your-program.sh 
    ```
    *(Note: Replace `your-script-name.sh` with the actual name of your script, e.g., `run.sh`)*

    This script will:
    * Compile the Java source code using Maven and package it into a `.jar` file.
    * Execute the compiled `.jar` file to start the Redis server.

3.  **Connect with `redis-cli`:**
    With the server running, open another terminal and connect to it using the standard Redis client.
    ```sh
    redis-cli
    ```
    You're in! Now you can test any of the implemented commands:
    ```
    127.0.0.1:6379> PING
    PONG
    127.0.0.1:6379> SET framework "spring"
    OK
    127.0.0.1:6379> GET framework
    "spring"
    127.0.0.1:6379> ZADD myzset 1 "one"
    (integer) 1
    ```

---

## 📚 Implemented Commands

The server supports the following Redis commands, grouped by module:

| Core & Config | Lists | Pub/Sub | Sorted Sets | Streams | Transactions | Replication |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `PING` | `LPUSH` | `SUBSCRIBE` | `ZADD` | `XADD` | `MULTI` | `PSYNC` |
| `ECHO` | `RPUSH` | `UNSUBSCRIBE` | `ZCARD` | `XRANGE`| `EXEC` | `REPLCONF` |
| `SET` | `LPOP` | `PUBLISH` | `ZRANGE`| `XREAD` | | `WAIT` |
| `GET` | `LRANGE`| | `ZRANK` | | | `INFO` |
| `INCR` | `LLEN` | | `ZREM` | `TYPE` | | |
| `CONFIG`| `BLPOP` | | `ZSCORE` | | | |
| `KEYS` | | | | | | |

---

## 🗺️ Development Journey

This project was built incrementally, following a structured path to ensure each piece of functionality was built on a solid foundation. Here's a look at the stages that were completed.

<details>
<summary><strong>Core Functionality</strong></summary>

- Bind to a port
- Respond to PING
- Respond to multiple PINGs
- Handle concurrent clients
- Implement the ECHO command
- Implement the SET & GET commands
- Expiry
</details>

<details>
<summary><strong>Lists</strong></summary>

- Create a list
- Append an element
- Append multiple elements
- List elements (positive & negative indexes)
- Prepend elements
- Query list length
- Remove an element (single & multiple)
- Blocking retrieval (with & without timeout)
</details>

<details>
<summary><strong>Streams</strong></summary>

- The TYPE command
- Create a stream & validate entry IDs
- Partially & fully auto-generated IDs
- Query entries from stream (with `*` and `+`)
- Query single & multiple streams using `XREAD`
- Blocking reads (with & without timeout, and using `$`)
</details>

<details>
<summary><strong>Transactions</strong></summary>

- The INCR command
- The MULTI & EXEC commands
- Handling of empty & queued transactions
- The DISCARD command
- Failures within transactions
- Multiple transactions
</details>

<details>
<summary><strong>Replication</strong></summary>

- Configure listening port
- The INFO command (master & replica)
- Replication ID and offset initialization
- Master-Replica handshake protocol
- Empty RDB transfer
- Single & multi-replica command propagation
- Asynchronous ACKs
- The WAIT command
</details>

<details>
<summary><strong>RDB Persistence</strong></summary>

- RDB file configuration
- Read single & multiple keys
- Read string values
- Read values with expiry
</details>

<details>
<summary><strong>Pub/Sub</strong></summary>

- Subscribe to single & multiple channels
- Enter subscribed mode
- PING in subscribed mode
- Publish & deliver messages
- Unsubscribe
</details>

<details>
<summary><strong>Sorted Sets</strong></summary>

- Create a sorted set
- Add members
- Retrieve member rank & score
- List members (with positive & negative indexes)
- Count members
- Remove a member
</details>

---

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.