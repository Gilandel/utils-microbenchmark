# utils-microbenchmark

[![Build Status](https://api.travis-ci.org/Gilandel/utils-microbenchmark.svg?branch=master)](https://travis-ci.org/Gilandel/utils-microbenchmark/builds)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/90454c15ecd24ce985b5ee82cb93a558)](https://www.codacy.com/app/gilles/utils-microbenchmark)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/90454c15ecd24ce985b5ee82cb93a558)](https://www.codacy.com/app/gilles/utils-microbenchmark)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/fr.landel.utils/utils-microbenchmark/badge.svg)](https://maven-badges.herokuapp.com/maven-central/fr.landel.utils/utils-microbenchmark)
[![Javadocs](http://www.javadoc.io/badge/fr.landel.utils/utils-microbenchmark.svg)](http://www.javadoc.io/doc/fr.landel.utils/utils-microbenchmark)

[![Tokei LoC](https://tokei.rs/b1/github/Gilandel/utils-microbenchmark)](https://github.com/Aaronepower/tokei)
[![Tokei NoFiles](https://tokei.rs/b1/github/Gilandel/utils-microbenchmark?category=files)](https://github.com/Aaronepower/tokei)
[![Tokei LoComments](https://tokei.rs/b1/github/Gilandel/utils-microbenchmark?category=comments)](https://github.com/Aaronepower/tokei)

Work progress:
![Code status](http://vbc3.com/script/progressbar.php?text=Code&progress=100)
![Test status](http://vbc3.com/script/progressbar.php?text=Test&progress=100)
![JavaDoc status](http://vbc3.com/script/progressbar.php?text=JavaDoc&progress=100)

```xml
<dependency>
	<groupId>fr.landel.utils</groupId>
	<artifactId>utils-microbenchmark</artifactId>
	<version>1.0.7</version>
</dependency>
```

## Features:
- AbstractMicrobenchmark: An abstract class to easily create JMH test cases

## How to use it

Create source directory (and resources):
- src/perf/java
- src/perf/resources

Add performance source directory (and resources) in pom.xml
```xml
<plugin>
	<groupId>org.codehaus.mojo</groupId>
	<artifactId>build-helper-maven-plugin</artifactId>
	<executions>
		<execution>
			<id>add-perf-sources</id>
			<phase>generate-sources</phase>
			<goals>
				<goal>add-test-source</goal>
			</goals>
			<configuration>
				<sources>
					<source>src/perf/java</source>
				</sources>
			</configuration>
		</execution>
		<!-- <execution>
			<id>add-perf-resources</id>
			<phase>generate-sources</phase>
			<goals>
				<goal>add-test-resource</goal>
			</goals>
			<configuration>
				<resources>
					<resource>
						<directory>src/perf/resources</directory>
					</resource>
				</resources>
			</configuration>
		</execution> -->
	</executions>
</plugin>
```

Add dependency in pom.xml
```
<!-- Unit testing -->
<dependency>
	<groupId>junit</groupId>
	<artifactId>junit</artifactId>
	<scope>test</scope>
</dependency>

<!-- Performance testing -->
<dependency>
	<groupId>fr.landel.utils</groupId>
	<artifactId>utils-microbenchmark</artifactId>
	<scope>test</scope>
</dependency>
```

Create the performance class test
```java
@State(Scope.Benchmark)
public class MyClassPerf extends AbstractMicrobenchmark {
	
	@Override
	protected double getExpectedMinNbOpsPerSeconds() {
		return 200_000d; // the minimum number of operations per seconds
	}
	
	/**
	 * Test method for
	 * {@link MyClass#myMethod}.
	 */
	@Benchmark
	public void testMyMethod() {
		// Place here, code under test
	}
	
	@Test
	public void testPerf() throws IOException, RunnerException {
		assertNotNull(super.run());
	}
}
```

First run this class or project through 'mvn test' command.
This will generate sources for the performance test (target/generated-test-sources/test-annotations/**).
After that, test classes can be run like any other JUnit test case.
Re-run the Maven command after each signature modification.

To analyze test error, the verbose can be increased by adding this:
```java
    @Override
    protected VerboseMode getVerboseMode() {
        return VerboseMode.EXTRA;
    }
```

Others methods that can be overridden:
- getWarmupMode: the warmup mode (default: INDI (do the individual warmup for every benchmark))
- getWarmupIterations: the number of loops to warm-up the task under test (by default: 3)
- getWarmupBatchSize: the batch size in warmup mode (default: 1)
- getWarmupForks: the number of warmup forks we discard (default: 0)
- getWarmupTime: the duration of warmup iterations (default: 1 second)
- getMeasurementThreads: the number of threads (default: 4)
- getMeasurementIterations: the number of loops to measure the task performance (by default: 5)
- getMeasurementBatchSize: the batch size in measurement mode (default: 1)
- getMeasurementForks: the forks in which we measure the workload (default: 1)
- getMeasurementTime: the duration of measurement iterations (default: 1 second)
- getNumForks: the number of forks to run (by default: 1)
- getJvmArgs: the JVM arguments (by default: -server)
- getOutputDirectory: the output directory (by default: target/benchmark)
- getVerboseMode: the verbose mode (default: silent)
- getMode: the benchmark mode (default: Throughput, ops/time)
- getTimeUnit: the time unit (default: seconds)
- getOpsPerInvocation: the operations per invocation (default: 1)

The "addOptions" can also be overridden to add extra options to the runner.

## Changelog
### 1.0.7 - 2018-07-02
- New: improve abstract microbenchmark class

### 1.0.6 - 2018-07-02
- Misc: update dependencies
- Misc: remove classpath definition from JAR (Wildfly warning when some dependencies are in multiple versions and defined provided)

## License
Apache License, version 2.0