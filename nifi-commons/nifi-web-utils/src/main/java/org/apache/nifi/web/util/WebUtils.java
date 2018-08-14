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
package org.apache.nifi.web.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import javax.net.ssl.SSLContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriBuilderException;
import org.apache.commons.lang3.StringUtils;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.nifi.util.NiFiProperties;
import com.google.common.base.Strings;

/**
 * Common utilities related to web development.
 */
public final class WebUtils {

    private static Logger logger = LoggerFactory.getLogger(WebUtils.class);

    final static ReadWriteLock lock = new ReentrantReadWriteLock();

    private static final String PROXY_CONTEXT_PATH_HTTP_HEADER = "X-ProxyContextPath";
    private static final String FORWARDED_CONTEXT_HTTP_HEADER = "X-Forwarded-Context";

    private WebUtils() {
    }

    /**
     * Creates a client for non-secure requests. The client will be created
     * using the given configuration. Additionally, the client will be
     * automatically configured for JSON serialization/deserialization.
     *
     * @param config client configuration
     * @return a Client instance
     */
    public static Client createClient(final ClientConfig config) {
        return createClientHelper(config, null);
    }

    /**
     * Creates a client for secure requests. The client will be created using
     * the given configuration and security context. Additionally, the client
     * will be automatically configured for JSON serialization/deserialization.
     *
     * @param config client configuration
     * @param ctx    security context
     * @return a Client instance
     */
    public static Client createClient(final ClientConfig config, final SSLContext ctx) {
        return createClientHelper(config, ctx);
    }

    /**
     * A helper method for creating clients. The client will be created using
     * the given configuration and security context. Additionally, the client
     * will be automatically configured for JSON serialization/deserialization.
     *
     * @param config client configuration
     * @param ctx    security context, which may be null for non-secure client
     *               creation
     * @return a Client instance
     */
    private static Client createClientHelper(final ClientConfig config, final SSLContext ctx) {

        ClientBuilder clientBuilder = ClientBuilder.newBuilder();

        if (config != null) {
            clientBuilder = clientBuilder.withConfig(config);
        }

        if (ctx != null) {

            // Apache http DefaultHostnameVerifier that checks subject alternative names against the hostname of the URI
            clientBuilder = clientBuilder.sslContext(ctx).hostnameVerifier(new DefaultHostnameVerifier());
        }

        clientBuilder = clientBuilder.register(ObjectMapperResolver.class).register(JacksonJaxbJsonProvider.class);

        return clientBuilder.build();

    }

    /**
     * This method will check the provided context path headers against a whitelist (provided in nifi.properties) and throw an exception if the requested context path is not registered.
     *
     * @param uri                     the request URI
     * @param request                 the HTTP request
     * @param whitelistedContextPaths comma-separated list of valid context paths
     * @return the resource path
     * @throws UriBuilderException if the requested context path is not registered (header poisoning)
     */
    public static String getResourcePath(URI uri, HttpServletRequest request, String whitelistedContextPaths) throws UriBuilderException {
        String resourcePath = uri.getPath();

        // Determine and normalize the context path
        String determinedContextPath = determineContextPath(request);
        determinedContextPath = normalizeContextPath(determinedContextPath);

        // If present, check it and prepend to the resource path
        if (StringUtils.isNotBlank(determinedContextPath)) {
            verifyContextPath(whitelistedContextPaths, determinedContextPath);

            // Determine the complete resource path
            resourcePath = determinedContextPath + resourcePath;
        }

        return resourcePath;
    }

    /**
     * Throws an exception if the provided context path is not in the whitelisted context paths list.
     *
     * @param whitelistedContextPaths a comma-delimited list of valid context paths
     * @param determinedContextPath   the normalized context path from a header
     * @throws UriBuilderException if the context path is not safe
     */
    public static void verifyContextPath(String whitelistedContextPaths, String determinedContextPath) throws UriBuilderException {
        // If blank, ignore
        if (StringUtils.isBlank(determinedContextPath)) {
            return;
        }

        // Check it against the whitelist
        List<String> individualContextPaths = Arrays.asList(StringUtils.split(whitelistedContextPaths, ","));
        if (!individualContextPaths.contains(determinedContextPath)) {
            final String msg = "The provided context path [" + determinedContextPath + "] was not whitelisted [" + whitelistedContextPaths + "]";
            logger.error(msg);
            throw new UriBuilderException(msg);
        }
    }

