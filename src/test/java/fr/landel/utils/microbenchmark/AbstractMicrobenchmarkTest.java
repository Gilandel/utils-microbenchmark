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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Check {@link AbstractMicrobenchmark}
 *
 * @since Feb 28, 2017
 * @author Gilles
 *
 */
@State(Scope.Benchmark)
public class AbstractMicrobenchmarkTest extends AbstractMicrobenchmark {

    private double expected = 1_000d;

    @Override
    protected double getExpectedMinNbOpsPerSeconds() {
        return this.expected;
    }

    /**
     * Test method for {@link String#format}.
     */
    @Benchmark
    public void perfFormat() {
        // last comparison
        // StringUtils.inject: 570 449,731 ops/s
        // String.format: 140 260,855 ops/s

        String.format("I'll go to the beach this afternoon");
        String.format("I'll go to %%s %%4$s %%s %%3$s");
        String.format("I'll go to %s %4$s %s %3$s", "the", "this", "afternoon", "beach");
        String.format("I'll go to %%4$s %s %3$s%%s %%5$s %%text", "the", "this", "afternoon", "beach");
    }

    /**
     * Test method for {@link AbstractMicrobenchmark#run()}.
     */
    @Test
    public void testRunSuper() throws IOException, RunnerException {
        assertNotNull(super.run());

        this.expected = 1_000_000_000d;

        try {
            super.run();
            fail();
        } catch (AssertionError e) {
            assertNotNull(e);
            assertThat(e.getMessage(), Matchers.containsString(
                    "[fr.landel.utils.microbenchmark.AbstractMicrobenchmarkTest.perfFormat] Average score is lower than expected:"));
        }
    }
}
