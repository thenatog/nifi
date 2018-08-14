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

import org.eclipse.jetty.servlet.FilterHolder;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import java.io.IOException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


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

    @Test
    public void testMatchingSourceReferrerAndTargetOrigin() throws ServletException, IOException {
        // Arrange

        // OriginFilter
        FilterHolder originFilter = new FilterHolder(new OriginFilter());

        // Set up request
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getRequestURI()).thenReturn("/");
        Mockito.when(mockRequest.getHeader("Referer")).thenReturn("http://localhost:8080/nifi");

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
    public void testNonMatchingSourceReferrerDomainAndTargetOrigin() throws ServletException, IOException {
        // Arrange

        // OriginFilter
        FilterHolder originFilter = new FilterHolder(new OriginFilter());

        // Set up request
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getRequestURI()).thenReturn("/");
        Mockito.when(mockRequest.getHeader("Referer")).thenReturn("http://nifi.com:8080/nifi");

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
    public void testNonMatchingSourceReferrerSchemeAndTargetOrigin() throws ServletException, IOException {
        // Arrange

        // OriginFilter
        FilterHolder originFilter = new FilterHolder(new OriginFilter());

        // Set up request
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getRequestURI()).thenReturn("/");
        Mockito.when(mockRequest.getHeader("Referer")).thenReturn("https://localhost:8080/nifi");

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
    public void testSourceOriginNullAndSourceRefererNull() throws ServletException, IOException {
        // Arrange

        // OriginFilter
        FilterHolder originFilter = new FilterHolder(new OriginFilter());

        // Set up request
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getRequestURI()).thenReturn("/");

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

}
