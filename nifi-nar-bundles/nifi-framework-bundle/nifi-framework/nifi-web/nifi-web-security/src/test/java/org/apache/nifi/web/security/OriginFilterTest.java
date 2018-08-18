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
package org.apache.nifi.web.security;

import org.apache.nifi.util.StringUtils;
import org.eclipse.jetty.servlet.FilterHolder;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.junit.Assert.assertEquals;

public class OriginFilterTest {

    @Test
    public void testSameSourceOriginAndTargetOrigin() throws ServletException, IOException {
        // Arrange

        // OriginFilter
        FilterHolder originFilter = new FilterHolder(new OriginFilter("http://localhost:8080"));

        // Set up request
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getRequestURI()).thenReturn("/");
        Mockito.when(mockRequest.getHeader("Origin")).thenReturn("http://localhost:8080");
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        FilterChain mockFilterChain = Mockito.mock(FilterChain.class);

        // Action
        originFilter.getFilter().doFilter(mockRequest, mockResponse, mockFilterChain);

        // Verify
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
    }

    @Test
    public void testDifferentSourceOriginPort() throws ServletException, IOException {
        // Arrange

        // OriginFilter
        FilterHolder originFilter = new FilterHolder(new OriginFilter("http://localhost:8080"));

        // Set up request
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getRequestURI()).thenReturn("/");
        Mockito.when(mockRequest.getHeader("Origin")).thenReturn("http://localhost:1111");
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        FilterChain mockFilterChain = Mockito.mock(FilterChain.class);

        // Action
        originFilter.getFilter().doFilter(mockRequest, mockResponse, mockFilterChain);

        // Verify
        assertEquals(mockResponse.getStatus(), HttpServletResponse.SC_FORBIDDEN);
        assertTrue(!StringUtils.isBlank(mockResponse.getErrorMessage()));
    }

    @Test
    public void testDifferentHostname() throws ServletException, IOException {
        // Arrange

        // OriginFilter
        FilterHolder originFilter = new FilterHolder(new OriginFilter("http://localhost:8080"));

        // Set up request
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getRequestURI()).thenReturn("/");
        Mockito.when(mockRequest.getHeader("Origin")).thenReturn("http://nifi.com:8080");
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        FilterChain mockFilterChain = Mockito.mock(FilterChain.class);

        // Action
        originFilter.getFilter().doFilter(mockRequest, mockResponse, mockFilterChain);

        // Verify
        assertEquals(mockResponse.getStatus(), HttpServletResponse.SC_FORBIDDEN);
        assertTrue(!StringUtils.isBlank(mockResponse.getErrorMessage()));
    }


    @Test
    public void testDifferentScheme() throws ServletException, IOException {
        // Arrange

        // OriginFilter
        FilterHolder originFilter = new FilterHolder(new OriginFilter("http://localhost:8080"));

        // Set up request
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getRequestURI()).thenReturn("/");
        Mockito.when(mockRequest.getHeader("Origin")).thenReturn("https://localhost:8080");
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        FilterChain mockFilterChain = Mockito.mock(FilterChain.class);

        // Action
        originFilter.getFilter().doFilter(mockRequest, mockResponse, mockFilterChain);

        // Verify
        assertEquals(mockResponse.getStatus(), HttpServletResponse.SC_FORBIDDEN);
        assertTrue(!StringUtils.isBlank(mockResponse.getErrorMessage()));
    }

    @Test
    public void testMatchingSourceReferrerAndTargetOrigin() throws ServletException, IOException {
        // Arrange

        // OriginFilter
        FilterHolder originFilter = new FilterHolder(new OriginFilter("http://localhost:8080"));

        // Set up request
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getRequestURI()).thenReturn("/");
        Mockito.when(mockRequest.getHeader("Referer")).thenReturn("http://localhost:8080/nifi");
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        FilterChain mockFilterChain = Mockito.mock(FilterChain.class);


        // Action
        originFilter.getFilter().doFilter(mockRequest, mockResponse, mockFilterChain);

        // Verify
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
    }

    @Test
    public void testNonMatchingSourceReferrerDomainAndTargetOrigin() throws ServletException, IOException {
        // Arrange

        // OriginFilter
        FilterHolder originFilter = new FilterHolder(new OriginFilter("http://localhost:8080"));

        // Set up request
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getRequestURI()).thenReturn("/");
        Mockito.when(mockRequest.getHeader("Referer")).thenReturn("http://nifi.com:8080/nifi");
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        FilterChain mockFilterChain = Mockito.mock(FilterChain.class);

        // Action
        originFilter.getFilter().doFilter(mockRequest, mockResponse, mockFilterChain);

        // Verify
        assertEquals(mockResponse.getStatus(), HttpServletResponse.SC_FORBIDDEN);
        assertTrue(!StringUtils.isBlank(mockResponse.getErrorMessage()));
    }

    @Test
    public void testNonMatchingSourceReferrerSchemeAndTargetOrigin() throws ServletException, IOException {
        // Arrange

        // OriginFilter
        FilterHolder originFilter = new FilterHolder(new OriginFilter("http://localhost:8080"));

        // Set up request
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getRequestURI()).thenReturn("/");
        Mockito.when(mockRequest.getHeader("Referer")).thenReturn("https://localhost:8080/nifi");
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        FilterChain mockFilterChain = Mockito.mock(FilterChain.class);

        // Action
        originFilter.getFilter().doFilter(mockRequest, mockResponse, mockFilterChain);

        // Verify
        assertEquals(mockResponse.getStatus(), HttpServletResponse.SC_FORBIDDEN);
        assertTrue(!StringUtils.isBlank(mockResponse.getErrorMessage()));
    }

    @Test
    public void testSourceOriginNullAndSourceRefererNull() throws ServletException, IOException {
        // Arrange

        // OriginFilter
        FilterHolder originFilter = new FilterHolder(new OriginFilter("http://localhost:8080"));

        // Set up request
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getRequestURI()).thenReturn("/");
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        FilterChain mockFilterChain = Mockito.mock(FilterChain.class);

        // Action
        originFilter.getFilter().doFilter(mockRequest, mockResponse, mockFilterChain);

        // Verify
        assertEquals(mockResponse.getStatus(), HttpServletResponse.SC_FORBIDDEN);
        assertTrue(!StringUtils.isBlank(mockResponse.getErrorMessage()));
    }



}
