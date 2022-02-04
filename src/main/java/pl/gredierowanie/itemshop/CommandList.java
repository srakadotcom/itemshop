package pl.gredierowanie.itemshop;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class CommandList {

  private final List<String> commands;

  public CommandList(List<String> commands) {
    this.commands = commands;
  }

  private static void executeCommand(String command) {
    Bukkit.getServer().dispatchCommand(
        Bukkit.getServer().getConsoleSender(),
        command
    );
  }

  public void execute(CommandSender player) {
    commands.stream()
        .map(str -> str.replace("{PLAYER}", player.getName()))
        .forEach(CommandList::executeCommand);
  }
}