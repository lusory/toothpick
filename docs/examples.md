# Examples

## Creating an Injector

An `Injector` instance can be created with the `Injector#of` method (invoke operator for Kotlin is available).

### Java

```java
import me.lusory.toothpick.Injector;

// ...

// dynamically populated injector (no providers by default)
final Injector injector = Injector.of();

// pre-populated injector
final Injector injector = Injector.of(new Module1());

// ...

// retrieves a javax.inject.Provider instance,
// which instantiates the Module1 class with the no-arg or @javax.inject.Inject-annotated constructor,
// following the dependency chain to satisfy the dependencies
injector.provider(Module1.class);

// equals to injector.provider(Module1.class).get()
injector.instance(Module1.class);
```

### Kotlin
```java
import me.lusory.toothpick.Injector

// ...

// dynamically populated injector (no providers by default)
val injector = Injector()

// pre-populated injector
val injector = Injector(Module1())

// ...

// retrieves a javax.inject.Provider instance,
// which instantiates the Module1 class with the no-arg or @javax.inject.Inject-annotated constructor,
// following the dependency chain to satisfy the dependencies
injector.provider(Module1::class)

// equals to injector.provider(Module1.class).get()
injector[Module1::class]
```