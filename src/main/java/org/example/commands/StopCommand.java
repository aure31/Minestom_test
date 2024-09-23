package org.example.commands;

import net.minestom.server.command.builder.Command;
import org.example.Main;

public class StopCommand extends Command {

    public StopCommand() {
        super("stop", "shutdown");

        setDefaultExecutor((sender, context) -> Main.stopServer());
    }
}
