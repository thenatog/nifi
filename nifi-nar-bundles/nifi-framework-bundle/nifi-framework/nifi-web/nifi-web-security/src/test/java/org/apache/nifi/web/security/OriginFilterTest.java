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

import org.apache.nifi.authorization.Authorizer;
import org.apache.nifi.authorization.util.IdentityMapping;
import org.apache.nifi.util.NiFiProperties;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import org.springframework.mock.web.MockHttpServletResponse;

import org.eclipse.jetty.servlet.FilterHolder;


public class OriginFilterTest {

    @Test
    public void testSameSourceOriginAndTargetOrigin() throws ServletException, IOException {
        // Arrange

        // OriginFilter
        FilterHolder originFilter = new FilterHolder(new OriginFilter());

        // Set up request
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getRequestURI()).thenReturn("/");
        Mockito.when(mockRequest.getHeader("Origin")).thenReturn("http://localhost:8080");

        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        FilterChain mockFilterChain = Mockito.mock(FilterChain.class);
        ServletContext mockContext = Mockito.mock(ServletContext.class);

        // Set up filter config
        FilterConfig mockFilterConfig = Mockito.mock(FilterConfig.class);
        when(mockFilterConfig.getInitParameter("JETTY_ORIGIN")).thenReturn("http://localhost:8080");
        when(mockFilterConfig.getServletContext()).thenReturn(mockContext);
        originFilter.getFilter().init(mockFilterConfig);

        // Action
        originFilter.getFilter().doFilter(mockRequest, mockResponse, mockFilterChain);

        // Verify
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
    }

    @Test(expected = BadRequestException.class)
    public void testDifferentSourceOriginPort() throws ServletException, IOException {
        // Arrange

        // OriginFilter
        FilterHolder originFilter = new FilterHolder(new OriginFilter());

        // Set up request
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getRequestURI()).thenReturn("/");
        Mockito.when(mockRequest.getHeader("Origin")).thenReturn("http://localhost:1111");

        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        FilterChain mockFilterChain = Mockito.mock(FilterChain.class);
        ServletContext mockContext = Mockito.mock(ServletContext.class);

        // Set up filter config
        FilterConfig mockFilterConfig = Mockito.mock(FilterConfig.class);
        when(mockFilterConfig.getInitParameter("JETTY_ORIGIN")).thenReturn("http://localhost:8080");
        when(mockFilterConfig.getServletContext()).thenReturn(mockContext);
        originFilter.getFilter().init(mockFilterConfig);

        // Action
        originFilter.getFilter().doFilter(mockRequest, mockResponse, mockFilterChain);

        // Verify
        // BadRequestException should be thrown
    }

    @Test(expected = BadRequestException.class)
    public void testDifferentHostname() throws ServletException, IOException {
        // Arrange

        // OriginFilter
        FilterHolder originFilter = new FilterHolder(new OriginFilter());

        // Set up request
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getRequestURI()).thenReturn("/");
        Mockito.when(mockRequest.getHeader("Origin")).thenReturn("http://nifi.com:8080");

        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        FilterChain mockFilterChain = Mockito.mock(FilterChain.class);
        ServletContext mockContext = Mockito.mock(ServletContext.class);

        // Set up filter config
        FilterConfig mockFilterConfig = Mockito.mock(FilterConfig.class);
        when(mockFilterConfig.getInitParameter("JETTY_ORIGIN")).thenReturn("http://localhost:8080");
        when(mockFilterConfig.getServletContext()).thenReturn(mockContext);
        originFilter.getFilter().init(mockFilterConfig);

        // Action
        originFilter.getFilter().doFilter(mockRequest, mockResponse, mockFilterChain);

        // Verify
        // BadRequestException should be thrown
    }


    @Test(expected = BadRequestException.class)
    public void testDifferentScheme() throws ServletException, IOException {
        // Arrange

        // OriginFilter
        FilterHolder originFilter = new FilterHolder(new OriginFilter());

        // Set up request
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getRequestURI()).thenReturn("/");
        Mockito.when(mockRequest.getHeader("Origin")).thenReturn("https://localhost:8080");

        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        FilterChain mockFilterChain = Mockito.mock(FilterChain.class);
        ServletContext mockContext = Mockito.mock(ServletContext.class);

        // Set up filter config
        FilterConfig mockFilterConfig = Mockito.mock(FilterConfig.class);
        when(mockFilterConfig.getInitParameter("JETTY_ORIGIN")).thenReturn("http://localhost:8080");
        when(mockFilterConfig.getServletContext()).thenReturn(mockContext);
        originFilter.getFilter().init(mockFilterConfig);

        // Action
        originFilter.getFilter().doFilter(mockRequest, mockResponse, mockFilterChain);

        // Verify
        // BadRequestException should be thrown
    }


}
