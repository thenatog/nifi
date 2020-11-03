package org.apache.nifi.web.security.anonymous


import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4.class)
class NiFiAnonymousAuthenticationAllowListTest extends GroovyTestCase  {

    @Test
    void testFileOnAllowList() {
        // Arrange
        String staticResourceServletPath = "/css/main.css";

        // Act
        boolean isAllowedAccess = NiFiAnonymousAuthenticationAllowList.isRequestedResourceInAllowList(staticResourceServletPath);

        // Assert
        assertTrue(isAllowedAccess)
    }

    @Test
    void testFileNotOnAllowList() {
        // Arrange
        String staticResourceServletPath = "/css/aFileNotOnTheAllowList.css";

        // Act
        boolean isAllowedAccess = NiFiAnonymousAuthenticationAllowList.isRequestedResourceInAllowList(staticResourceServletPath);

        // Assert
        assertFalse(isAllowedAccess)
    }

    @Test
    void testMaliciousAccessTokenRequest() {
        // Arrange
        String staticResourceServletPath = "/css/main.css&/access/token";

        // Act
        boolean isAllowedAccess = NiFiAnonymousAuthenticationAllowList.isRequestedResourceInAllowList(staticResourceServletPath);

        // Assert
        assertFalse(isAllowedAccess)
    }

    @Test
    void testBangInFileRequest() {
        // Arrange
        String staticResourceServletPath = "/css/main.css!/access/token";

        // Act
        boolean isAllowedAccess = NiFiAnonymousAuthenticationAllowList.isRequestedResourceInAllowList(staticResourceServletPath);

        // Assert
        assertFalse(isAllowedAccess)
    }

    @Test
    void testParentDirectoryInFileRequest() {
        // Arrange
        String staticResourceServletPath = "../../css/main.css/../../../explet";

        // Act
        boolean isAllowedAccess = NiFiAnonymousAuthenticationAllowList.isRequestedResourceInAllowList(staticResourceServletPath);

        // Assert
        assertFalse(isAllowedAccess)
    }
}
