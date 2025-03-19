package com.bloxbean.cardano.example.yaci;

import com.bloxbean.cardano.client.util.JsonUtil;
import com.bloxbean.cardano.yaci.core.common.Constants;
import com.bloxbean.cardano.yaci.core.model.Block;
import com.bloxbean.cardano.yaci.core.model.Era;
import com.bloxbean.cardano.yaci.core.protocol.chainsync.messages.Point;
import com.bloxbean.cardano.yaci.core.util.Tuple;
import com.bloxbean.cardano.yaci.helper.BlockRangeSync;
import com.bloxbean.cardano.yaci.helper.listener.BlockChainDataListener;
import com.bloxbean.cardano.yaci.helper.model.Transaction;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class BlockFetchTest {
    private static final String node = Constants.PREPROD_PUBLIC_RELAY_ADDR;
    private static final int nodePort = Constants.PREPROD_PUBLIC_RELAY_PORT;
    private static final long protocolMagic = Constants.PREPROD_PROTOCOL_MAGIC;

    private BlockRangeSync blockRangeSync
            = new BlockRangeSync(node, nodePort, protocolMagic);

    @Test
    public void fetchBlocks() throws Exception {

        var blockChainDataListener = new BlockChainDataListener() {

            @Override
            public void onBlock(Era era, Block block, List<Transaction> transactions) {
                System.out.println("Block: " + block.getHeader().getHeaderBody().getBlockNumber());
                transactions.stream()
                        .flatMap(transaction -> transaction.getBody().getOutputs().stream())
                        .map(transactionOutput -> new Tuple<>(transactionOutput.getAddress(), transactionOutput.getAmounts()))
                        .forEach(tuple -> {
                            System.out.println("Address: " + tuple._1);
                            System.out.println("Amounts: " + JsonUtil.getPrettyJson(tuple._2));
                            System.out.println("");
                        });
            }

            @Override
            public void onRollback(Point point) {
                System.out.println("Rollback received : " + point);
            }
        };

        var fromPoint = new Point(31818133, "61401bb83bd3c2cbab5356bc194add0cbb2fe04d8904c5d092a4d048311ffdf4");
        var toPoint = new Point(53444580, "9fd1c844dbc644503c1cf7778da2a5ae9deccff0c3afc4a8901eca5dddffc908");

        blockRangeSync.start(blockChainDataListener);
        blockRangeSync.fetch(fromPoint, toPoint);

        var countDownLatch = new CountDownLatch(1);
        countDownLatch.await();
    }

}
