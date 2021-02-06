import net.dv8tion.jda.api.JDA;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StatusUpdater implements Runnable{

    private final JDA jda;
    private int exceptionAmount;
    private int messageAmount;

    private final int UPDATE_DELAY;

    public static final Logger logger = LogManager.getLogger(Application.class);

    public StatusUpdater(JDA jda) {
        this.jda = jda;
        this.messageAmount = 0;
        this.exceptionAmount = 0;
        UPDATE_DELAY = 28800000;
    }

    @SuppressWarnings({"InfiniteLoopStatement", "BusyWait"})
    @Override
    public void run(){
        String status;
        while(true) {
            status = "--------------------" +
                    "\nStatus <JDA>: " + jda.getStatus() +
                    "\nStatus <DB>: " + "DatabaseHandler.getStatus()" +
                    "\nAmount <SERVERS>: " + jda.getGuilds().size() +
                    "\nAmount <EXCEPTIONS>: " + exceptionAmount +
                    "\nAmount <MESSAGES>: " + messageAmount +
                    "\n--------------------";
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

    public void addException() {
        this.exceptionAmount++;
    }

    public void addMessage() {
        this.messageAmount++;
    }
}
