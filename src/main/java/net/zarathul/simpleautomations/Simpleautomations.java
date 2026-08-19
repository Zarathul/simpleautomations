package net.zarathul.simpleautomations;

import net.fabricmc.api.ModInitializer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zarathul.simpleautomations.items.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.IllegalFormatException;

public class Simpleautomations implements ModInitializer
{
	public static final String MOD_ID = "simpleautomations";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize()
	{
		Items.initialize();
	}

	// TODO: Remove this once the Utils situation is fixed.

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
	/**
	 * Gets the localized formatted components for the specified key and formatting arguments.
	 *
	 * @param key
	 * The localization key.
	 * @param args
	 * Formatting arguments.
	 * @return
	 * A list of localized text components for the specified key, or an empty list if the key was not found.
	 */
	public static ArrayList<Component> multiLineTranslate(String key, Object... args)
	{
		ArrayList<Component> components = new ArrayList<>();

		String text = translate(key, args);
		String[] lines = text.split("\\n");

		for (String line : lines)
		{
			components.add(Component.literal(line));
		}

		return components;
	}

	/**
	 * Get the formatted localized string literal for the specified key and formatting arguments.
	 *
	 * @param key
	 * The localization key.
	 * @param args
	 * Formatting arguments.
	 * @return
	 * The formatted, localized string. The key itself, if no localized string was found. Or the key with {@code " :: Format Error"} appended, if formatting failed.
	 */
	public static String translate(String key, Object... args)
	{
		Language I18N = Language.getInstance();
		if ((key != null) && I18N.has(key))
		{
			String text = I18N.getOrDefault(key);

			try
			{
				return String.format(text, args);
			}
			catch (IllegalFormatException _)
			{
				return key + " :: Format Error";
			}
		}

		return key;
	}
}
