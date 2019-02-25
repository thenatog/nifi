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

import org.apache.commons.cli.CommandLine;

import org.apache.nifi.toolkit.tls.commandLine.BaseCommandLine;
import org.apache.nifi.toolkit.tls.commandLine.CommandLineParseException;
import org.apache.nifi.toolkit.tls.commandLine.ExitCode;
import org.apache.nifi.toolkit.tls.configuration.KeystoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Command line parser for a StandaloneConfig object and a main entry point to invoke the parser and run the standalone generator
 */
public class TlsToolkitKeystoreCommandLine extends BaseCommandLine {
    public static final String OUTPUT_DIRECTORY_ARG = "outputDirectory";
    public static final String KEY_STORE_PASSWORD_ARG = "keyStorePassword";
    public static final String KEY_PASSWORD_ARG = "keyPassword";
    public static final String KEY_STORE_FILENAME_ARG = "keyStoreFilename";

    public static final String DEFAULT_OUTPUT_DIRECTORY = calculateDefaultOutputDirectory(Paths.get("."));

    protected static String calculateDefaultOutputDirectory(Path currentPath) {
        Path currentAbsolutePath = currentPath.toAbsolutePath();
        Path parent = currentAbsolutePath.getParent();
        if (currentAbsolutePath.getRoot().equals(parent)) {
            return parent.toString();
        } else {
            Path currentNormalizedPath = currentAbsolutePath.normalize();
            return "../" + currentNormalizedPath.getFileName().toString();
        }
    }

    public static final String DESCRIPTION = "Ingests a JKS and outputs the certs and keys unencrypted.";

    private final Logger logger = LoggerFactory.getLogger(TlsToolkitKeystoreCommandLine.class);
    private File outputDir;
    private char[] keyStorePassword;
    private char[] keyPassword;
    private File keyStore;

    protected TlsToolkitKeystoreCommandLine() {
        super(DESCRIPTION);
        addOptionWithArg("o", OUTPUT_DIRECTORY_ARG, "The directory to output cert and key files.", DEFAULT_OUTPUT_DIRECTORY);
        addOptionWithArg("f", KEY_STORE_FILENAME_ARG, "The keystore file to split into certificates and keys in PEM format.");
        addOptionWithArg("S", KEY_STORE_PASSWORD_ARG, "Keystore password to decrypt password protected keystore.");
        addOptionWithArg("K", KEY_PASSWORD_ARG, "Key password to decrypt private keys contained in the keystore..");
    }

    public static void main(String[] args) {
        TlsToolkitKeystoreCommandLine tlsToolkitKeystoreCommandLine = new TlsToolkitKeystoreCommandLine();
        try {
            tlsToolkitKeystoreCommandLine.parse(args);
        } catch (CommandLineParseException e) {
            System.exit(e.getExitCode().ordinal());
        }
        try {
            new TlsToolkitKeystore().splitKeystore(tlsToolkitKeystoreCommandLine.createConfig());
        } catch (Exception e) {
            tlsToolkitKeystoreCommandLine.printUsage("Error generating TLS configuration. (" + e.getMessage() + ")");
            System.exit(ExitCode.ERROR_GENERATING_CONFIG.ordinal());
        }
        System.exit(ExitCode.SUCCESS.ordinal());
    }

    @Override
    protected CommandLine doParse(String... args) throws CommandLineParseException {
        CommandLine commandLine = super.doParse(args);
        String outputDirectory = commandLine.getOptionValue(OUTPUT_DIRECTORY_ARG, DEFAULT_OUTPUT_DIRECTORY);
        String keyStoreFilename = commandLine.getOptionValue(KEY_STORE_FILENAME_ARG);
        outputDir = new File(outputDirectory);
        keyStore = new File(keyStoreFilename);
        keyPassword = commandLine.getOptionValue(KEY_PASSWORD_ARG).toCharArray();
        keyStorePassword = commandLine.getOptionValue(KEY_STORE_PASSWORD_ARG).toCharArray();

        return commandLine;
    }

    /**
     * Creates the StandaloneConfig for use in running TlsToolkitStandalone
     *
     * @return the StandaloneConfig for use in running TlsToolkitStandalone
     */
    public KeystoreConfig createConfig() {
        KeystoreConfig keystoreConfig = new KeystoreConfig();
        keystoreConfig.setOutputDir(outputDir);
        keystoreConfig.setKeyStore(keyStore);
        keystoreConfig.setKeyPassword(keyPassword);
        keystoreConfig.setKeyStorePassword(keyStorePassword);

        keystoreConfig.initDefaults();

        return keystoreConfig;
    }
}
