public class Log {

    private static Log self = null;

    private Log() {
    }

    public static Log getInstance() {
        if (self == null) {
            self = new Log();
        }

        return self;
    }

    public void write(String message) {
        System.out.println(message);
    }
}
