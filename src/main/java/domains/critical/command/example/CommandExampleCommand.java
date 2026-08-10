package domains.critical.command.example;

import org.bukkit.command.CommandSender;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;

@CommandAlias("test")
public class CommandExampleCommand extends BaseCommand {

	@Default
	@Description("Test command completion first subcommand")
	@Subcommand("command")
	public void onTest1(CommandSender sender) {
		sender.sendMessage("Test command completion first subcommand");
	}

	@Description("Test command completion second subcommand")
	@Subcommand("example")
	public void onTest2(CommandSender sender) {
		sender.sendMessage("Test command completion second subcommand");
	}
}
