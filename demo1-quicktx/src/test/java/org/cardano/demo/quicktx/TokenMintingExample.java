package org.cardano.demo.quicktx;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.api.util.PolicyUtil;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.blockfrost.common.Constants;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.bloxbean.cardano.client.cip.cip20.MessageMetadata;
import com.bloxbean.cardano.client.cip.cip25.NFT;
import com.bloxbean.cardano.client.cip.cip25.NFTFile;
import com.bloxbean.cardano.client.cip.cip25.NFTMetadata;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.Tx;
import com.bloxbean.cardano.client.transaction.spec.Asset;
import com.bloxbean.cardano.client.transaction.spec.Policy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardano.demo.quicktx.DemoConstant.SENDER1_ADDRESS;

public class TokenMintingExample {
    private final static BackendService backendService
            = new BFBackendService("http://localhost:8080/api/v1/", "");

    private static Account sender1;
    private static Account sender2;

    private static QuickTxBuilder quickTxBuilder;

    @BeforeAll
    static void setup() {
        sender1 = new Account(Networks.testnet(), DemoConstant.SENDER1_MNEMONIC);
        sender2 = new Account(Networks.testnet(), DemoConstant.SENDER2_MNEMONIC);

        quickTxBuilder = new QuickTxBuilder(backendService);
    }

    @Test
    public void mintFT() throws Exception {
        Policy policy = PolicyUtil.createMultiSigScriptAtLeastPolicy("test_policy", 1, 1);

        String assetName = "DemoFT-1";
        BigInteger qty = BigInteger.valueOf(1000);

        var metadata = MessageMetadata.create().add("FT Minting tx");

        Tx tx = new Tx()
                .mintAssets(policy.getPolicyScript(), new Asset(assetName, qty), SENDER1_ADDRESS)
                .attachMetadata(metadata)
                .from(sender1.baseAddress());

        Result<String> result = quickTxBuilder.compose(tx)
                .withSigner(SignerProviders.signerFrom(sender1))
                .withSigner(SignerProviders.signerFrom(policy))
                .completeAndWait(System.out::println);

        System.out.println(result);
        assertThat(result.isSuccessful()).isTrue();
    }

    @Test
    public void mintNFT() throws Exception {
        Policy policy = PolicyUtil.createMultiSigScriptAtLeastPolicy("test_policy", 1, 1);

        String assetName = "DemoNFT-1";
        BigInteger qty = BigInteger.valueOf(1);

        NFT nft = NFT.create()
                .assetName(assetName)
                .name(assetName)
                .image("ipfs://Qmcv6hwtmdVumrNeb42R1KmCEWdYWGcqNgs17Y3hj6CkP4")
                .mediaType("image/png")
                .addFile(NFTFile.create()
                        .name("file-1")
                        .mediaType("image/png")
                        .src("ipfs://Qmcv6hwtmdVumrNeb42R1KmCEWdYWGcqNgs17Y3hj6CkP4"))
                .description("This is a test NFT")
                .description("This is a test NFT-2")
                .property("Artist", "Simply NFT")
                .property("Brand", "My Brand")
                .property("Series", "2")
                .property("Attributes", Arrays.asList("Accessory", "Chain", "Clothing"));

        NFTMetadata nftMetadata = NFTMetadata.create().addNFT(policy.getPolicyId(), nft);

        Tx tx = new Tx()
                .mintAssets(policy.getPolicyScript(), new Asset(assetName, qty), SENDER1_ADDRESS)
                .attachMetadata(nftMetadata)
                .from(SENDER1_ADDRESS);

        Result<String> result = quickTxBuilder.compose(tx)
                .withSigner(SignerProviders.signerFrom(sender1))
                .withSigner(SignerProviders.signerFrom(policy))
                .completeAndWait(System.out::println);

        System.out.println(result);
        assertThat(result.isSuccessful()).isTrue();
    }
}
