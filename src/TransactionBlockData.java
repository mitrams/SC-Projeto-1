import java.util.ArrayList;
import java.util.List;

public class TransactionBlockData {
    byte[] hashOfPreviousBlock = new byte[32];
    long blk_id;
    long n_trx = 0;
    List<Transaction> transactions = new ArrayList<Transaction>();

    /* 
     * Constructors
     */

    public TransactionBlockData(long blk_id) {
        this.blk_id = blk_id;
    }

    public TransactionBlockData(byte[] hashOfPreviousBlock, long blk_id) {
        this.hashOfPreviousBlock = hashOfPreviousBlock;
        this.blk_id = blk_id;
    }

    /* 
     * Getter and Setters
     */

    public byte[] getHashOfPreviousBlock() {
        return hashOfPreviousBlock;
    }

    public void setHashOfPreviousBlock(byte[] hashOfPreviousBlock) {
        this.hashOfPreviousBlock = hashOfPreviousBlock;
    }

    public long getBlk_id() {
        return blk_id;
    }

    public long getN_trx() {
        return n_trx;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
    
    /* 
     * Other Methods
     */

    public void addTransaction(Transaction trx) {
        transactions.add(trx);
        n_trx++;
    }

}
