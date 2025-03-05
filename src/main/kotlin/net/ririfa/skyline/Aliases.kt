package net.ririfa.skyline

import net.minecraft.client.MinecraftClient
import net.ririfa.skyline.config.ConfigManager
import net.ririfa.skyline.translation.SLMSGProvider

val LM by lazy { Skyline.langMan }
val SL by lazy { Skyline }

val provider = SLMSGProvider.instance
val client: MinecraftClient by lazy { Skyline.client }
val Logger by lazy { Skyline.logger }
val Config by lazy { ConfigManager.config }