import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Blockchain {
    private static Blockchain self = new Blockchain();

    private List<TransactionBlock> blocks = new ArrayList<TransactionBlock>();
    private TransactionBlock newestBlock;

    private Blockchain() {
    }

    public static Blockchain getInstance() {
        self.initBlock();
        return self;
    }

    private void initBlock() {
        if (blocks.size() != 0) {
            return;
        }
        TransactionBlock newBlock = new TransactionBlock(1);
        blocks.add(newBlock);
        newestBlock = newBlock;
    }

    private void addBlock(TransactionBlock trxBlock) {
        blocks.add(trxBlock);
        newestBlock = trxBlock;
    }

    public List<TransactionBlock> getBlocks() {
        return blocks;
    }

    public void logTransaction(Transaction transaction) {
        newestBlock.addTransaction(transaction);

        if (newestBlock.getN_trx() == 5)
            saveToFile();

    }

    private void saveToFile() {
        // TODO save blocks to files

        

        ObjectOutputStream oos;
        try {
            File blockFile = new File("Blocks", "block_" + blocks.size() + ".blk");
            blockFile.createNewFile();

            oos = new ObjectOutputStream(new FileOutputStream(blockFile));
            
            newestBlock.updateSignature();

            oos.writeObject(newestBlock.getHashOfPreviousBlock());
            oos.writeObject(newestBlock.getBlk_id());
            oos.writeObject(newestBlock.getN_trx());
            for (Transaction transaction : newestBlock.getData().getTransactions()) {
                oos.writeObject(transaction.getType());
                oos.writeObject(transaction.getWineID());
                oos.writeObject(transaction.getQuantity());
                oos.writeObject(transaction.getValue());
                oos.writeObject(transaction.getUserID());
            }
            oos.writeObject(newestBlock.getSignature());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
        System.out.println("Saved transactions to file");

        
        byte[] hashOfPreviousBlock = new byte[32];
        
        try {
            // Create a MessageDigest instance using SHA-256 algorithm
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                ObjectOutputStream os = new ObjectOutputStream(out);
                os.writeObject(newestBlock);
                os.flush();
            } catch (Exception e) {
                // TODO: handle exception
            }

            // Get the hash bytes of the object
            byte[] hashBytes = digest.digest(out.toByteArray());
            
            // Fill hash array with the hash bytes
            System.arraycopy(hashBytes, 0, hashOfPreviousBlock, 0, Math.min(hashBytes.length, hashOfPreviousBlock.length));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        addBlock(new TransactionBlock(blocks.size() + 1));
        newestBlock.setHashOfPreviousBlock(hashOfPreviousBlock);
    }

    

}
