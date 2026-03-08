package dev.cloud.master.console;

import dev.cloud.master.console.commands.CommandManager;
import dev.cloud.master.console.commands.CommandSender;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs the interactive master console using JLine3.
 * Reads input lines, dispatches them to the command manager,
 * and supports tab-completion via {@link ConsoleCompleter}.
 */
public class ConsoleHandler implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ConsoleHandler.class);
    private static final String PROMPT = "\u001B[36mcloud>\u001B[0m ";

    private final CommandManager commandManager;
    private volatile boolean running = true;

    public ConsoleHandler(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public void run() {
        try {
            Terminal terminal = TerminalBuilder.builder().system(true).build();
            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(new ConsoleCompleter(commandManager))
                    .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
                    .build();

            CommandSender consoleSender = new ConsoleSender();

            while (running) {
                try {
                    String line = reader.readLine(PROMPT);
                    if (line != null && !line.isBlank()) {
                        commandManager.dispatch(consoleSender, line.trim());
                    }
                } catch (UserInterruptException e) {
                    consoleSender.sendMessage("Use 'stop' to shut down the master.");
                } catch (EndOfFileException e) {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Console error: {}", e.getMessage(), e);
        }
    }

    public void stop() {
        running = false;
    }

    // ── inner sender ─────────────────────────────────────────────────────────

    private static class ConsoleSender implements CommandSender {
        @Override
        public void sendMessage(String message) {
            System.out.println(message);
        }

        @Override
        public String getName() {
            return "CONSOLE";
        }
    }
}