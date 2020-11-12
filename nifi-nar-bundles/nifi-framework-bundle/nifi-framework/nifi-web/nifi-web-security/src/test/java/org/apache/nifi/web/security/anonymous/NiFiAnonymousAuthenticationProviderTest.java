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
package org.apache.nifi.web.security.anonymous;

import org.apache.nifi.authorization.Authorizer;
import org.apache.nifi.authorization.user.NiFiUserDetails;
import org.apache.nifi.util.NiFiProperties;
import org.apache.nifi.util.StringUtils;
import org.apache.nifi.web.security.InvalidAuthenticationException;
import org.apache.nifi.web.security.token.NiFiAuthenticationToken;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NiFiAnonymousAuthenticationProviderTest {

    private static final Logger logger = LoggerFactory.getLogger(NiFiAnonymousAuthenticationProviderTest.class);
    private static final boolean IS_NOT_STATIC_RESOURCE = false;
    private static final boolean IS_STATIC_RESOURCE = false;

    @Test
    public void testAnonymousDisabledNotSecure() throws Exception {
        final NiFiProperties nifiProperties = Mockito.mock(NiFiProperties.class);
        when(nifiProperties.isAnonymousAuthenticationAllowed()).thenReturn(false);

        final NiFiAnonymousAuthenticationProvider anonymousAuthenticationProvider = new NiFiAnonymousAuthenticationProvider(nifiProperties, mock(Authorizer.class));

        final NiFiAnonymousAuthenticationRequestToken authenticationRequest = new NiFiAnonymousAuthenticationRequestToken(false, StringUtils.EMPTY, IS_NOT_STATIC_RESOURCE);

        final NiFiAuthenticationToken authentication = (NiFiAuthenticationToken) anonymousAuthenticationProvider.authenticate(authenticationRequest);
        final NiFiUserDetails userDetails = (NiFiUserDetails) authentication.getDetails();
        assertTrue(userDetails.getNiFiUser().isAnonymous());
    }

    @Test
    public void testAnonymousEnabledNotSecure() throws Exception {
        final NiFiProperties nifiProperties = Mockito.mock(NiFiProperties.class);
        when(nifiProperties.isAnonymousAuthenticationAllowed()).thenReturn(true);

        final NiFiAnonymousAuthenticationProvider anonymousAuthenticationProvider = new NiFiAnonymousAuthenticationProvider(nifiProperties, mock(Authorizer.class));

        final NiFiAnonymousAuthenticationRequestToken authenticationRequest = new NiFiAnonymousAuthenticationRequestToken(false, StringUtils.EMPTY, IS_NOT_STATIC_RESOURCE);

        final NiFiAuthenticationToken authentication = (NiFiAuthenticationToken) anonymousAuthenticationProvider.authenticate(authenticationRequest);
        final NiFiUserDetails userDetails = (NiFiUserDetails) authentication.getDetails();
        assertTrue(userDetails.getNiFiUser().isAnonymous());
    }

    @Test(expected = InvalidAuthenticationException.class)
    public void testAnonymousDisabledSecure() throws Exception {
        final NiFiProperties nifiProperties = Mockito.mock(NiFiProperties.class);
        when(nifiProperties.isAnonymousAuthenticationAllowed()).thenReturn(false);

        final NiFiAnonymousAuthenticationProvider anonymousAuthenticationProvider = new NiFiAnonymousAuthenticationProvider(nifiProperties, mock(Authorizer.class));

        final NiFiAnonymousAuthenticationRequestToken authenticationRequest = new NiFiAnonymousAuthenticationRequestToken(true, StringUtils.EMPTY, IS_NOT_STATIC_RESOURCE);

        anonymousAuthenticationProvider.authenticate(authenticationRequest);
    }

    @Test
    public void testAnonymousEnabledSecure() throws Exception {
        final NiFiProperties nifiProperties = Mockito.mock(NiFiProperties.class);
        when(nifiProperties.isAnonymousAuthenticationAllowed()).thenReturn(true);

        final NiFiAnonymousAuthenticationProvider anonymousAuthenticationProvider = new NiFiAnonymousAuthenticationProvider(nifiProperties, mock(Authorizer.class));

        final NiFiAnonymousAuthenticationRequestToken authenticationRequest = new NiFiAnonymousAuthenticationRequestToken(true, StringUtils.EMPTY, IS_NOT_STATIC_RESOURCE);

        final NiFiAuthenticationToken authentication = (NiFiAuthenticationToken) anonymousAuthenticationProvider.authenticate(authenticationRequest);
        final NiFiUserDetails userDetails = (NiFiUserDetails) authentication.getDetails();
        assertTrue(userDetails.getNiFiUser().isAnonymous());
    }

    @Test
    public void testAnonymousDisabledNotSecureIsStaticResource() throws Exception {
        final NiFiProperties nifiProperties = Mockito.mock(NiFiProperties.class);
        when(nifiProperties.isAnonymousAuthenticationAllowed()).thenReturn(false);

        final NiFiAnonymousAuthenticationProvider anonymousAuthenticationProvider = new NiFiAnonymousAuthenticationProvider(nifiProperties, mock(Authorizer.class));

        final NiFiAnonymousAuthenticationRequestToken authenticationRequest = new NiFiAnonymousAuthenticationRequestToken(false, StringUtils.EMPTY, true);

        final NiFiAuthenticationToken authentication = (NiFiAuthenticationToken) anonymousAuthenticationProvider.authenticate(authenticationRequest);
        final NiFiUserDetails userDetails = (NiFiUserDetails) authentication.getDetails();
        assertTrue(userDetails.getNiFiUser().isAnonymous());
    }

    @Test
    public void testAnonymousDisabledSecureIsStaticResource() throws Exception {
        final NiFiProperties nifiProperties = Mockito.mock(NiFiProperties.class);
        when(nifiProperties.isAnonymousAuthenticationAllowed()).thenReturn(false);

        final NiFiAnonymousAuthenticationProvider anonymousAuthenticationProvider = new NiFiAnonymousAuthenticationProvider(nifiProperties, mock(Authorizer.class));

        final NiFiAnonymousAuthenticationRequestToken authenticationRequest = new NiFiAnonymousAuthenticationRequestToken(true, StringUtils.EMPTY, true);

        final NiFiAuthenticationToken authentication = (NiFiAuthenticationToken) anonymousAuthenticationProvider.authenticate(authenticationRequest);
        final NiFiUserDetails userDetails = (NiFiUserDetails) authentication.getDetails();
        assertTrue(userDetails.getNiFiUser().isAnonymous());
    }

    @Test
    public void testAnonymousEnabledSecureIsStaticResource() throws Exception {
        final NiFiProperties nifiProperties = Mockito.mock(NiFiProperties.class);
        when(nifiProperties.isAnonymousAuthenticationAllowed()).thenReturn(true);

        final NiFiAnonymousAuthenticationProvider anonymousAuthenticationProvider = new NiFiAnonymousAuthenticationProvider(nifiProperties, mock(Authorizer.class));

        final NiFiAnonymousAuthenticationRequestToken authenticationRequest = new NiFiAnonymousAuthenticationRequestToken(true, StringUtils.EMPTY, true);

        final NiFiAuthenticationToken authentication = (NiFiAuthenticationToken) anonymousAuthenticationProvider.authenticate(authenticationRequest);
        final NiFiUserDetails userDetails = (NiFiUserDetails) authentication.getDetails();
        assertTrue(userDetails.getNiFiUser().isAnonymous());
    }
}
