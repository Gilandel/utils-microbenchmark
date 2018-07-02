/*-
 * #%L
 * utils-microbenchmark
 * %%
 * Copyright (C) 2016 - 2018 Gilles Landel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package fr.landel.utils.microbenchmark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.DoubleStream;

import org.junit.Test;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.results.BenchmarkResult;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Defaults;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.openjdk.jmh.runner.options.VerboseMode;
import org.openjdk.jmh.runner.options.WarmupMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all JMH benchmarks.
 * 
 * <p>
 * Based on the Netty project class.
 * </p>
 * 
 * <p>
 * To use it directly in Eclipse (example for debugging purpose), just run the
 * maven build in test life cycle to generate JMH classes.
 * </p>
 *
 * @since Aug 8, 2016
 * @author Gilles
 *
 */
public abstract class AbstractMicrobenchmark {

	protected static final Mode DEFAULT_MODE = Defaults.BENCHMARK_MODE;
    protected static final TimeUnit DEFAULT_TIME_UNIT = Defaults.OUTPUT_TIMEUNIT;
	protected static final WarmupMode DEFAULT_WARMUP_MODE = Defaults.WARMUP_MODE;
	protected static final int DEFAULT_WARMUP_FORKS = Defaults.WARMUP_FORKS;
	protected static final int DEFAULT_WARMUP_BATCH_SIZE = Defaults.WARMUP_BATCHSIZE;
    protected static final int DEFAULT_WARMUP_ITERATIONS = 3;
    protected static final TimeValue DEFAULT_WARMUP_TIME = TimeValue.seconds(1);
    protected static final int DEFAULT_MEASUREMENT_ITERATIONS = Defaults.MEASUREMENT_ITERATIONS;
    protected static final int DEFAULT_MEASUREMENT_FORKS = 1;
    protected static final int DEFAULT_MEASUREMENT_BATCH_SIZE = Defaults.MEASUREMENT_BATCHSIZE;
    protected static final int DEFAULT_MEASUREMENT_THREADS = 4;
    protected static final TimeValue DEFAULT_MEASUREMENT_TIME = TimeValue.seconds(2);
    protected static final int DEFAULT_OPS_PER_INVOCATION = Defaults.OPS_PER_INVOCATION;

    // Use the default JVM args
    protected static final String JVM_ARGS = "-server";

    protected static final String OUTPUT_DIRECTORY = "target/benchmark/";

    private static final Logger LOGGER = LoggerFactory.getLogger("Microbenchmark");

    public Collection<RunResult> run() throws IOException, RunnerException {
        final String classPath = this.getClass().getCanonicalName();

        // @formatter:off
        final ChainedOptionsBuilder runnerOptions = new OptionsBuilder()
                .include(classPath)
                .jvmArgs(this.getJvmArgs())
                .mode(this.getMode())
                .timeUnit(this.getTimeUnit())
                .warmupIterations(this.getWarmupIterations())
                .warmupBatchSize(this.getWarmupBatchSize())
                .warmupForks(this.getWarmupForks())
                .warmupMode(this.getWarmupMode())
                .warmupTime(this.getWarmupTime())
                .threads(this.getMeasurementThreads())
                .measurementIterations(this.getMeasurementIterations())
                .measurementBatchSize(this.getMeasurementBatchSize())
                .forks(this.getMeasurementForks())
                .measurementTime(this.getMeasurementTime())
                .operationsPerInvocation(this.getOpsPerInvocation());
        // @formatter:on

        this.addOptions(runnerOptions);

        final File file = new File(this.getOutputDirectory(), classPath + ".json");

        if (!file.getParentFile().exists()) {
            assertTrue(file.getParentFile().mkdirs());
        } else {
            file.delete();
        }

        runnerOptions.resultFormat(ResultFormatType.JSON);
        runnerOptions.result(file.getAbsolutePath());

        runnerOptions.verbosity(this.getVerboseMode());

        final Runner runner = new Runner(runnerOptions.build());

        final Collection<RunResult> runResults = runner.run();

        assertNotNull(runResults);
        assertNotEquals(0, runResults.size());

        for (final RunResult runResult : runResults) {
            assertEquals(this.getMeasurementForks(), runResult.getBenchmarkResults().size());

            for (final BenchmarkResult result : runResult.getBenchmarkResults()) {
                assertEquals(this.getMeasurementIterations(), result.getIterationResults().size());

                final DoubleStream.Builder scores = DoubleStream.builder();
                for (final IterationResult ir : result.getIterationResults()) {
                    scores.add(ir.getRawPrimaryResults().stream().mapToDouble(Result::getScore).average().getAsDouble());
                }

                final Double avgScore = scores.build().average().getAsDouble();
                LOGGER.info(String.format("[%1$s] score: %2$,.3f %4$s, expected > %3$,.3f %4$s", result.getParams().getBenchmark(),
                        avgScore, this.getExpectedMinNbOpsPerSeconds(), result.getScoreUnit()));

                final String onBadScore = String.format("[%1$s] Average score is lower than expected: %2$,.3f %4$s < %3$,.3f %4$s",
                        result.getParams().getBenchmark(), avgScore, this.getExpectedMinNbOpsPerSeconds(), result.getScoreUnit());
                assertTrue(onBadScore, avgScore > this.getExpectedMinNbOpsPerSeconds());
            }
        }

        return runResults;
    }

