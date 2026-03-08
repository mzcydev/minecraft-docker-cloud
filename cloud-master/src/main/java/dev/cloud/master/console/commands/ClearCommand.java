package dev.cloud.master.console.commands;

/**
 * Clears the console output.
 */
public class ClearCommand implements Command {

    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public String getDescription() {
        return "Clears the console.";
    }

    @Override
    public String getUsage() {
        return "clear";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // ANSI escape: move cursor to top-left and clear screen
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}