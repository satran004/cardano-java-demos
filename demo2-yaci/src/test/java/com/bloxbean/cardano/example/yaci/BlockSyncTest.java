package com.bloxbean.cardano.example.yaci;

import com.bloxbean.cardano.yaci.core.common.Constants;
import com.bloxbean.cardano.yaci.core.model.Block;
import com.bloxbean.cardano.yaci.core.model.Era;
import com.bloxbean.cardano.yaci.core.protocol.chainsync.messages.Point;
import com.bloxbean.cardano.yaci.helper.BlockSync;
import com.bloxbean.cardano.yaci.helper.listener.BlockChainDataListener;
import com.bloxbean.cardano.yaci.helper.model.Transaction;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class BlockSyncTest {

    private static final String node = Constants.PREPROD_IOHK_RELAY_ADDR;
    private static final int nodePort = Constants.PREPROD_IOHK_RELAY_PORT;
    private static final long protocolMagic = Constants.PREPROD_PROTOCOL_MAGIC;

    private BlockSync blockSync = new BlockSync(node, nodePort, protocolMagic, Constants.WELL_KNOWN_PREPROD_POINT);

    @Test
    public void syncFromTip() throws Exception {

        var blockChainDataListener = new BlockChainDataListener() {
            @Override
            public void onBlock(Era era, Block block, List<Transaction> transactions) {
                System.out.println("Block: " + block.getHeader().getHeaderBody().getBlockNumber());
                transactions.stream()
                        .forEach(tx -> {
                            System.out.println("Tx Hash: " + tx.getTxHash());
                        });
            }

            @Override
            public void onRollback(Point point) {
                System.out.println("Rollback to point: " + point);
            }
        };


        blockSync.startSyncFromTip(blockChainDataListener);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();

    }


    @Test
    public void syncFromAPoint() throws Exception {

        var blockChainDataListener = new BlockChainDataListener() {
            @Override
            public void onBlock(Era era, Block block, List<Transaction> transactions) {
                System.out.println("Block: " + block.getHeader().getHeaderBody().getBlockNumber());
                transactions.stream()
                        .forEach(tx -> {
                            System.out.println("Tx Hash: " + tx.getTxHash());
                        });
            }

            @Override
            public void onRollback(Point point) {
                System.out.println("Rollback to point: " + point);
            }
        };


        blockSync.startSync(new Point(51100065, "14458986ae4c71c7354df7b841516871ab090fb6ac53c565dd71e46dab8bda09"),
                blockChainDataListener);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();

    }
}
