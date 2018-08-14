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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.ws.rs.BadRequestException;

/**
 * A filter to ensure that source origin and target origins match.
 *
 */
public class OriginFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(OriginFilter.class);
    private String targetOrigin;

    @Override
    public void doFilter(final ServletRequest req, final ServletResponse resp, final FilterChain filterChain)
            throws IOException, ServletException {


        HttpServletRequest httpReq = (HttpServletRequest) req;
        String sourceOrigin = httpReq.getHeader("Origin");

        if(sourceOrigin != null && targetOrigin != null) {
            // Target and Source Origins match so we can continue with the filter chain
            if(sourceOrigin.equalsIgnoreCase(targetOrigin)) {
                filterChain.doFilter(req, resp);
            } else {
                // Should this be a BadRequestException or simply a ServletException?
                throw new BadRequestException("The request's Origin header did not match the server's (target) Origin");
            }
        } else {
            throw new BadRequestException("The request's Origin header was null");
        }

//        try {
//
//        } catch (RequestRejectedException e) {
//            if (logger.isDebugEnabled()) {
//                logger.debug("An exception was caught performing the HTTP request security filter check and the stacktrace has been suppressed from the response");
//            }
//
//            HttpServletResponse filteredResponse = (HttpServletResponse) resp;
//            filteredResponse.setStatus(500);
//            filteredResponse.getWriter().write(e.getMessage());
//
//            StringWriter sw = new StringWriter();
//            sw.write("Exception caught by ExceptionFilter:\n");
//            PrintWriter pw = new PrintWriter(sw);
//            e.printStackTrace(pw);
//            logger.error(sw.toString());
//        }

    }

    @Override
    public void init(final FilterConfig config) {
        targetOrigin = config.getInitParameter("JETTY_ORIGIN");

    }

    @Override
    public void destroy() {
    }
}