    /**
     * Add extra options to the runner
     * 
     * @param runnerOptions
     *            the current chained options builder
     */
    protected void addOptions(final ChainedOptionsBuilder runnerOptions) {
        // do nothing
    }
    
    /**
     * @return the operations per invocation
     */
    protected int getOpsPerInvocation() {
		return AbstractMicrobenchmark.DEFAULT_OPS_PER_INVOCATION;
	}
    
    /**
     * @return the time unit
     */
    protected TimeUnit getTimeUnit() {
		return AbstractMicrobenchmark.DEFAULT_TIME_UNIT;
	}
    
    /**
     * @return the mode
     */
    protected Mode getMode() {
    	return AbstractMicrobenchmark.DEFAULT_MODE;
    }
    
    /**
     * @return the warmup mode
     */
    protected WarmupMode getWarmupMode() {
		return AbstractMicrobenchmark.DEFAULT_WARMUP_MODE;
	}
    
    /**
     * @return the number of warmup forks
     */
    protected int getWarmupForks() {
        return AbstractMicrobenchmark.DEFAULT_WARMUP_FORKS;
    }

    /**
     * @return the number of warmup iterations
     */
    protected int getWarmupIterations() {
        return AbstractMicrobenchmark.DEFAULT_WARMUP_ITERATIONS;
    }
    
    /**
     * @return the number of batch size
     */
    protected int getWarmupBatchSize() {
		return AbstractMicrobenchmark.DEFAULT_WARMUP_BATCH_SIZE;
	}
    
    /**
     * @return the warmup time
     */
    protected TimeValue getWarmupTime() {
		return AbstractMicrobenchmark.DEFAULT_WARMUP_TIME;
	}

    /**
     * @return the measurement iterations
     */
    protected int getMeasurementIterations() {
        return AbstractMicrobenchmark.DEFAULT_MEASUREMENT_ITERATIONS;
    }
    
    /**
     * @return the measurement batch size
     */
    protected int getMeasurementBatchSize() {
		return AbstractMicrobenchmark.DEFAULT_MEASUREMENT_BATCH_SIZE;
	}
    
    /**
     * @return the measurement time
     */
    protected TimeValue getMeasurementTime() {
		return AbstractMicrobenchmark.DEFAULT_MEASUREMENT_TIME;
	}

    /**
     * @return the measurement number of forks
     */
    protected int getMeasurementForks() {
        return AbstractMicrobenchmark.DEFAULT_MEASUREMENT_FORKS;
    }

    /**
     * @return the measurement number of threads
     */
    protected int getMeasurementThreads() {
		return AbstractMicrobenchmark.DEFAULT_MEASUREMENT_THREADS;
	}
    
    /**
     * @return the jvmArgs
     */
    protected String getJvmArgs() {
        return AbstractMicrobenchmark.JVM_ARGS;
    }

    /**
     * @return the outputDirectory
     */
    protected String getOutputDirectory() {
        return AbstractMicrobenchmark.OUTPUT_DIRECTORY;
    }

    /**
     * @return the verbose mode
     */
    protected VerboseMode getVerboseMode() {
        return VerboseMode.SILENT;
    }

    /**
     * @return the expectedMinNbOpsPerSeconds
     */
    protected abstract double getExpectedMinNbOpsPerSeconds();

    /**
     * Run the test
     * 
     * @throws IOException
     *             on error related to the associated json file
     * @throws RunnerException
     *             on runner error
     */
    @Test
    public void testRun() throws IOException, RunnerException {
        assertNotNull(this.run());
    }
}
