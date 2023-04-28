import java.util.ArrayList;
import java.util.List;

public class Blockchain {
    private static Blockchain self = new Blockchain();

    private List<TransactionBlock> blocks = new ArrayList<TransactionBlock>();

    private Blockchain() {
    }

    public static Blockchain getInstance() {
        return self;
    }

    public void initBlock() {
        if (blocks.size() != 0) {
            return;
        }
        TransactionBlock newBlock = new TransactionBlock();
        blocks.add(newBlock);
    }

    public void addBlock(TransactionBlock trxBlock) {
        blocks.add(trxBlock);
    }

    public List<TransactionBlock> getBlocks() {
        return blocks;
    }

    

}