    /**
     * Returns a normalized context path (leading /, no trailing /). If the parameter is blank, an empty string will be returned.
     *
     * @param determinedContextPath the raw context path
     * @return the normalized context path
     */
    public static String normalizeContextPath(String determinedContextPath) {
        if (StringUtils.isNotBlank(determinedContextPath)) {
            // normalize context path
            if (!determinedContextPath.startsWith("/")) {
                determinedContextPath = "/" + determinedContextPath;
            }

            if (determinedContextPath.endsWith("/")) {
                determinedContextPath = determinedContextPath.substring(0, determinedContextPath.length() - 1);
            }

            return determinedContextPath;
        } else {
            return "";
        }
    }

    /**
     * Returns a "safe" context path value from the request headers to use in a proxy environment.
     * This is used on the JSP to build the resource paths for the external resources (CSS, JS, etc.).
     * If no headers are present specifying this value, it is an empty string.
     *
     * @param request the HTTP request
     * @return the context path safe to be printed to the page
     */
    public static String sanitizeContextPath(ServletRequest request, String whitelistedContextPaths, String jspDisplayName) {
        if (StringUtils.isBlank(jspDisplayName)) {
            jspDisplayName = "JSP page";
        }
        String contextPath = normalizeContextPath(determineContextPath((HttpServletRequest) request));
        try {
            verifyContextPath(whitelistedContextPaths, contextPath);
            return contextPath;
        } catch (UriBuilderException e) {
            logger.error("Error determining context path on " + jspDisplayName + ": " + e.getMessage());
            return "";
        }
    }

    /**
     * Determines the context path if populated in {@code X-ProxyContextPath} or {@code X-ForwardContext} headers. If not populated, returns an empty string.
     *
     * @param request the HTTP request
     * @return the provided context path or an empty string
     */
    public static String determineContextPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String proxyContextPath = request.getHeader(PROXY_CONTEXT_PATH_HTTP_HEADER);
        String forwardedContext = request.getHeader(FORWARDED_CONTEXT_HTTP_HEADER);

        logger.debug("Context path: " + contextPath);
        String determinedContextPath = "";

        // If either header is set, log both
        if (anyNotBlank(proxyContextPath, forwardedContext)) {
            logger.debug(String.format("On the request, the following context paths were parsed" +
                            " from headers:\n\t X-ProxyContextPath: %s\n\tX-Forwarded-Context: %s",
                    proxyContextPath, forwardedContext));

            // Implementing preferred order here: PCP, FCP
            determinedContextPath = StringUtils.isNotBlank(proxyContextPath) ? proxyContextPath : forwardedContext;
        }

