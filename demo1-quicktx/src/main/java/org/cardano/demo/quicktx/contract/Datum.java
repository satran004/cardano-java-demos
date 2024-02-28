package org.cardano.demo.quicktx.contract;

import com.bloxbean.cardano.client.plutus.annotation.Constr;
import lombok.Data;

@Constr
@Data
public class Datum {
    private byte[] sender;
}
