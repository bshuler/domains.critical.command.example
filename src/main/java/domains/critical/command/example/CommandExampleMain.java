package domains.critical.command.example;

import org.bukkit.plugin.java.JavaPlugin;

import co.aikar.commands.PaperCommandManager;
import domains.critical.command.example.CommandExampleCommand;;

public class CommandExampleMain extends JavaPlugin {
	@Override
	public void onEnable() {
		PaperCommandManager manager = new PaperCommandManager(this);
		manager.registerCommand(new CommandExampleCommand());
	}

}
