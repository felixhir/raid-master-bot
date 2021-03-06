package runtime;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

public class ReadyListener implements EventListener {


    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof ReadyEvent) {
            LogManager.getLogger(ReadyListener.class).info("connected to the discord API successfully");
        }
    }
}
