package org.cardano.demo.quicktx;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.blockfrost.common.Constants;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.bloxbean.cardano.client.cip.cip20.MessageMetadata;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.metadata.MetadataBuilder;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.Tx;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardano.demo.quicktx.DemoConstant.SENDER1_ADDRESS;

public class SimpleTransactions {

    private final BackendService backendService
            = new BFBackendService("http://localhost:8080/api/v1/", "");

    private static Account sender1;
    private static Account sender2;

    @BeforeAll
    static void setup() {
        sender1 = new Account(Networks.testnet(), DemoConstant.SENDER1_MNEMONIC);
        sender2 = new Account(Networks.testnet(), DemoConstant.SENDER2_MNEMONIC);
    }

    @Test
    public void simpleTransfer() {
        var tx = new Tx()
                .payToAddress(DemoConstant.RECEIVER1, Amount.ada(10))
                .from(SENDER1_ADDRESS);

        var quickTxBuilder = new QuickTxBuilder(backendService);

        Result<String> result = quickTxBuilder.compose(tx)
                .withSigner(SignerProviders.signerFrom(sender1))
                .completeAndWait(System.out::println);

        System.out.println(result);
        assertThat(result.isSuccessful()).isTrue();
    }

    @Test
    public void simpleTransfer_multipleSenders() {

        var metadata1 = MetadataBuilder.createMetadata()
                .put(1001, MetadataBuilder.createList()
                                    .add("Sender1 Tx Metadata")
                                    .add("Demo1"));

        var tx1 = new Tx()
                .payToAddress(DemoConstant.RECEIVER1, Amount.ada(10))
                .attachMetadata(metadata1)
                .from(SENDER1_ADDRESS);

        var tx2 = new Tx()
                .payToAddress(DemoConstant.RECEIVER2, Amount.ada(5))
                .attachMetadata(MessageMetadata.create().add("Sender2 Tx Metadata"))
                .from(DemoConstant.SENDER2_ADDRESS);

        var quickTxBuilder = new QuickTxBuilder(backendService);

        Result<String> result = quickTxBuilder.compose(tx1, tx2)
                .feePayer(SENDER1_ADDRESS)
                .withSigner(SignerProviders.signerFrom(sender1, sender2))
                .completeAndWait(System.out::println);

        System.out.println(result);
        assertThat(result.isSuccessful()).isTrue();
    }
}
