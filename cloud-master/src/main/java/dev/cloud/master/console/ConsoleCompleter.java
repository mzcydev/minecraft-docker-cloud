package dev.cloud.master.console;

import dev.cloud.master.console.commands.CommandManager;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

/**
 * JLine3 tab-completion provider for the master console.
 * Completes command names and their subcommands.
 */
public class ConsoleCompleter implements Completer {

    private final CommandManager commandManager;

    public ConsoleCompleter(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        List<String> words = line.words();

        if (words.size() <= 1) {
            // Complete command names
            commandManager.allCommands().stream()
                    .map(cmd -> new Candidate(cmd.getName(), cmd.getName(),
                            null, cmd.getDescription(), null, null, true))
                    .forEach(candidates::add);
        } else {
            // Subcommand completions per command
            String cmdName = words.get(0).toLowerCase();
            commandManager.find(cmdName).ifPresent(cmd -> {
                switch (cmdName) {
                    case "group" -> List.of("create", "delete", "list", "info")
                            .stream().map(Candidate::new).forEach(candidates::add);
                    case "service" -> List.of("list", "stop", "info")
                            .stream().map(Candidate::new).forEach(candidates::add);
                    case "node" -> List.of("list", "info")
                            .stream().map(Candidate::new).forEach(candidates::add);
                    case "player" -> List.of("list", "info")
                            .stream().map(Candidate::new).forEach(candidates::add);
                    default -> {
                    }
                }
            });
        }
    }
}