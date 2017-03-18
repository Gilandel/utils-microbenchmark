# utils-microbenchmark

[![Build Status](https://api.travis-ci.org/Gilandel/utils-microbenchmark.svg?branch=master)](https://travis-ci.org/Gilandel/utils-microbenchmark/builds)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/90454c15ecd24ce985b5ee82cb93a558)](https://www.codacy.com/app/gilles/utils-microbenchmark)
[![Dependency Status](https://www.versioneye.com/user/projects/58b29b6f7b9e15003a17e544/badge.svg?style=flat)](https://www.versioneye.com/user/projects/58b29b6f7b9e15003a17e544)
[![codecov.io](https://codecov.io/github/Gilandel/utils-microbenchmark/coverage.svg?branch=master)](https://codecov.io/github/Gilandel/utils-microbenchmark?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/fr.landel.utils/utils-microbenchmark/badge.svg)](https://maven-badges.herokuapp.com/maven-central/fr.landel.utils/utils-microbenchmark)

[![codecov.io tree](https://codecov.io/gh/Gilandel/utils-benchmark/branch/master/graphs/tree.svg)](https://codecov.io/gh/Gilandel/utils-benchmark/branch/master)
[![codecov.io sunburst](https://codecov.io/gh/Gilandel/utils-benchmark/branch/master/graphs/sunburst.svg)](https://codecov.io/gh/Gilandel/utils-benchmark/branch/master)

Work progress:
![Code status](http://vbc3.com/script/progressbar.php?text=Code&progress=100)
![Test status](http://vbc3.com/script/progressbar.php?text=Test&progress=100)
![JavaDoc status](http://vbc3.com/script/progressbar.php?text=JavaDoc&progress=100)

```xml
<dependency>
	<groupId>fr.landel.utils</groupId>
	<artifactId>utils-microbenchmark</artifactId>
	<version>1.0.2</version>
</dependency>
```

## Features:
- AbstractMicrobenchmark: An abstract class to easily create JMH test cases

## How to use it

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
<!-- Performance testing -->
<dependency>
	<groupId>fr.landel.utils</groupId>
	<artifactId>utils-microbenchmark</artifactId>
	<version>${utils-microbenchmark.version}</version>
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

First run this class through 'mvn test' command.
This will generate performance configuration.
After that, test classes can be run like any other JUnit test case. 

## License
See [main project license](https://github.com/Gilandel/utils/LICENSE): Apache License, version 2.0