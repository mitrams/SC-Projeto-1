import java.util.ArrayList;
import java.util.List;

public class Log {

    private static Log self = null;
    private long blockCount = 0;

    private List<Transaction> currentTransactions = new ArrayList<Transaction>();

    private Log() {
    }

    public static Log getInstance() {
        if (self == null) {
            self = new Log();
        }

        return self;
    }

    /*
     * public void write(String message) {
     * System.out.println(message);
     * 
     * transactionCount++;
     * 
     * if (transactionCount == 5) {
     * System.out.println("New Block: " + transactionCount);
     * if (transactionCount > 5) {
     * System.out.println("hash_of_previous_block");
     * } else {
     * System.out.println(0);
     * }
     * System.out.println((long) blockCount);
     * 
     * }
     * }
     */

    public void logTransaction(Transaction transaction) {
        currentTransactions.add(transaction);

        if (currentTransactions.size() == 5)
            saveToFile();

    }

    private void saveToFile() {
        blockCount++;
        System.out.println("Save transactions to file");
        currentTransactions.clear();
    }

}
