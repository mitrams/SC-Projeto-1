import java.security.Signature;
import java.util.ArrayList;
import java.util.List;

public class TransactionBlock {

    private TransactionBlockData data;
    private Signature signature = null;

    public TransactionBlock() {
    }

    public TransactionBlock(TransactionBlockData data) {
        this.data = data;
    }

    public byte[] getHashOfPreviousBlock() {
        return data.getHashOfPreviousBlock();
    }

    public void setHashOfPreviousBlock(byte[] hashOfPreviousBlock) {
        this.data.hashOfPreviousBlock = hashOfPreviousBlock;
    }

    public long getBlk_id() {
        return data.getBlk_id();
    }

    public long getN_trx() {
        return data.getN_trx();
    }

    public List<Transaction> getTransactions() {
        return data.getTransactions();
    }

    public TransactionBlockData getData() {
        return data;
    }

    public Signature getSignature() {
        return signature;
    }

    public void setData(TransactionBlockData data) {
        this.data = data;
    }

    public void setSignature(Signature signature) {
        this.signature = signature;
    }

    

}
