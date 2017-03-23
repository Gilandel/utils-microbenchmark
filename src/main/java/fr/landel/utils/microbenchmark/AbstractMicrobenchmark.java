/*-
 * #%L
 * utils-microbenchmark
 * %%
 * Copyright (C) 2016 - 2017 Gilandel
 * %%
 * Authors: Gilles Landel
 * URL: https://github.com/Gilandel
 * 
 * This file is under Apache License, version 2.0 (2004).
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
import java.util.stream.DoubleStream;

import org.openjdk.jmh.results.BenchmarkResult;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;
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

    protected static final int WARMUP_ITERATIONS = 3;
    protected static final int MEASURE_ITERATIONS = 5;
    protected static final int NUM_FORKS = 1;

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
                .warmupIterations(this.getWarmupIterations())
                .measurementIterations(this.getMeasureIterations())
                .forks(this.getNumForks());
        // @formatter:on

        this.addOptions(runnerOptions);

        final File file = new File(this.getOutputDirectory(), classPath + ".json");

        file.getParentFile().mkdirs();
        file.delete();

        runnerOptions.resultFormat(ResultFormatType.JSON);
        runnerOptions.result(file.getAbsolutePath());

        runnerOptions.verbosity(this.getVerboseMode());

        final Runner runner = new Runner(runnerOptions.build());

        final Collection<RunResult> runResults = runner.run();

        assertNotNull(runResults);
        assertNotEquals(0, runResults.size());

        for (final RunResult runResult : runResults) {
            assertEquals(this.getNumForks(), runResult.getBenchmarkResults().size());

            for (final BenchmarkResult result : runResult.getBenchmarkResults()) {
                assertEquals(this.getMeasureIterations(), result.getIterationResults().size());

                final DoubleStream.Builder scores = DoubleStream.builder();
                for (final IterationResult ir : result.getIterationResults()) {
                    scores.add(ir.getRawPrimaryResults().stream().mapToDouble((r) -> r.getScore()).average().getAsDouble());
                }

                final Double avgScore = scores.build().average().getAsDouble();
                LOGGER.info(String.format("[%s] score: %,.3f %s", result.getParams().getBenchmark(), avgScore, result.getScoreUnit()));

                final String onBadScore = String.format("[%s] Average score is lower than expected: %,.3f ops/s < %,.3f ops/s",
                        result.getParams().getBenchmark(), avgScore, this.getExpectedMinNbOpsPerSeconds());
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
     * @return the warmupIterations
     */
    protected int getWarmupIterations() {
        return AbstractMicrobenchmark.WARMUP_ITERATIONS;
    }

    /**
     * @return the measureIterations
     */
    protected int getMeasureIterations() {
        return AbstractMicrobenchmark.MEASURE_ITERATIONS;
    }

    /**
     * @return the numForks
     */
    protected int getNumForks() {
        return AbstractMicrobenchmark.NUM_FORKS;
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
}
