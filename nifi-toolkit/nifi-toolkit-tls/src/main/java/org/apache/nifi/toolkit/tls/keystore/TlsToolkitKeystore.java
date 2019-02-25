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

package org.apache.nifi.toolkit.tls.keystore;

import org.apache.nifi.toolkit.tls.configuration.KeystoreConfig;
import org.apache.nifi.toolkit.tls.util.OutputStreamFactory;
import org.apache.nifi.toolkit.tls.util.TlsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.HashMap;

public class TlsToolkitKeystore {
    public static final String NIFI_KEY = "nifi-key";
    public static final String NIFI_CERT = "nifi-cert";
    public static final String NIFI_PROPERTIES = "nifi.properties";

    private final Logger logger = LoggerFactory.getLogger(TlsToolkitKeystore.class);
    private final OutputStreamFactory outputStreamFactory;

    public TlsToolkitKeystore() {
        this(FileOutputStream::new);
    }

    public TlsToolkitKeystore(OutputStreamFactory outputStreamFactory) {
        this.outputStreamFactory = outputStreamFactory;
    }

    private void splitKeystore(KeyStore keyStore, char[] keystorePassphrase, char[] keyPassphrase, File outputDirectory) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
        HashMap<String, Certificate> certificates = TlsHelper.extractCerts(keyStore);
        HashMap<String, Key> keys = TlsHelper.extractKeys(keyStore, keyPassphrase);
        TlsHelper.outputCertsAsPem(certificates, outputDirectory, ".crt");
        TlsHelper.outputKeysAsPem(keys, outputDirectory, ".key");
    }

    public void splitKeystore(KeystoreConfig keystoreConfig) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(keystoreConfig.getKeyStore()), keystoreConfig.getKeyStorePassword());
        splitKeystore(keyStore, keystoreConfig.getKeyStorePassword(), keystoreConfig.getKeyPassword(), keystoreConfig.getOutputDir());
    }

}
