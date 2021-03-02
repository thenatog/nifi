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
package org.apache.nifi.encrypt;

import org.apache.nifi.security.util.crypto.SecureHasher;
import org.apache.nifi.security.util.crypto.SecureHasherFactory;
import org.apache.nifi.util.NiFiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

/**
 * Encode a sensitive value using the NiFi sensitive properties key to derive the secret key used in the MAC operation.
 */
public class StandardSensitiveValueEncoder implements SensitiveValueEncoder {

    private static final Logger logger = LoggerFactory.getLogger(StandardSensitiveValueEncoder.class);

    private byte[] hashedSensitivePropertyKey;
    private String HMAC_SHA256 = "HmacSHA256";

    public StandardSensitiveValueEncoder(final NiFiProperties properties) {
        this(properties.getProperty(NiFiProperties.SENSITIVE_PROPS_KEY),
                SecureHasherFactory.getSecureHasher(properties.getProperty(NiFiProperties.SENSITIVE_PROPS_ALGORITHM)));
    }

    // We use the sensitive properties key and a SecureHasher impl to derive a secret key for the getEncoded() method
    public StandardSensitiveValueEncoder(final String sensitivePropertiesKey, final SecureHasher hasher) {
        Objects.requireNonNull(sensitivePropertiesKey, "Sensitive Properties Key is required");
        Objects.requireNonNull(hasher, "SecureHasher is required");
        hashedSensitivePropertyKey = hasher.hashRaw(sensitivePropertiesKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates a securely-derived, deterministic representation of the provided decrypted NiFi property value
     * for logging/comparison purposes. A SecureHasher implementation is used to derive a secret key from the sensitive which is
     * then used to generate an HMAC using HMAC+SHA256.
     *
     * @param plaintextSensitivePropertyValue A decrypted, sensitive property value
     *
     * @return a deterministic, securely hashed representation of the value which will be consistent across nodes. Safe to print in a log.
     */
    @Override
    public String getEncoded(final String plaintextSensitivePropertyValue) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(hashedSensitivePropertyKey, "HmacSHA256"));
            byte[] hashedBytes = mac.doFinal(plaintextSensitivePropertyValue.getBytes(StandardCharsets.UTF_8));
            return "[MASKED] (" + Base64.getEncoder().encodeToString(hashedBytes) + ")";
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Encountered an error making the sensitive value loggable: {}", e.getLocalizedMessage());
            return "[Unable to mask value]";
        }
    }
}
