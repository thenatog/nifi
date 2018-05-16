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
package org.apache.nifi.processors.geth;

import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;
import org.web3j.protocol.http.HttpService;


import java.io.IOException;
import java.math.BigInteger;
import java.util.*;



@Tags({"example"})
@CapabilityDescription("Provide a description")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute="", description="")})
@WritesAttributes({@WritesAttribute(attribute="", description="")})
public class GetGethTransactions extends AbstractProcessor {

    public static final PropertyDescriptor MY_PROPERTY = new PropertyDescriptor
            .Builder().name("MY_PROPERTY")
            .displayName("My property")
            .description("Example Property")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final Relationship REL_SUCCESS = new Relationship.Builder()
            .name("REL_SUCCESS")
            .description("Example relationship")
            .build();

    private List<PropertyDescriptor> descriptors;

    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
        //XSdescriptors.add(MY_PROPERTY);
        this.descriptors = Collections.unmodifiableList(descriptors);

        final Set<Relationship> relationships = new HashSet<Relationship>();
        relationships.add(REL_SUCCESS);
        this.relationships = Collections.unmodifiableSet(relationships);


        Web3j web3 = Web3j.build(new HttpService());  // defaults to http://localhost:8545/
        Web3ClientVersion web3ClientVersion = null;
        try {
            web3ClientVersion = web3.web3ClientVersion().send();
            String clientVersion = web3ClientVersion.getWeb3ClientVersion();
            context.getLogger().error("Web3j client version is: " + clientVersion);
            System.out.println("Web3j client version is: " + clientVersion);


        } catch (IOException e) {
            context.getLogger().error("NJGOUGH");
            e.printStackTrace();
        }

        try {
            EthBlockNumber latestBlock = web3.ethBlockNumber().send();
            EthSyncing sync = web3.ethSyncing().send();

            BigInteger blockNumber = latestBlock.getBlockNumber();

            EthBlock ethBlock = web3.ethGetBlockByNumber(new DefaultBlockParameterNumber(blockNumber), true).send();
//            String test = ethBlock.getRawResponse();
//            System.out.println("ethBlock raw response: " + test);

            System.out.println("NJGOUGH latest block is: " + String.valueOf(blockNumber));

            System.out.println("NJGOUGH is geth syncing:" + sync.isSyncing());

            System.out.println("NJGOUGH ethBlock is: " + ethBlock.getBlock().getHash());

//            System.out.println("NJGOUGH transactions is: " + );
            System.out.println("NJGOOF raw: " + ethBlock.getRawResponse());
            ListIterator<EthBlock.TransactionResult> a = ethBlock.getBlock().getTransactions().listIterator();


           // EthBlock.TransactionObject obj = (TransactionObject) a.;
            // System.out.println("NJGOUGH ### from: " + obj.get().getFrom() + " to: " + obj.get().getTo());

            while(a.hasNext()) {
                EthBlock.TransactionObject b = (TransactionObject) a.next();
                Transaction trans = b.get();
                //List<TransactionObject> trans = ethBlock.getBlock().getTransactions();

                StringJoiner joiner = new StringJoiner(", ");
                joiner.add("| Transaction: " + trans.getHash());
                joiner.add(" From: " + trans.getFrom());
                joiner.add(" To: " + trans.getTo() + " |");

                System.out.println(joiner.toString());
            }



        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {

    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if ( flowFile == null ) {
            return;
        }

        session.transfer(flowFile, REL_SUCCESS);

        // TODO implement

    }
}
