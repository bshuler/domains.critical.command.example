package domains.critical.command.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.command.ConsoleCommandSenderMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandExampleCommandTest {

    private ServerMock server;
    private CommandExampleCommand command;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        MockBukkit.load(CommandExampleMain.class);
        command = new CommandExampleCommand();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void onTest1SendsFirstSubcommandMessageDirectly() {
        // Direct unit-level exercise of the @Default handler, independent
        // of ACF's routing.
        PlayerMock player = server.addPlayer();
        command.onTest1(player);
        assertEquals("Test command completion first subcommand", player.nextMessage());
    }

    @Test
    void onTest2SendsSecondSubcommandMessageDirectly() {
        PlayerMock player = server.addPlayer();
        command.onTest2(player);
        assertEquals("Test command completion second subcommand", player.nextMessage());
    }

    @Test
    void onTest1SendsMessageToConsoleSender() {
        // Neither handler restricts the sender type - both accept any
        // CommandSender - so the console path is exercised too, not just
        // the player path.
        ConsoleCommandSenderMock console = server.getConsoleSender();
        command.onTest1(console);
        assertEquals("Test command completion first subcommand", console.nextMessage());
    }

    @Test
    void onTest2SendsMessageToConsoleSender() {
        ConsoleCommandSenderMock console = server.getConsoleSender();
        command.onTest2(console);
        assertEquals("Test command completion second subcommand", console.nextMessage());
    }

    @Test
    void defaultSubcommandDispatchesViaTestCommand() {
        // End-to-end via ACF's PaperCommandManager routing (registered in
        // CommandExampleMain.onEnable): "/test" with no subcommand hits the
        // @Default handler (onTest1).
        PlayerMock player = server.addPlayer();
        boolean dispatched = player.performCommand("test");
        assertTrue(dispatched);
        assertEquals("Test command completion first subcommand", player.nextMessage());
    }

    @Test
    void exampleSubcommandDispatchesViaTestCommand() {
        // "/test example" routes to onTest2 via ACF's @Subcommand("example").
        PlayerMock player = server.addPlayer();
        boolean dispatched = player.performCommand("test example");
        assertTrue(dispatched);
        assertEquals("Test command completion second subcommand", player.nextMessage());
    }
}
