package runtime;

import net.dv8tion.jda.api.JDA;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StatusUpdater implements Runnable{

    private final JDA jda;
    private static int exceptionAmount;
    private static int messageAmount;

    private final int UPDATE_DELAY;

    public static final Logger logger = LogManager.getLogger(Application.class);

    public StatusUpdater(JDA jda) {
        this.jda = jda;
        messageAmount = 0;
        exceptionAmount = 0;
        UPDATE_DELAY = 28800000;
    }

    @SuppressWarnings({"InfiniteLoopStatement", "BusyWait"})
    @Override
    public void run(){
        String status;
        while(true) {
            status = "Status <JDA>: " + jda.getStatus() +
                    " Status <DB>: " + "DatabaseHandler.getStatus()" +
                    " Amount <SERVERS>: " + jda.getGuilds().size() +
                    " Amount <EXCEPTIONS>: " + exceptionAmount +
                    " Amount <MESSAGES>: " + messageAmount;
            exceptionAmount = 0;
            messageAmount = 0;
            logger.info(status);
            try {
                Thread.sleep(UPDATE_DELAY);
            } catch (InterruptedException exception) {
                exceptionAmount++;
                exception.printStackTrace();
            }
        }
    }

    public static void addException() {
        exceptionAmount++;
    }

    public static void addMessage() {
        messageAmount++;
    }
}
