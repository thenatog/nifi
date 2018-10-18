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
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Security;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;

/**
 * A filter to apply the Content Security Policy (which supersedes the X-Frame-Options header).
 *
 */
public class ContentSecurityPolicyFilter implements Filter {
    private static final String CSP = "Content-Security-Policy";
    private static final String CSPvalue = "frame-ancestors 'self'";

    private static final String XXSS = "X-XSS-Protection";
    private static final String XXSSvalue = "1; mode=block";

    private static final String XCTO = "X-Content-Type-Options";
    private static final String XCTOvalue = "nosniff";

    private static final String HSTS = "Strict-Transport-Security";
    private static final String HSTSvalue = "max-age=31540000";

    private static final Logger logger = LoggerFactory.getLogger(ContentSecurityPolicyFilter.class);

    @Override
    public void doFilter(final ServletRequest req, final ServletResponse resp, final FilterChain filterChain)
            throws IOException, ServletException {

        final HttpServletResponse response = (HttpServletResponse) resp;
        response.setHeader(CSP, CSPvalue);
        response.setHeader(XXSS, XXSSvalue);
        response.setHeader(XCTO, XCTOvalue);
        response.setHeader(HSTS, HSTSvalue);


        filterChain.doFilter(req, resp);
    }

    @Override
    public void init(final FilterConfig config) {
    }

    @Override
    public void destroy() {
    }
}