        logger.debug("Determined context path: " + determinedContextPath);
        return determinedContextPath;
    }

    /**
     * Returns true if any of the provided arguments are not blank.
     *
     * @param strings a variable number of strings
     * @return true if any string has content (not empty or all whitespace)
     */
    private static boolean anyNotBlank(String... strings) {
        for (String s : strings) {
            if (StringUtils.isNotBlank(s)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> generateDefaultHostnames(NiFiProperties niFiProperties) {
        List<String> validHosts = new ArrayList<>();
        int serverPort = 0;

        if (niFiProperties == null) {
            logger.warn("NiFiProperties not configured; returning minimal default hostnames");
        } else {
            try {
                serverPort = niFiProperties.getConfiguredHttpOrHttpsPort();
            } catch (RuntimeException e) {
                logger.warn("Cannot fully generate list of default hostnames because the server port is not configured in nifi.properties. Defaulting to port 0 for host header evaluation");
            }

            // Add any custom network interfaces
            try {
                final int lambdaPort = serverPort;
                List<String> customIPs = extractIPsFromNetworkInterfaces(niFiProperties);
                customIPs.stream().forEach(ip -> {
                    validHosts.add(ip);
                    validHosts.add(ip + ":" + lambdaPort);
                });
            } catch (final Exception e) {
                logger.warn("Failed to determine custom network interfaces.", e);
            }
        }

        // Sometimes the hostname is left empty but the port is always populated
        validHosts.add("127.0.0.1");
        validHosts.add("127.0.0.1:" + serverPort);
        validHosts.add("localhost");
        validHosts.add("localhost:" + serverPort);
        validHosts.add("[::1]");
        validHosts.add("[::1]:" + serverPort);

        // Add the loopback and actual IP address and hostname used
        try {
            validHosts.add(InetAddress.getLoopbackAddress().getHostAddress().toLowerCase());
            validHosts.add(InetAddress.getLoopbackAddress().getHostAddress().toLowerCase() + ":" + serverPort);

            validHosts.add(InetAddress.getLocalHost().getHostName().toLowerCase());
            validHosts.add(InetAddress.getLocalHost().getHostName().toLowerCase() + ":" + serverPort);

            validHosts.add(InetAddress.getLocalHost().getHostAddress().toLowerCase());
            validHosts.add(InetAddress.getLocalHost().getHostAddress().toLowerCase() + ":" + serverPort);
        } catch (final Exception e) {
            logger.warn("Failed to determine local hostname.", e);
        }

        // Dedupe but maintain order
        final List<String> uniqueHosts = uniqueList(validHosts);
        if (logger.isDebugEnabled()) {
            logger.debug("Determined {} valid default hostnames and IP addresses for incoming headers: {}", new Object[]{uniqueHosts.size(), StringUtils.join(uniqueHosts, ", ")});
        }
        return uniqueHosts;
    }

    /**
     * Extracts the list of IP addresses from custom bound network interfaces. If both HTTPS and HTTP interfaces are
     * defined and HTTPS is enabled, only HTTPS interfaces will be returned. If none are defined, an empty list will be
     * returned.
     *
     * @param niFiProperties the NiFiProperties object
     * @return the list of IP addresses
     */
    static List<String> extractIPsFromNetworkInterfaces(NiFiProperties niFiProperties) {
        Map<String, String> networkInterfaces = niFiProperties.isHTTPSConfigured() ? niFiProperties.getHttpsNetworkInterfaces() : niFiProperties.getHttpNetworkInterfaces();
        if (isNotDefined(networkInterfaces)) {
            // No custom interfaces defined
            return new ArrayList<>(0);
        } else {
            List<String> allIPAddresses = new ArrayList<>();
            for (Map.Entry<String, String> entry : networkInterfaces.entrySet()) {
                final String networkInterfaceName = entry.getValue();
                try {
                    NetworkInterface ni = NetworkInterface.getByName(networkInterfaceName);
                    List<String> ipAddresses = Collections.list(ni.getInetAddresses()).stream().map(inetAddress -> inetAddress.getHostAddress().toLowerCase()).collect(Collectors.toList());
                    logger.debug("Resolved the following IP addresses for network interface {}: {}", new Object[]{networkInterfaceName, StringUtils.join(ipAddresses, ", ")});
                    allIPAddresses.addAll(ipAddresses);
                } catch (SocketException e) {
                    logger.warn("Cannot resolve network interface named " + networkInterfaceName);
                }
            }

            // Dedupe while maintaining order
            return uniqueList(allIPAddresses);
        }
    }

    /**
     * Returns a unique {@code List} of the elements maintaining the original order.
     *
     * @param duplicateList a list that may contain duplicate elements
     * @return a list maintaining the original order which no longer contains duplicate elements
     */
    public static List<String> uniqueList(List<String> duplicateList) {
        return new ArrayList<>(new LinkedHashSet<>(duplicateList));
    }

    /**
     * Returns true if the provided map of properties and network interfaces is null, empty, or the actual definitions are empty.
     *
     * @param networkInterfaces the map of properties to bindings
     *                          ({@code ["nifi.web.http.network.interface.first":"eth0"]})
     * @return
     */
    static boolean isNotDefined(Map<String, String> networkInterfaces) {
        return networkInterfaces == null || networkInterfaces.isEmpty() || networkInterfaces.values().stream().filter(value -> !Strings.isNullOrEmpty(value)).collect(Collectors.toList()).isEmpty();
    }



}
