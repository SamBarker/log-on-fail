# logsquelcher

A JUnit 5 extension that silences log output during passing tests and replays it only on failure.

## The problem

Many test suites deliberately exercise failure modes — filters that reject malformed messages,
handlers that short-circuit on bad input, retry logic that expects transient errors. The code under
test legitimately logs warnings and stack traces as part of these scenarios.

The trouble is that this output appears during **every** test run, including passing ones. Hundreds
of stack traces scroll past on a green build, training developers to ignore the log output entirely
— which means real problems get lost in the noise.

A `ThreadLocal` approach doesn't help when the logging happens off the test thread. Frameworks like
[Kroxylicious](https://github.com/kroxylicious/kroxylicious) run filter code on Netty I/O threads
from a fixed shared pool, so thread-local capture misses exactly the events that matter.

## How it works

`logsquelcher` registers itself as the SLF4J provider and wraps the real logging backend (Logback,
Log4j2, or `slf4j-simple` as a last resort). All log events from all threads are captured into a
time-stamped global buffer.

A JUnit 5 extension records the start time of each test. On failure it extracts the events that
fell inside the test's window and replays them through the real backend — so they appear in the
normal console output. On success the events are silently discarded.

## Requirements

- Java 17+
- JUnit Jupiter 5.x
- SLF4J 2.x

## Installation

```xml
<dependency>
    <groupId>nz.thebarkers</groupId>
    <artifactId>logsquelcher</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

`slf4j-simple` ships as a transitive dependency and acts as a fallback backend when no other SLF4J
backend is on the classpath. If you already have Logback or Log4j2, they are preferred automatically.

## Registration

### Automatic (recommended)

Add to `src/test/resources/junit-platform.properties`:

```properties
junit.jupiter.extensions.autodetection.enabled=true
```

The extension and SLF4J provider register themselves via `ServiceLoader` — no per-class annotation
needed.

### Per-class

```java
@ExtendWith(LogSquelcherExtension.class)
class MyTest { ... }
```

## Usage

With the extension registered, tests require no changes. Logs are silenced on green runs and
replayed automatically on failure.

### Asserting log output in tests

Inject the extension as a parameter to query captured events:

```java
@Test
void warningIsLoggedWhenPluginIsDeprecated(LogSquelcherExtension ext) {
    // exercise the code under test
    subject.doSomething();

    assertThat(ext.logged(MyService.class, Level.WARN))
            .hasFormattedMessage("Plugin is deprecated");
}
```

`ext.logged(Class, Level)` returns the first matching `LoggingEvent` or throws `AssertionError`
listing everything that was captured if none matched.

Use `ext.logged(Class)` to match any level:

```java
assertThat(ext.logged(MyService.class))
        .hasFormattedMessage("Plugin is deprecated");
```

### Key-value pairs (structured logging)

```java
assertThat(ext.logged(MyService.class, Level.WARN))
        .hasFormattedMessage("Plugin is deprecated")
        .containsKeyValue("filterName", "myFilterDef");
```

### Negative assertion

```java
ext.assertNotLogged(MyService.class, Level.ERROR);
```

Throws `AssertionError` if any matching event was captured, listing the offending messages.

## Backend compatibility

`logsquelcher` wraps whichever SLF4J provider it finds. Preference order:

1. Any real backend (Logback, Log4j2, etc.)
2. `slf4j-simple` (bundled as fallback)

When multiple providers are on the classpath SLF4J will warn about the ambiguity and pick one.
In most Maven setups `logsquelcher` wins because it is a direct dependency. If your project declares
Logback as a direct dependency and it takes precedence, pin the provider explicitly in Surefire:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <systemPropertyVariables>
            <slf4j.provider>nz.thebarkers.logsquelcher.LogSquelcherSLF4JProvider</slf4j.provider>
        </systemPropertyVariables>
    </configuration>
</plugin>
```
