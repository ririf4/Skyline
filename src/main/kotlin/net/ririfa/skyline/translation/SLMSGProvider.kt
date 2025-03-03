package net.ririfa.skyline.translation

import net.minecraft.text.Text
import net.ririfa.langman.def.MessageProviderDefault
import net.ririfa.skyline.client

class SLMSGProvider : MessageProviderDefault<SLMSGProvider, Text>(Text::class.java) {
	companion object {
		val instance = SLMSGProvider()
	}

	override fun getLanguage(): String {
		val lang = client.options?.language?.split("_")?.getOrNull(0) ?: "en"
		return lang
	}

}