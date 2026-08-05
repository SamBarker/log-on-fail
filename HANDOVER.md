# logsquelcher — Project Handover

This document captures the design agreed with the user. Use it as the implementation brief.

---

## Context

Many Kroxylicious tests exercise failure modes intentionally. Filter code runs on Netty shared event
loop threads and logs exceptions as part of normal test operation. Currently all these logs appear on
stdout during every test run — hundreds of stack traces that trigger alarm even on passing builds.

**Goal**: silence logs during passing tests, surface them only on failure.

**Why not ThreadLocal**: Netty uses a fixed pool of shared I/O threads. `ThreadLocal` only captures
the JUnit test thread's logs, missing filter-layer exceptions — exactly where the noise originates.

**Why a standalone library**: the utility is not Kroxylicious-specific; a standalone repo under
`io.github.sambarker` can be versioned independently and potentially adopted by other projects or promoted
to the Kroxylicious org later.

---

## Approach: global bounded buffer + time-windowed extraction

One `ConcurrentLinkedDeque` accumulates all log events from all threads, each stamped with
`System.nanoTime()`. The JUnit extension records `startNanos` / `endNanos` around each test and
extracts the matching slice on failure.

**Known limitation — parallel tests**: the extension makes no attempt to discriminate between
threads belonging to different test instances. When two test classes run concurrently, each with
their own Kroxylicious instance, their time windows overlap and Netty thread events from one
instance may appear in the other's failure dump. This is accepted: the primary goal is silencing
passing-test noise, not perfect per-test isolation. A future enhancement (thread-set registration)
could address this if it becomes painful.

---

## Design

### `CapturedEvent` (record)
```
long nanoTime
LogEvent event   // immutable copy via event.toImmutable()
```

### `CapturingAppender` (extends `AbstractAppender`)

- Static `ConcurrentLinkedDeque<CapturedEvent>` shared across all tests.
- `append()`: timestamps event with `System.nanoTime()`, adds to deque, then trims entries older
  than a configurable TTL (default 300 s) to bound memory.
- `ensureRegistered()`: synchronized, idempotent. On first call, adds the appender to Log4j2's
  root logger at `Level.TRACE` via the programmatic API and calls `ctx.updateLoggers()`.
  The level means the appender sees whatever events the existing `log4j2-test.*` configs emit;
  no changes to those files are needed.
- `extractWindow(long startNanos, long endNanos)`: returns a snapshot list of events in range.

### `LogSquelcherExtension` (implements `BeforeEachCallback`, `TestWatcher`)

- `beforeEach(ctx)`: calls `CapturingAppender.ensureRegistered()`, stores
  `startNanos = System.nanoTime()` in the extension's `ExtensionContext.Store`.
- `testFailed(ctx, cause)`: records `endNanos`, calls `extractWindow(...)`, routes to output.
- `testSuccessful / testAborted / testDisabled`: no-op — events age out of the deque naturally.

### Output routing (factory methods on `LogSquelcherExtension`)

| Factory | Behaviour |
|---|---|
| `new LogSquelcherExtension()` or `.auto()` | Check `CI` env var: CI → file; otherwise → stdout |
| `.stdout()` | Always stdout |
| `.file(Path dir)` | Always write `<dir>/<ClassName>/<methodName>.log` |

CI file path: `target/log-capture/<SimpleClassName>/<methodName>.log` (created if absent).

### Output format (stdout)
```
══════════════════════════════════════════════════════════
LOG CAPTURE — MyTest#testFoo [FAILED]
══════════════════════════════════════════════════════════
2024-01-01 12:00:00.001  WARN  [krox-io-thread-0]  io.kroxylicious.Foo - message
...
══════════════════════════════════════════════════════════
```

---

## Project coordinates

- **GAV**: `io.github.sambarker:logsquelcher:0.1.0-SNAPSHOT`
- **Java**: 17
- **No Kroxylicious dependencies**

### Compile dependencies
- `org.apache.logging.log4j:log4j-core`
- `org.junit.jupiter:junit-jupiter-api`

### Test dependencies
- `org.junit.jupiter:junit-jupiter-engine`
- `org.apache.logging.log4j:log4j-slf4j2-impl` (to exercise the SLF4J path)
- `org.slf4j:slf4j-api`

### Version reference (match Kroxylicious primary target)
- Log4j2: `2.26.1`
- JUnit: `5.14.4`
- SLF4J: `2.0.18`

---

## Project structure

```
/Users/sbarker/src/sambarker/logsquelcher/
├── pom.xml
└── src/
    ├── main/java/io/github/sambarker/logsquelcher/
    │   ├── LogSquelcherExtension.java
    │   ├── CapturingAppender.java
    │   └── CapturedEvent.java
    └── test/java/io/github/sambarker/logsquelcher/
        └── LogSquelcherExtensionTest.java
```

---

## Usage in Kroxylicious (once published)

```java
@RegisterExtension
static LogSquelcherExtension logOnFail = LogSquelcherExtension.auto();
```

Or class-wide via `@ExtendWith(LogSquelcherExtension.class)` (uses `auto()` behaviour).

---

## Commit plan (one subject per commit, tests with production code)

1. **Scaffold project**: `pom.xml`, empty package structure, `.gitignore`, init git
2. **`CapturedEvent` + `CapturingAppender`**: buffer, append, TTL trim, extractWindow, ensureRegistered — with unit tests
3. **`LogSquelcherExtension` (stdout path)**: beforeEach, testFailed, testSuccessful, stdout output — with unit tests
4. **File output + CI detection**: `.file()` and `.auto()` factory methods — with unit tests
5. **Integration smoke test**: a test that deliberately fails via SLF4J to verify the full end-to-end path

### Commit message format
```
<type>: <subject>

<body>

Assisted-by: Claude Sonnet 4.6 <noreply@anthropic.com>
Signed-off-by: Sam Barker <...>
```

---

## Verification checklist

1. A test that logs then fails → log output appears on stdout
2. A test that logs then passes → no output
3. Two tests in sequence in the same class: only the failing one produces output
4. `CI=true mvn test` → file written to `target/log-capture/<Class>/<method>.log`
