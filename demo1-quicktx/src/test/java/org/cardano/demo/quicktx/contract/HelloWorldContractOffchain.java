package org.cardano.demo.quicktx.contract;

import com.bloxbean.cardano.aiken.AikenTransactionEvaluator;
import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.client.api.UtxoSupplier;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.api.DefaultUtxoSupplier;
import com.bloxbean.cardano.client.backend.blockfrost.common.Constants;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.function.helper.ScriptUtxoFinders;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.plutus.blueprint.PlutusBlueprintUtil;
import com.bloxbean.cardano.client.plutus.blueprint.model.PlutusVersion;
import com.bloxbean.cardano.client.plutus.spec.BytesPlutusData;
import com.bloxbean.cardano.client.plutus.spec.ConstrPlutusData;
import com.bloxbean.cardano.client.plutus.spec.PlutusData;
import com.bloxbean.cardano.client.plutus.spec.PlutusScript;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.ScriptTx;
import com.bloxbean.cardano.client.quicktx.Tx;
import org.cardano.demo.quicktx.DemoConstant;
import org.cardano.demo.quicktx.ProjectKey;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardano.demo.quicktx.DemoConstant.RECEIVER1;
import static org.cardano.demo.quicktx.DemoConstant.SENDER1_ADDRESS;

public class HelloWorldContractOffchain {
    private final static BackendService backendService
            = new BFBackendService(Constants.BLOCKFROST_PREPROD_URL, ProjectKey.BLOCKFROST_PROJECT_ID);

    private static Account sender1 = new Account(Networks.testnet(), DemoConstant.SENDER1_MNEMONIC);
    ;

    static String compiledCode = "59010d010000323232323232323232322223232533300a3232533300c002100114a06644646600200200644a66602400229404c8c94ccc044cdc78010028a511330040040013015002375c60260026eb0cc01cc024cc01cc024011200048040dd71980398048032400066e3cdd71980318040022400091010d48656c6c6f2c20576f726c642100149858c94ccc028cdc3a400000226464a66601e60220042930b1bae300f00130080041630080033253330093370e900000089919299980718080010a4c2c6eb8c038004c01c01058c01c00ccc0040052000222233330073370e0020060164666600a00a66e000112002300d001002002230053754002460066ea80055cd2ab9d5573caae7d5d0aba21";
    static PlutusScript plutusScript = PlutusBlueprintUtil.getPlutusScriptFromCompiledCode(compiledCode, PlutusVersion.v2);

    static String scrtiptAddr = AddressProvider.getEntAddress(plutusScript, Networks.testnet()).toBech32();

    @Test
    public void lock() {
        //create datum
        PlutusData datum = ConstrPlutusData.of(0,
                BytesPlutusData.of(sender1.getBaseAddress().getPaymentCredentialHash().get()));

        Tx tx = new Tx()
                .payToContract(scrtiptAddr, Amount.ada(10), datum)
                .from(SENDER1_ADDRESS);

        var quickTxBuilder = new QuickTxBuilder(backendService);
        Result<String> result = quickTxBuilder.compose(tx)
                .withSigner(SignerProviders.signerFrom(sender1))
                .completeAndWait(System.out::println);

        System.out.println(result);
        assertThat(result.isSuccessful()).isTrue();
    }

    @Test
    public void unlock() throws Exception {
        PlutusData datum =
                ConstrPlutusData.of(0, BytesPlutusData.of(sender1.getBaseAddress().getPaymentCredentialHash().get()));

        UtxoSupplier utxoSupplier = new DefaultUtxoSupplier(backendService.getUtxoService());
        Utxo scriptUtxo = ScriptUtxoFinders.findFirstByInlineDatum(utxoSupplier, scrtiptAddr, datum).orElseThrow();

        PlutusData redeemer = ConstrPlutusData.of(0, BytesPlutusData.of("Hello, World!"));

        ScriptTx sctipTx = new ScriptTx()
                .collectFrom(scriptUtxo, redeemer)
                .payToAddress(RECEIVER1, Amount.ada(10))
                .attachSpendingValidator(plutusScript);

        QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);
        Result<String> result = quickTxBuilder.compose(sctipTx)
                .feePayer(SENDER1_ADDRESS)
                .withSigner(SignerProviders.signerFrom(sender1))
                .withRequiredSigners(sender1.getBaseAddress())
//              .collateralPayer(SENDER1_ADDRESS)
//              .withTxEvaluator(new AikenTransactionEvaluator(backendService))
                .completeAndWait(System.out::println);

        System.out.println(result);
        assertThat(result.isSuccessful()).isTrue();
    }

}
