# Benchmarks

Benchmarking is done via the Java Microbenchmark Harness (JMH), two other popular DI frameworks are included for comparison:

 - [Feather](https://github.com/zsoltherpai/feather)
 - [Guice](https://github.com/google/guice)

The benchmark source code can be viewed [here](https://github.com/lusory/toothpick/blob/master/core/src/jmh/java/me/lusory/toothpick/jmh/InjectorBenchmarkTest.java).
If you want to benchmark yourself, run the `jmh` Gradle task (`./gradlew jmh`, may take around 20 minutes to complete).

```
# ref: 818f57ba5fe0568a31501b641486645454a2fc57

# Warmup: 5 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Throughput, ops/time

Result "me.lusory.toothpick.jmh.InjectorBenchmarkTest.injectFeather":
  2292110,973 ±(99.9%) 23716,448 ops/s [Average]
  (min, avg, max) = (2215965,796, 2292110,973, 2342697,310), stdev = 31660,779
  CI (99.9%): [2268394,525, 2315827,420] (assumes normal distribution)

Result "me.lusory.toothpick.jmh.InjectorBenchmarkTest.injectGuice":
  64629,688 ±(99.9%) 2341,572 ops/s [Average]
  (min, avg, max) = (59531,646, 64629,688, 70265,624), stdev = 3125,932
  CI (99.9%): [62288,115, 66971,260] (assumes normal distribution)

Result "me.lusory.toothpick.jmh.InjectorBenchmarkTest.injectToothpick":
  418040,166 ±(99.9%) 25708,211 ops/s [Average]
  (min, avg, max) = (370046,111, 418040,166, 470898,187), stdev = 34319,726
  CI (99.9%): [392331,955, 443748,378] (assumes normal distribution)

Benchmark                               Mode  Cnt        Score       Error  Units
InjectorBenchmarkTest.injectFeather    thrpt   25  2292110,973 ± 23716,448  ops/s
InjectorBenchmarkTest.injectGuice      thrpt   25    64629,688 ±  2341,572  ops/s
InjectorBenchmarkTest.injectToothpick  thrpt   25   418040,166 ± 25708,211  ops/s
```