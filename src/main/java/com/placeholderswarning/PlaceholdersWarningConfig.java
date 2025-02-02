package com.placeholderswarning;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Notification;

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

	@ConfigItem(
			keyName = "bankclose",
			name = "Prevent Bank Close",
			description = "Prevents you from closing your bank interface until \"Always Set Placeholders\" is turned back on",
			position = 4
	)
	default boolean bankClose()
	{
		return true;
	}
}
