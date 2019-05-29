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
package org.apache.nifi.security.xml;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.io.FileLocator;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

// Possible to override only the relevant methods to make XMLConfiguration use a SafeXMLReader/XMLStreamReader, but

public class SafeXMLConfiguration extends XMLConfiguration {

    // override public facing methods which utilise the XML stream reader, wrap the incoming stream reader with a safe stream reader and call the super method with the safe reader as input.
    /** Schema Langauge key for the parser */
    private static final String JAXP_SCHEMA_LANGUAGE =
            "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    /** Schema Language for the parser */
    private static final String W3C_XML_SCHEMA =
            "http://www.w3.org/2001/XMLSchema";

    /** DocumentBuilderFactory parsing DTD features must be disabled to protect against XXE attacks **/
    private static final String DISALLOW_DOCTYPES = "http://apache.org/xml/features/disallow-doctype-decl";
    private static final String ALLOW_EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
    private static final String ALLOW_EXTERNAL_PARAM_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
    private static final String ALLOW_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    @Override
    public void initFileLocator(FileLocator loc) {
        super.initFileLocator(loc);
    }

    public SafeXMLConfiguration() {
        super();
    }

    public SafeXMLConfiguration(HierarchicalConfiguration<ImmutableNode> c) {
        super(c);
    }

    @Override
    public DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
        if (getDocumentBuilder() != null)
        {
            return getDocumentBuilder();
        }
        final DocumentBuilderFactory factory = DocumentBuilderFactory
                .newInstance();
        if (isValidating())
        {
            factory.setValidating(true);
            if (isSchemaValidation())
            {
                factory.setNamespaceAware(true);
                factory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
            }
        }

        // Disable DTDs and external entities to protect against XXE
        factory.setAttribute(DISALLOW_DOCTYPES, true);
        factory.setAttribute(ALLOW_EXTERNAL_GENERAL_ENTITIES, false);
        factory.setAttribute(ALLOW_EXTERNAL_PARAM_ENTITIES, false);
        factory.setAttribute(ALLOW_EXTERNAL_DTD, false);

        final DocumentBuilder result;
        result = factory.newDocumentBuilder();

        result.setEntityResolver(super.getEntityResolver());



            // register an error handler which detects validation errors
            result.setErrorHandler(new DefaultHandler()
            {
                @Override
                public void error(final SAXParseException ex) throws SAXException
                {
                    throw ex;
                }
            });

        return result;
    }



}
