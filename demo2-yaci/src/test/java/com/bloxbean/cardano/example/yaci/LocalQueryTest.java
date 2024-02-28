package com.bloxbean.cardano.example.yaci;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.yaci.core.common.Constants;
import com.bloxbean.cardano.yaci.core.protocol.localstate.queries.BlockHeightQuery;
import com.bloxbean.cardano.yaci.core.protocol.localstate.queries.BlockHeightQueryResult;
import com.bloxbean.cardano.yaci.core.protocol.localstate.queries.UtxoByAddressQuery;
import com.bloxbean.cardano.yaci.core.protocol.localstate.queries.UtxoByAddressQueryResult;
import com.bloxbean.cardano.yaci.helper.LocalClientProvider;
import com.bloxbean.cardano.yaci.helper.LocalStateQueryClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

public class LocalQueryTest {
    private final static String nodeSocketFile = "/Users/satya/work/cardano-node/preprod-8.7.3/db/node.socket";
    private final static long protocolMagic = Constants.PREPROD_PROTOCOL_MAGIC;

    private static LocalClientProvider localClientProvider = new LocalClientProvider(nodeSocketFile, protocolMagic);
    private static LocalStateQueryClient localStateQueryClient;

    @BeforeAll
    static void setup() {
        localClientProvider.start();
        localStateQueryClient = localClientProvider.getLocalStateQueryClient();
    }

    @Test
    public void getUtxos() throws Exception {
        localStateQueryClient.acquire().block(Duration.ofSeconds(5));

        UtxoByAddressQueryResult result = (UtxoByAddressQueryResult) localStateQueryClient.executeQuery(new UtxoByAddressQuery(new Address("addr_test1qpx28h3fempmtuwsm8ldy8qrk9xe6ehrcdqlhu09m950q60n8k9j6yjs9mna4d9fcxlc28ge73qjh9hnyvzk7s5uy5zqrcpls5")))
                .block(Duration.ofSeconds(5));

        List<Utxo> utxos = result.getUtxoList();

        System.out.println("Utxos >> " + utxos);
    }

    @Test
    public void blockHeightQuery() throws Exception {
        BlockHeightQueryResult queryResult = (BlockHeightQueryResult) localStateQueryClient.executeQuery(new BlockHeightQuery())
                .block(Duration.ofSeconds(5));
        System.out.println("Block Height >> " + queryResult.getBlockHeight());
    }
}
