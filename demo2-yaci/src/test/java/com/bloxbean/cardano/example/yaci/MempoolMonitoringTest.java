package com.bloxbean.cardano.example.yaci;

import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import com.bloxbean.cardano.yaci.core.common.Constants;
import com.bloxbean.cardano.yaci.helper.LocalClientProvider;
import com.bloxbean.cardano.yaci.helper.LocalStateQueryClient;
import com.bloxbean.cardano.yaci.helper.LocalTxMonitorClient;
import com.bloxbean.cardano.yaci.helper.model.MempoolStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MempoolMonitoringTest {
    private final static String nodeSocketFile = "/Users/satya/work/cardano-node/preprod-8.7.3/db/node.socket";
    private final static long protocolMagic = Constants.PREPROD_PROTOCOL_MAGIC;

    private static LocalClientProvider localClientProvider = new LocalClientProvider(nodeSocketFile, protocolMagic);
    private static LocalTxMonitorClient localTxMonitorClient;

    @BeforeAll
    static void setup() {
        localClientProvider.start();
        localTxMonitorClient = localClientProvider.getTxMonitorClient();
    }

    @Test
    public void testMempoolMonitoring() throws Exception {
        while (true) {
            System.out.println("Waiting to acquire next snapshot ...");
            List<byte[]> txBytesList = localTxMonitorClient.acquireAndGetMempoolTransactionsAsMono().block();

            for(byte[] txBytes: txBytesList) {
                String txHash = TransactionUtil.getTxHash(txBytes);
                System.out.println("Tx Hash >> " + txHash);

                Transaction transaction = Transaction.deserialize(txBytes);
                System.out.println("Tx Body >> " + transaction);
            }

            MempoolStatus mempoolStatus = localTxMonitorClient.getMempoolSizeAndCapacity().block();
            System.out.println("Mem Pool >> " + mempoolStatus);
        }
    }
}
