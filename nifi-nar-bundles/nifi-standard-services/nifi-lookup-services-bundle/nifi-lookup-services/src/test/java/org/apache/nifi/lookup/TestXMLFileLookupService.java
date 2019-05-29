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
package org.apache.nifi.lookup;

import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TestXMLFileLookupService {

    final static Optional<String> EMPTY_STRING = Optional.empty();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testXMLFileLookupService() throws InitializationException, LookupFailureException {
        final TestRunner runner = TestRunners.newTestRunner(TestProcessor.class);
        final XMLFileLookupService service = new XMLFileLookupService();

        runner.addControllerService("xml-file-lookup-service", service);
        runner.setProperty(service, XMLFileLookupService.CONFIGURATION_FILE, "src/test/resources/test.xml");
        runner.enableControllerService(service);
        runner.assertValid(service);

        final XMLFileLookupService lookupService =
            (XMLFileLookupService) runner.getProcessContext()
                .getControllerServiceLookup()
                .getControllerService("xml-file-lookup-service");

        assertThat(lookupService, instanceOf(LookupService.class));

        final Optional<String> property1 = lookupService.lookup(Collections.singletonMap("key", "properties.property(0)"));
        assertEquals(Optional.of("this is property 1"), property1);

        final Optional<String> property2 = lookupService.lookup(Collections.singletonMap("key", "properties.property(1)"));
        assertEquals(Optional.of("this is property 2"), property2);

        final Optional<String> property3 = lookupService.lookup(Collections.singletonMap("key", "properties.property(2)[@value]"));
        assertEquals(Optional.of("this is property 3"), property3);

        final Optional<String> property4 = lookupService.lookup(Collections.singletonMap("key", "properties.property(3)"));
        assertEquals(EMPTY_STRING, property4);
    }

    @Test
    public void testXXEProtection() throws InitializationException {

        // Arrange
        //expectedException.expectCause(isA(SAXParseException.class));
        //expectedException.expectMessage("DOCTYPE is disallowed when the feature \"http://apache.org/xml/features/disallow-doctype-decl\" set to true.");
        try {
            System.out.println("Nathan Debug 1");
            final TestRunner runner = TestRunners.newTestRunner(TestProcessor.class);
            final XMLFileLookupService service = new XMLFileLookupService();
            System.out.println("Nathan Debug 2");
            runner.addControllerService("xml-file-lookup-service", service);
        // Act

            runner.setProperty(service, XMLFileLookupService.CONFIGURATION_FILE, "src/test/resources/test-xxe.xml");
            System.out.println("Nathan Debug 3");
            runner.enableControllerService(service);
            System.out.println("Nathan Debug 4");
            runner.assertValid(service);
            System.out.println("Nathan Debug 5");


                final XMLFileLookupService lookupService =
                        (XMLFileLookupService) runner.getProcessContext()
                                .getControllerServiceLookup()
                                .getControllerService("xml-file-lookup-service");
        } catch (final AssertionError e) {

            System.out.println("########################################");
            System.out.println("HI NATHAN " + e.getCause());
        }

        // Assert
        // Expect parsing exception


    }

}
