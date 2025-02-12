package com.placeholderswarning;

import net.runelite.client.config.*;

@ConfigGroup("placeholderswarning")
public interface PlaceholdersWarningConfig extends Config
{
	@ConfigItem(
			keyName = "blink",
			name = "Icon Blinking",
			description = "Blink the button's icon every tick",
			position = 1
	)
	default boolean blink()
	{
		return true;
	}
	@ConfigItem(
			keyName = "chatmessage",
			name = "Chat Message",
			description = "Receive a game message in chat when opening the bank",
			position = 2
	)
	default boolean chatmessage()
	{
		return true;
	}
	@ConfigItem(
			keyName = "notification",
			name = "Notification",
			description = "Receive a notification when opening the bank",
			position = 3
	)
	default Notification notification()
	{
		return Notification.ON;
	}

	@ConfigSection(
			name = "Prevent Bank Close Options",
			description = "Options for preventing you from closing your bank until \"Always Set Placeholders\" is turned back on",
			position = 4
	)
	String bankClose = "bankClose";

	@ConfigItem(
			keyName = "bankcloseesc",
			name = "Esc Key",
			section = bankClose,
			description = "Prevents you from closing your bank interface with esc key (if set)",
			position = 0
	)
	default boolean bankCloseEsc()
	{
		return false;
	}
	@ConfigItem(
			keyName = "bankclosemap",
			name = "Minimap",
			section = bankClose,
			description = "Prevents you from closing your bank interface with your minimap",
			position = 1
	)
	default boolean bankCloseMinimap()
	{
		return false;
	}

	@ConfigItem(
			keyName = "bankcloseexit",
			name = "Bank Exit Button",
			section = bankClose,
			description = "Prevents you from closing the exit button (removes left click)",
			position = 2
	)
	default boolean bankCloseExit()
	{
		return false;
	}
}
