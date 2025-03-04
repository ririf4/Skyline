package net.ririfa.skyline.config

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.ririfa.skyline.client
import net.ririfa.skyline.provider
import net.ririfa.skyline.translation.SkylineMessageKey
import org.lwjgl.glfw.GLFW

object KeyBindingManager {
	private val openConfigKey: KeyBinding = KeyBindingHelper.registerKeyBinding(
		KeyBinding(
			provider.getMessage(SkylineMessageKey.Config.OpenConfig).string,
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_O,
			provider.getMessage(SkylineMessageKey.Config.CategoryInSettings).string
		)
	)

	fun register() {
		ClientTickEvents.END_CLIENT_TICK.register {
			while (openConfigKey.wasPressed()) {
				client.setScreen(client.currentScreen?.let { ConfigManager.createConfigScreen(it) })
			}
		}
	}
}
