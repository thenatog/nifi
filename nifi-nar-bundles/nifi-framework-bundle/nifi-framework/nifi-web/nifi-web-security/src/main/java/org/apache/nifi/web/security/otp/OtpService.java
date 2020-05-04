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
package org.apache.nifi.web.security.otp;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.codec.binary.Base64;
import org.apache.nifi.web.security.token.OtpAuthenticationToken;
import org.apache.nifi.web.security.util.CacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * OtpService is a service for generating and verifying one time password tokens.
 */
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);

    private static final String HMAC_SHA256 = "HmacSHA256";

    // protected for testing purposes
    protected static final int MAX_CACHE_SOFT_LIMIT = 100;

    private final Cache<CacheKey, String> downloadTokensToUsers;
    private final Cache<CacheKey, String> uiExtensionTokensToUsers;

    // keep a reverse cache to allow look-ups in both directions
    private final Cache<String, CacheKey> usersToDownloadTokens;
    private final Cache<String, CacheKey> usersToUiExtensionTokens;

    /**
     * Creates a new OtpService with an expiration of 5 minutes.
     */
    public OtpService() {
        this(5, TimeUnit.MINUTES);
    }

    /**
     * Creates a new OtpService.
     *
     * @param duration                  The expiration duration
     * @param units                     The expiration units
     * @throws NullPointerException     If units is null
     * @throws IllegalArgumentException If duration is negative
     */
    public OtpService(final int duration, final TimeUnit units) {
        downloadTokensToUsers = CacheBuilder.newBuilder().expireAfterWrite(duration, units).build();
        uiExtensionTokensToUsers = CacheBuilder.newBuilder().expireAfterWrite(duration, units).build();
        usersToDownloadTokens = CacheBuilder.newBuilder().expireAfterWrite(duration, units).build();
        usersToUiExtensionTokens = CacheBuilder.newBuilder().expireAfterWrite(duration, units).build();
    }

    /**
     * Generates a download token for the specified authentication.
     *
     * @param authenticationToken       The authentication
     * @return                          The one time use download token
     */
    public String generateDownloadToken(final OtpAuthenticationToken authenticationToken) {
        // ? downloadTokensToUsers.cleanUp();
        return generateToken(downloadTokensToUsers.asMap(), usersToDownloadTokens.asMap(), authenticationToken);
    }

    /**
     * Gets the authenticated identity from the specified one time use download token. This method will not return null.
     *
     * @param token                     The one time use download token
     * @return                          The authenticated identity
     * @throws OtpAuthenticationException   When the specified token does not correspond to an authenticated identity
     */
    public String getAuthenticationFromDownloadToken(final String token) throws OtpAuthenticationException {
        return getAuthenticationFromToken(downloadTokensToUsers.asMap(), usersToDownloadTokens.asMap(), token);
    }

    /**
     * Generates a UI extension token for the specified authentication.
     *
     * @param authenticationToken       The authentication
     * @return                          The one time use UI extension token
     */
    public String generateUiExtensionToken(final OtpAuthenticationToken authenticationToken) {
        return generateToken(uiExtensionTokensToUsers.asMap(), usersToUiExtensionTokens.asMap(), authenticationToken);
    }

    /**
     * Gets the authenticated identity from the specified one time use UI extension token. This method will not return null.
     *
     * @param token                     The one time use UI extension token
     * @return                          The authenticated identity
     * @throws OtpAuthenticationException   When the specified token does not correspond to an authenticated identity
     */
    public String getAuthenticationFromUiExtensionToken(final String token) throws OtpAuthenticationException {
        return getAuthenticationFromToken(uiExtensionTokensToUsers.asMap(), usersToUiExtensionTokens.asMap(), token);
    }

    /**
     * Generates a token and stores it in the specified cache.
     *
     * @param tokenCache                A cache that maps tokens to users
     * @param userCache                 A cache that maps users to tokens
     * @param authenticationToken       The authentication
     * @return                          The one time use token
     */
    private String generateToken(final ConcurrentMap<CacheKey, String> tokenCache, final ConcurrentMap<String, CacheKey> userCache, final OtpAuthenticationToken authenticationToken) {

        final String userId = (String) authenticationToken.getPrincipal();

        // If the user has a token already, return it
        if(userCache.containsKey(userId)) {
            return userCache.get(userId).getKey();
        } else {
            // Otherwise, we generate a token
            if (tokenCache.size() >= MAX_CACHE_SOFT_LIMIT) {
                throw new IllegalStateException("The maximum number of single use tokens have been issued.");
            }

            // Hash the authentication and build a cache key
            final CacheKey cacheKey = new CacheKey(hash(authenticationToken));

            // Store the token and user mappings in their respective caches
            tokenCache.putIfAbsent(cacheKey, userId);
            userCache.putIfAbsent(userId, cacheKey);

            // Return the token
            return cacheKey.getKey();
        }
    }

    /**
     * Gets the corresponding authentication for the specified one time use token. The specified token will be removed
     * from the token and user cache.
     *
     * @param tokenCache                A cache that maps tokens to users
     * @param userCache                 A cache that maps users to tokens
     * @param token                     The one time use token
     * @return                          The authenticated identity
     */
    private String getAuthenticationFromToken(final ConcurrentMap<CacheKey, String> tokenCache, final ConcurrentMap<String, CacheKey> userCache, final String token) throws OtpAuthenticationException {
        final String authenticatedUser = tokenCache.remove(new CacheKey(token));

        if (authenticatedUser == null) {
            throw new OtpAuthenticationException("Unable to validate the access token.");
        } else {
            userCache.remove(authenticatedUser);
        }

        return authenticatedUser;
    }

    /**
     * Hashes the specified authentication token. The resulting value will be used as the one time use token.
     *
     * @param authenticationToken   the authentication token
     * @return                      the one time use token
     */
    private String hash(final OtpAuthenticationToken authenticationToken) {
        try {
            // input is the user identity and timestamp
            final String input = authenticationToken.getName() + "-" + System.nanoTime();

            // create the secret using secure random
            final SecureRandom secureRandom = new SecureRandom();
            final byte[] randomBytes = new byte[32];
            secureRandom.nextBytes(randomBytes);
            final SecretKeySpec secret = new SecretKeySpec(randomBytes, HMAC_SHA256); // 256 bit

            // hash the input
            final Mac hmacSha256 = Mac.getInstance(HMAC_SHA256);
            hmacSha256.init(secret);
            final byte[] output = hmacSha256.doFinal(input.getBytes(StandardCharsets.UTF_8));

            // return the result as a base 64 string
            return Base64.encodeBase64URLSafeString(output);
        } catch (final NoSuchAlgorithmException | InvalidKeyException e) {
            final String errorMessage = "There was an error generating the OTP";
            logger.error(errorMessage, e);
            throw new IllegalStateException("Unable to generate single use token.");
        }
    }
}
