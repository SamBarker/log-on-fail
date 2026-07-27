# log-on-fail

A JUnit 5 extension that silences Log4j2 output during passing tests and surfaces it only on failure.

## The problem

Many test suites deliberately exercise failure modes — filters that reject malformed messages,
handlers that short-circuit on bad input, retry logic that expects transient errors. The code under
test legitimately logs warnings and stack traces as part of these scenarios.

The trouble is that this output appears on stdout during **every** test run, including passing ones.
Hundreds of stack traces scroll past on a green build, training developers to ignore the log output
entirely — which means real problems get lost in the noise.

A `ThreadLocal` approach doesn't help when the logging happens off the test thread. Frameworks like
[Kroxylicious](https://github.com/kroxylicious/kroxylicious) run filter code on Netty I/O threads
from a fixed shared pool, so thread-local capture misses exactly the events that matter.

## The solution

`log-on-fail` installs a single Log4j2 appender that buffers all log events from all threads in a
time-stamped ring buffer. A JUnit 5 extension records the wall-clock window around each test. On
failure, it extracts and prints only the events that fell inside that window. On success, nothing
is printed and the events age out of the buffer automatically.

## Usage

```java
@RegisterExtension
static LogOnFailExtension logOnFail = LogOnFailExtension.auto();
```

`auto()` writes to stdout locally and to `target/log-capture/<Class>/<method>.log` on CI
(detected via the `CI` environment variable). You can also use `LogOnFailExtension.stdout()` or
`LogOnFailExtension.file(Path)` to pin the destination explicitly.

Alternatively, apply it to every test in a class without a field:

```java
@ExtendWith(LogOnFailExtension.class)
class MyTest { ... }
```

## Known limitation — parallel tests

When two test classes run concurrently, their capture windows overlap. Log events from one test's
infrastructure threads may appear in the other test's failure dump. This is accepted: the primary
goal is silencing passing-test noise, not perfect per-test isolation.

## Coordinates

```xml
<dependency>
    <groupId>nz.thebarkers</groupId>
    <artifactId>log-on-fail</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```
