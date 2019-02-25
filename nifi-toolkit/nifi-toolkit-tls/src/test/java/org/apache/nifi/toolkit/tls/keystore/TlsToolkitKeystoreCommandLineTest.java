package org.apache.nifi.toolkit.tls.keystore;

import org.apache.nifi.toolkit.tls.commandLine.CommandLineParseException;
import org.apache.nifi.toolkit.tls.commandLine.ExitCode;
import org.apache.nifi.toolkit.tls.util.PasswordUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.stubbing.defaultanswers.ForwardsInvocations;

import java.io.File;
import java.security.SecureRandom;
import java.util.Random;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class TlsToolkitKeystoreCommandLineTest {

    private SecureRandom secureRandom;
    private TlsToolkitKeystoreCommandLine tlsToolkitKeystoreCommandLine;

    @Before
    public void setup() {
        secureRandom = mock(SecureRandom.class);
        doAnswer(new ForwardsInvocations(new Random())).when(secureRandom).nextBytes(any(byte[].class));
        tlsToolkitKeystoreCommandLine = new TlsToolkitKeystoreCommandLine();
    }

    @Test
    public void testUnknownArg() {
        try {
            tlsToolkitKeystoreCommandLine.parse("--unknownArg");
            fail("Expected error parsing command line");
        } catch (CommandLineParseException e) {
            assertEquals(ExitCode.ERROR_PARSING_COMMAND_LINE, e.getExitCode());
        }
    }

    @Test
    public void testHelpArg() {
        try {
            tlsToolkitKeystoreCommandLine.parse("-h");
            fail("Expected usage and help exit");
        } catch (CommandLineParseException e) {
            assertEquals(ExitCode.HELP, e.getExitCode());
        }
    }

    @Test
    public void testOutputDirectoryMissing() throws CommandLineParseException {
        String testPath = File.separator + "fake" + File.separator + "path" + File.separator + "doesnt" + File.separator + "exist";
        tlsToolkitKeystoreCommandLine.parse("-o", testPath, "");
        assertEquals(testPath, tlsToolkitKeystoreCommandLine.createConfig().getOutputDir().getPath());
    }

    @Test
    public void testOutputDirectoryMissing() throws CommandLineParseException {
        String testPath = File.separator + "fake" + File.separator + "path" + File.separator + "doesnt" + File.separator + "exist";
        tlsToolkitKeystoreCommandLine.parse("-o", testPath);
        assertEquals(testPath, tlsToolkitKeystoreCommandLine.createConfig().getOutputDir().getPath());
    }

    @Test
    public void testKeystoreSplit() throws CommandLineParseException {
        String testPath = "KeyPass";
        tlsToolkitKeystoreCommandLine.parse("-K", "KeyPass");
        assertEquals(testPath, tlsToolkitKeystoreCommandLine.createConfig().getKeyPassword());
    }






}