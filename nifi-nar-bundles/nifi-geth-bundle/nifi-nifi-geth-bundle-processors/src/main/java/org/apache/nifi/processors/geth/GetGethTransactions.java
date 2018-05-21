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
import org.web3j.tx.Contract;


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


//        Contract con = new Contract() {
//        }


        Web3j web3 = Web3j.build(new HttpService(true));  // defaults to http://localhost:8545/
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
            System.out.println("NJGOOF raw: " + ethBlock.getRawResponse());
            EthGetTransactionReceipt recpt = web3.ethGetTransactionReceipt("0x32a1cfe0aad10102c7e7b7db38b3e73a78008802a26ea1b8c6488b8adddabaa4").send();
            System.out.println("NGOLF recpt = " + recpt.getRawResponse());
            EthCompileSolidity solid = web3.ethCompileSolidity("{\"status\":\"1\",\"message\":\"OK\",\"result\":\"[{\\\"constant\\\":false,\\\"inputs\\\":[{\\\"name\\\":\\\"tokenGet\\\",\\\"type\\\":\\\"address\\\"},{\\\"name\\\":\\\"amountGet\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"name\\\":\\\"tokenGive\\\",\\\"type\\\":\\\"address\\\"},{\\\"name\\\":\\\"amountGive\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"name\\\":\\\"expires\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"name\\\":\\\"nonce\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"name\\\":\\\"user\\\",\\\"type\\\":\\\"address\\\"},{\\\"name\\\":\\\"v\\\",\\\"type\\\":\\\"uint8\\\"},{\\\"name\\\":\\\"r\\\",\\\"type\\\":\\\"bytes32\\\"},{\\\"name\\\":\\\"s\\\",\\\"type\\\":\\\"bytes32\\\"},{\\\"name\\\":\\\"amount\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"name\\\":\\\"trade\\\",\\\"outputs\\\":[],\\\"payable\\\":false,\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":false,\\\"inputs\\\":[{\\\"name\\\":\\\"tokenGet\\\",\\\"type\\\":\\\"address\\\"},{\\\"name\\\":\\\"amountGet\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"name\\\":\\\"tokenGive\\\",\\\"type\\\":\\\"address\\\"},{\\\"name\\\":\\\"amountGive\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"name\\\":\\\"expires\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"name\\\":\\\"nonce\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"name\\\":\\\"order\\\",\\\"outputs\\\":[],\\\"payable\\\":false,\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":true,\\\"inputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"address\\\"},{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"bytes32\\\"}],\\\"name\\\":\\\"orderFills\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"payable\\\":false,\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":false,\\\"inputs\\\":[{\\\"name\\\":\\\"tokenGet\\\",\\\"type\\\":\\\"address\\\"},{\\\"name\\\":\\\"amountGet\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"name\\\":\\\"tokenGive\\\",\\\"type\\\":\\\"address\\\"},{\\\"name\\\":\\\"amountGive\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"name\\\":\\\"expires\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"name\\\":\\\"nonce\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"name\\\":\\\"v\\\",\\\"type\\\":\\\"uint8\\\"},{\\\"name\\\":\\\"r\\\",\\\"type\\\":\\\"bytes32\\\"},{\\\"name\\\":\\\"s\\\",\\\"type\\\":\\\"bytes32\\\"}],\\\"name\\\":\\\"cancelOrder\\\",\\\"outputs\\\":[],\\\"payable\\\":false,\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":false,\\\"inputs\\\":[{\\\"name\\\":\\\"amount\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"name\\\":\\\"withdraw\\\",\\\"outputs\\\":[],\\\"payable\\\":false,\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":false,\\\"inputs\\\":[{\\\"name\\\":\\\"token\\\",\\\"type\\\":\\\"address\\\"},{\\\"name\\\":\\\"amount\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"name\\\":\\\"depositToken\\\",\\\"outputs\\\":[],\\\"payable\\\":false,\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":true,\\\"inputs\\\":[{\\\"name\\\":\\\"tokenGet\\\",\\\"type\\\":\\\"address\\\"},{\\\"name\\\":\\\"amountGet\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"name\\\":\\\"tokenGive\\\",\\\"type\\\":\\\"address\\\"},{\\\"name\\\":\\\"amountGive\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"name\\\":\\\"expires\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"name\\\":\\\"nonce\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"name\\\":\\\"user\\\",\\\"type\\\":\\\"address\\\"},{\\\"name\\\":\\\"v\\\",\\\"type\\\":\\\"uint8\\\"},{\\\"name\\\":\\\"r\\\",\\\"type\\\":\\\"bytes32\\\"},{\\\"name\\\":\\\"s\\\",\\\"type\\\":\\\"bytes32\\\"}],\\\"name\\\":\\\"amountFilled\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"payable\\\":false,\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":true,\\\"inputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"address\\\"},{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"address\\\"}],\\\"name\\\":\\\"tokens\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"payable\\\":false,\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":false,\\\"inputs\\\":[{\\\"name\\\":\\\"feeMake_\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"name\\\":\\\"changeFeeMake\\\",\\\"outputs\\\":[],\\\"payable\\\":false,\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":true,\\\"inputs\\\":[],\\\"name\\\":\\\"feeMake\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"payable\\\":false,\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":false,\\\"inputs\\\":[{\\\"name\\\":\\\"feeRebate_\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"name\\\":\\\"changeFeeRebate\\\",\\\"outputs\\\":[],\\\"payable\\\":false,\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":true,\\\"inputs\\\":[],\\\"name\\\":\\\"feeAccount\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"address\\\"}],\\\"payable\\\":false,\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":true,\\\"inputs\\\":[{\\\"name\\\":\\\"tokenGet\\\",\\\"type\\\":\\\"address\\\"},{\\\"name\\\":\\\"amountGet\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"name\\\":\\\"tokenGive\\\",\\\"type\\\":\\\"address\\\"},{\\\"name\\\":\\\"amountGive\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"name\\\":\\\"expires\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"name\\\":\\\"nonce\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"name\\\":\\\"user\\\",\\\"type\\\":\\\"address\\\"},{\\\"name\\\":\\\"v\\\",\\\"type\\\":\\\"uint8\\\"},{\\\"name\\\":\\\"r\\\",\\\"type\\\":\\\"bytes32\\\"},{\\\"name\\\":\\\"s\\\",\\\"type\\\":\\\"bytes32\\\"},{\\\"name\\\":\\\"amount\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"name\\\":\\\"sender\\\",\\\"type\\\":\\\"address\\\"}],\\\"name\\\":\\\"testTrade\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"bool\\\"}],\\\"payable\\\":false,\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":false,\\\"inputs\\\":[{\\\"name\\\":\\\"feeAccount_\\\",\\\"type\\\":\\\"address\\\"}],\\\"name\\\":\\\"changeFeeAccount\\\",\\\"outputs\\\":[],\\\"payable\\\":false,\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":true,\\\"inputs\\\":[],\\\"name\\\":\\\"feeRebate\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"payable\\\":false,\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":false,\\\"inputs\\\":[{\\\"name\\\":\\\"feeTake_\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"name\\\":\\\"changeFeeTake\\\",\\\"outputs\\\":[],\\\"payable\\\":false,\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":false,\\\"inputs\\\":[{\\\"name\\\":\\\"admin_\\\",\\\"type\\\":\\\"address\\\"}],\\\"name\\\":\\\"changeAdmin\\\",\\\"outputs\\\":[],\\\"payable\\\":false,\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":false,\\\"inputs\\\":[{\\\"name\\\":\\\"token\\\",\\\"type\\\":\\\"address\\\"},{\\\"name\\\":\\\"amount\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"name\\\":\\\"withdrawToken\\\",\\\"outputs\\\":[],\\\"payable\\\":false,\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":true,\\\"inputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"address\\\"},{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"bytes32\\\"}],\\\"name\\\":\\\"orders\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"bool\\\"}],\\\"payable\\\":false,\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":true,\\\"inputs\\\":[],\\\"name\\\":\\\"feeTake\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"payable\\\":false,\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":false,\\\"inputs\\\":[],\\\"name\\\":\\\"deposit\\\",\\\"outputs\\\":[],\\\"payable\\\":true,\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":false,\\\"inputs\\\":[{\\\"name\\\":\\\"accountLevelsAddr_\\\",\\\"type\\\":\\\"address\\\"}],\\\"name\\\":\\\"changeAccountLevelsAddr\\\",\\\"outputs\\\":[],\\\"payable\\\":false,\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":true,\\\"inputs\\\":[],\\\"name\\\":\\\"accountLevelsAddr\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"address\\\"}],\\\"payable\\\":false,\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":true,\\\"inputs\\\":[{\\\"name\\\":\\\"token\\\",\\\"type\\\":\\\"address\\\"},{\\\"name\\\":\\\"user\\\",\\\"type\\\":\\\"address\\\"}],\\\"name\\\":\\\"balanceOf\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"payable\\\":false,\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":true,\\\"inputs\\\":[],\\\"name\\\":\\\"admin\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"address\\\"}],\\\"payable\\\":false,\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":true,\\\"inputs\\\":[{\\\"name\\\":\\\"tokenGet\\\",\\\"type\\\":\\\"address\\\"},{\\\"name\\\":\\\"amountGet\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"name\\\":\\\"tokenGive\\\",\\\"type\\\":\\\"address\\\"},{\\\"name\\\":\\\"amountGive\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"name\\\":\\\"expires\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"name\\\":\\\"nonce\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"name\\\":\\\"user\\\",\\\"type\\\":\\\"address\\\"},{\\\"name\\\":\\\"v\\\",\\\"type\\\":\\\"uint8\\\"},{\\\"name\\\":\\\"r\\\",\\\"type\\\":\\\"bytes32\\\"},{\\\"name\\\":\\\"s\\\",\\\"type\\\":\\\"bytes32\\\"}],\\\"name\\\":\\\"availableVolume\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"payable\\\":false,\\\"type\\\":\\\"function\\\"},{\\\"inputs\\\":[{\\\"name\\\":\\\"admin_\\\",\\\"type\\\":\\\"address\\\"},{\\\"name\\\":\\\"feeAccount_\\\",\\\"type\\\":\\\"address\\\"},{\\\"name\\\":\\\"accountLevelsAddr_\\\",\\\"type\\\":\\\"address\\\"},{\\\"name\\\":\\\"feeMake_\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"name\\\":\\\"feeTake_\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"name\\\":\\\"feeRebate_\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"payable\\\":false,\\\"type\\\":\\\"constructor\\\"},{\\\"payable\\\":false,\\\"type\\\":\\\"fallback\\\"},{\\\"anonymous\\\":false,\\\"inputs\\\":[{\\\"indexed\\\":false,\\\"name\\\":\\\"tokenGet\\\",\\\"type\\\":\\\"address\\\"},{\\\"indexed\\\":false,\\\"name\\\":\\\"amountGet\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"indexed\\\":false,\\\"name\\\":\\\"tokenGive\\\",\\\"type\\\":\\\"address\\\"},{\\\"indexed\\\":false,\\\"name\\\":\\\"amountGive\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"indexed\\\":false,\\\"name\\\":\\\"expires\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"indexed\\\":false,\\\"name\\\":\\\"nonce\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"indexed\\\":false,\\\"name\\\":\\\"user\\\",\\\"type\\\":\\\"address\\\"}],\\\"name\\\":\\\"Order\\\",\\\"type\\\":\\\"event\\\"},{\\\"anonymous\\\":false,\\\"inputs\\\":[{\\\"indexed\\\":false,\\\"name\\\":\\\"tokenGet\\\",\\\"type\\\":\\\"address\\\"},{\\\"indexed\\\":false,\\\"name\\\":\\\"amountGet\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"indexed\\\":false,\\\"name\\\":\\\"tokenGive\\\",\\\"type\\\":\\\"address\\\"},{\\\"indexed\\\":false,\\\"name\\\":\\\"amountGive\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"indexed\\\":false,\\\"name\\\":\\\"expires\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"indexed\\\":false,\\\"name\\\":\\\"nonce\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"indexed\\\":false,\\\"name\\\":\\\"user\\\",\\\"type\\\":\\\"address\\\"},{\\\"indexed\\\":false,\\\"name\\\":\\\"v\\\",\\\"type\\\":\\\"uint8\\\"},{\\\"indexed\\\":false,\\\"name\\\":\\\"r\\\",\\\"type\\\":\\\"bytes32\\\"},{\\\"indexed\\\":false,\\\"name\\\":\\\"s\\\",\\\"type\\\":\\\"bytes32\\\"}],\\\"name\\\":\\\"Cancel\\\",\\\"type\\\":\\\"event\\\"},{\\\"anonymous\\\":false,\\\"inputs\\\":[{\\\"indexed\\\":false,\\\"name\\\":\\\"tokenGet\\\",\\\"type\\\":\\\"address\\\"},{\\\"indexed\\\":false,\\\"name\\\":\\\"amountGet\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"indexed\\\":false,\\\"name\\\":\\\"tokenGive\\\",\\\"type\\\":\\\"address\\\"},{\\\"indexed\\\":false,\\\"name\\\":\\\"amountGive\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"indexed\\\":false,\\\"name\\\":\\\"get\\\",\\\"type\\\":\\\"address\\\"},{\\\"indexed\\\":false,\\\"name\\\":\\\"give\\\",\\\"type\\\":\\\"address\\\"}],\\\"name\\\":\\\"Trade\\\",\\\"type\\\":\\\"event\\\"},{\\\"anonymous\\\":false,\\\"inputs\\\":[{\\\"indexed\\\":false,\\\"name\\\":\\\"token\\\",\\\"type\\\":\\\"address\\\"},{\\\"indexed\\\":false,\\\"name\\\":\\\"user\\\",\\\"type\\\":\\\"address\\\"},{\\\"indexed\\\":false,\\\"name\\\":\\\"amount\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"indexed\\\":false,\\\"name\\\":\\\"balance\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"name\\\":\\\"Deposit\\\",\\\"type\\\":\\\"event\\\"},{\\\"anonymous\\\":false,\\\"inputs\\\":[{\\\"indexed\\\":false,\\\"name\\\":\\\"token\\\",\\\"type\\\":\\\"address\\\"},{\\\"indexed\\\":false,\\\"name\\\":\\\"user\\\",\\\"type\\\":\\\"address\\\"},{\\\"indexed\\\":false,\\\"name\\\":\\\"amount\\\",\\\"type\\\":\\\"uint256\\\"},{\\\"indexed\\\":false,\\\"name\\\":\\\"balance\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"name\\\":\\\"Withdraw\\\",\\\"type\\\":\\\"event\\\"}]\"}").send();
            Map<String, EthCompileSolidity.Code> compiledSolid = solid.getCompiledSolidity();
            System.out.println("Compiled Solid: " + String.valueOf(compiledSolid));

            ListIterator<EthBlock.TransactionResult> a = ethBlock.getBlock().getTransactions().listIterator();

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
