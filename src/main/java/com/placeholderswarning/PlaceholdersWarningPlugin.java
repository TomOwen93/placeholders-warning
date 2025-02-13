package com.placeholderswarning;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.*;
import net.runelite.api.widgets.ComponentID;
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

    private static final String CLOSE = "Close";
    private static final String TOGGLE_PLACEHOLDERS = "Always set placeholders";
    private boolean forceRightClickFlag;

    @Override
    protected void shutDown() {
        keyManager.unregisterKeyListener(this);
        clientThread.invokeLater(this::restoreMiniMapAndClickThrough);
        forceRightClickFlag = false;
    }

    @Override
    protected void startUp() {
        keyManager.registerKeyListener(this);
        clientThread.invokeLater(() -> {
            if (isAlwaysSetPlaceHoldersOn()) {
                restoreMiniMapAndClickThrough();
            } else {
                hideMiniMapAndClickThrough();
            }
        });
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged configChanged) {
        if (!configChanged.getGroup().equals("placeholderswarning")) return;

        if (configChanged.getKey().equals("blink")) {
            Widget setPlaceHoldersButton = client.getWidget(12, 40);
            clientThread.invokeLater(() -> {
                if (setPlaceHoldersButton != null && !setPlaceHoldersButton.isHidden())
                    Objects.requireNonNull(client.getWidget(12, 39)).setHidden(false);
            });
        }

        if (configChanged.getKey().equals("bankclosemap")) {
            if (Objects.equals(configChanged.getNewValue(), "false")) {
                restoreMiniMapAndClickThrough();
            } else {
                clientThread.invokeLater(() -> {
                    if (isAlwaysSetPlaceHoldersOn()) {
                        restoreMiniMapAndClickThrough();
                    } else {
                        hideMiniMapAndClickThrough();
                    }
                });
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        if (!config.blink()) return;
        Widget setPlaceHoldersButton = client.getWidget(12, 40);
        if (setPlaceHoldersButton == null || setPlaceHoldersButton.isHidden()) return;

        Widget icon = client.getWidget(12, 41);
        assert icon != null;
        if (setPlaceHoldersButton.getSpriteId() != 170) icon.setHidden(false);
        else icon.setHidden(!icon.isHidden());
    }

    @Subscribe
    void onMenuShouldLeftClick(MenuShouldLeftClick event) {
        if (!forceRightClickFlag || isAlwaysSetPlaceHoldersOn()) {
            return;
        }

        forceRightClickFlag = false;
        MenuEntry[] menuEntries = client.getMenu().getMenuEntries();

        for (MenuEntry entry : menuEntries) {
            if (entry.getOption().equals(CLOSE) && config.bankCloseExit()) {
                event.setForceRightClick(true);
                return;
            }
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (!config.bankCloseMinimap()) return;

        if (event.getMenuTarget().contains(TOGGLE_PLACEHOLDERS)) {
            clientThread.invokeLater(() -> {
                if (isAlwaysSetPlaceHoldersOn()) {
                    restoreMiniMapAndClickThrough();
                } else {
                    hideMiniMapAndClickThrough();
                }
            });
        }
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        if ((event.getOption().equals(CLOSE) && config.bankCloseExit())) {
            forceRightClickFlag = true;
        }
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded widgetLoaded) {
        if (widgetLoaded.getGroupId() != InterfaceID.BANK) return;
        if (isAlwaysSetPlaceHoldersOn()) {
            restoreMiniMapAndClickThrough();
            return;
        }

        if (config.notification().isEnabled())
            notifier.notify(config.notification(), "Always Set Placeholders is turned off!");
        if (config.chatmessage())
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=ff0000>Always Set Placeholders is turned off!</col>", null);
        if (config.bankCloseMinimap()) {
            hideMiniMapAndClickThrough();
        }
    }

    private void hideMiniMapAndClickThrough() {
        Widget bankWidget = client.getWidget(10551310);
        Widget miniMapWidget = client.getWidget(10551391);
        assert bankWidget != null;
        bankWidget.setNoClickThrough(true);
        assert miniMapWidget != null;
            clientThread.invokeLater(() ->
            miniMapWidget.setHidden(true));
    }

    private void restoreMiniMapAndClickThrough() {
        Widget bankWidget = client.getWidget(10551310);
        Widget miniMapWidget = client.getWidget(10551391);
        assert bankWidget != null;
        bankWidget.setNoClickThrough(false);
        assert miniMapWidget != null;
        clientThread.invokeLater(() ->
            miniMapWidget.setHidden(false)
        );
    }

    @Subscribe
    public void onWidgetClosed(WidgetClosed widgetClosed) {
        if (widgetClosed.getGroupId() != InterfaceID.BANK) return;
        restoreMiniMapAndClickThrough();
    }

    private boolean isAlwaysSetPlaceHoldersOn() {
        if (isBankClosed()) {
            return true;
        }

        Widget setPlaceHoldersButton = client.getWidget(12, 40);
        if (setPlaceHoldersButton == null) {
            return false;
        }
        return setPlaceHoldersButton.getSpriteId() != 170;
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
        if (!config.bankCloseEsc()) {
            return;
        }

        if (!isAlwaysSetPlaceHoldersOn() && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            e.consume();

            if (config.chatmessage()) {
                clientThread.invokeLater(() ->
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=ff0000>Placeholders Warning: You cannot use escape to exit the bank!</col>", null)
            );

                if(config.bankCloseMinimap()){
                    clientThread.invokeLater(() ->
                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "<col=ff0000>Placeholders Warning: Minimap is disabled in plugin config!</col>", null)
                    );
                }
            }
        }
    }

    private boolean isBankClosed() {
        return client.getWidget(ComponentID.BANK_CONTAINER) == null;
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
