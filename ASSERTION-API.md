# logsquelcher — Assertion API Design

## Goal

Allow tests to assert on captured log output without requiring `@RegisterExtension`
or any field declaration. The auto-registered extension (via ServiceLoader) is
sufficient for both suppression and assertions.

## Mechanism

`LogSquelcherExtension` implements `ParameterResolver` in addition to `BeforeEachCallback`
and `TestWatcher`.

During `beforeEach`, the extension stores itself in the `ExtensionContext.Store`:

```java
store(context).put(SELF_KEY, this);
```

`ParameterResolver` implementation:

```java
@Override
public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return parameterContext.getParameter().getType() == LogSquelcherExtension.class;
}

@Override
public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    return extensionContext.getStore(NAMESPACE).get(SELF_KEY, LogSquelcherExtension.class);
}
```

## Usage

Tests that only want suppression: no change, no annotation, no field.

Tests that want to assert on log output: declare the extension as a method parameter.

```java
@Test
void shouldLogWarningWhenPluginMissing(LogSquelcherExtension logOnFail) {
    // trigger the behaviour under test
    logOnFail.assertLogged(FilterChainFactory.class, Level.WARN,
        msg -> assertThat(msg).contains("plugin not found"));
}
```

## Assertion API shape (tell-don't-ask)

```java
// Assert at least one log event from the given logger at the given level
// satisfies the assertion. Scoped to the current test's time window.
public void assertLogged(Class<?> logger, Level level, Consumer<String> assertion)

// Convenience — any level
public void assertLogged(Class<?> logger, Consumer<String> assertion)
```

The `Consumer<String>` receives the formatted log message. The caller uses
AssertJ (or any assertion library) inside the consumer. If no matching event
exists, the method fails with a descriptive message listing what was captured.

## Replacing logcaptor

This API covers the logcaptor use cases in Kroxylicious:

| logcaptor | logsquelcher equivalent |
|---|---|
| `LogCaptor.forClass(Foo.class)` | `LogSquelcherExtension` parameter |
| `logCaptor.getLogEvents()` | `assertLogged(Foo.class, ...)` |
| `logCaptor.getWarnLogs()` | `assertLogged(Foo.class, Level.WARN, ...)` |
| `assertThat(logCaptor.getWarnLogs()).isEmpty()` | `assertNotLogged(Foo.class, Level.WARN)` |

The `assertNotLogged` variant is also needed:

```java
public void assertNotLogged(Class<?> logger, Level level)
```

## Effect on Kroxylicious

Once this API is available in logsquelcher:

1. Rewrite `FilterChainFactoryTest` and `ServiceBasedPluginFactoryRegistryTest`
   to use `LogSquelcherExtension` parameter injection
2. Remove `io.github.hakky54:logcaptor` from `kroxylicious-runtime/pom.xml`
3. `log4j-to-slf4j` (pulled in transitively by logcaptor) disappears from the
   classpath, resolving the `ClassCastException` in `HostPortTest`
