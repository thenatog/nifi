/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.processors.geth;

import org.apache.nifi.util.MockProcessContext;
import org.apache.nifi.util.MockProcessorInitializationContext;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertNotNull;


public class GetGethTransactionsTest {

    private TestRunner runner;
    protected GetGethTransactions proc;

    @Before
    public void init() {
        runner = TestRunners.newTestRunner(GetGethTransactions.class);
        proc = new GetGethTransactions();


        MockProcessContext context = new MockProcessContext(proc);
        MockProcessorInitializationContext initContext = new MockProcessorInitializationContext(proc, context);
        proc.initialize(initContext);

        assertNotNull(proc.getSupportedPropertyDescriptors());
        runner = TestRunners.newTestRunner(proc);

    }

    @Test
    public void checkWeb3jVersion() {
        runner.enqueue("test content".getBytes(StandardCharsets.UTF_8));
        runner.run();
        runner.assertAllFlowFilesTransferred(proc.REL_SUCCESS.getName(), 1);
    }

}
