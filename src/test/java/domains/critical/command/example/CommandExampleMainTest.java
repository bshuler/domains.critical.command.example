package domains.critical.command.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CommandExampleMainTest {

    private ServerMock server;
    private CommandExampleMain plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(CommandExampleMain.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void onEnableRegistersTestCommand() {
        // CommandExampleCommand is registered via ACF's PaperCommandManager,
        // which writes directly into the server's CommandMap - it never
        // appears via server.getPluginCommand("test") because plugin.yml
        // declares no `commands:` section. Same gotcha as the
        // domains.critical.groups/domains.critical.regions siblings.
        assertNotNull(server.getCommandMap().getCommand("test"));
    }

    @Test
    void onDisableDoesNotThrow() {
        assertDoesNotThrow(plugin::onDisable);
    }

    @Test
    void onLoadDoesNotThrow() {
        // onLoad() already ran once as part of MockBukkit.load() in
        // setUp(); invoking it again directly exercises the same
        // super.onLoad() delegation without relying on that as the only
        // coverage source.
        assertDoesNotThrow(plugin::onLoad);
    }
}
