package com.placeholderswarning;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.util.Objects;

@Slf4j
@PluginDescriptor(
        name = "Placeholders Warning",
        description = "Alerts you when \"Always Set Placeholders\" is off in your bank",
        tags = {"always", "set", "placeholders", "bank", "organization", "sorting"}
)
public class PlaceholdersWarningPlugin extends Plugin implements KeyListener {
    @Inject
    private Notifier notifier;
    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private PlaceholdersWarningConfig config;
    @Inject
    KeyManager keyManager;

    @Subscribe
    public void onConfigChanged(ConfigChanged configChanged) {
        if (!configChanged.getGroup().equals("placeholderswarning")) return;

        if (configChanged.getKey().equals("blink")) {
            Widget button = client.getWidget(12, 40);
            clientThread.invokeLater(() -> {
                if (button != null && !button.isHidden())
                    Objects.requireNonNull(client.getWidget(12, 39)).setHidden(false);
            });
        }
        if (configChanged.getKey().equals("bankclose")) {
            clientThread.invokeLater(() -> {
                Widget parent = client.getWidget(12, 2);
                if (parent != null) {
                    Widget button = parent.getChild(11);
                    if (button != null) {
                        button.setHidden(false);
                    }
                }
            });
        }
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        handleBlink();
        handleHideExitBank();

    }

    private void handleHideExitBank() {
        if (!config.bankClose()) return;
        Widget exitBankButton = Objects.requireNonNull(client.getWidget(12, 2)).getChild(11);
        if (Objects.requireNonNull(client.getWidget(12, 40)).getSpriteId() != 170) {
            assert exitBankButton != null;
            exitBankButton.setHidden(false);
        } else {
            assert exitBankButton != null;
            exitBankButton.setHidden(true);
        }
    }

    private void handleBlink() {
        if (!config.blink()) return;
        Widget button = client.getWidget(12, 40);
        if (button == null || button.isHidden()) return;
        Widget icon = client.getWidget(12, 41);
        assert icon != null;
        if (button.getSpriteId() != 170) icon.setHidden(false);
        else icon.setHidden(!icon.isHidden());
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded widgetLoaded) {
        if (widgetLoaded.getGroupId() != InterfaceID.BANK) return;
        if (Objects.requireNonNull(client.getWidget(12, 40)).getSpriteId() != 170) return;
        if (config.notification().isEnabled())
            notifier.notify(config.notification(), "Always Set Placeholders is turned off!");
        if (config.chatmessage())
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=ff0000>Always Set Placeholders is turned off!</col>", null);
    }

    @Provides
    PlaceholdersWarningConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PlaceholdersWarningConfig.class);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (config.bankClose() && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            e.consume();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    protected void startUp() throws Exception {
        keyManager.registerKeyListener(this);
    }

    @Override
    protected void shutDown() throws Exception {
        keyManager.unregisterKeyListener(this);
    }
}
