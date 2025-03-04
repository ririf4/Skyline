package net.ririfa.skyline

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.ririfa.langman.InitType
import net.ririfa.langman.LangMan
import net.ririfa.skyline.config.ConfigManager
import net.ririfa.skyline.config.KeyBindingManager
import net.ririfa.skyline.renderer.ChunkCache
import net.ririfa.skyline.translation.SLMSGProvider
import net.ririfa.skyline.translation.SkylineMessageKey
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.use

class Skyline : ClientModInitializer, ModMenuApi {
	companion object {
		const val MOD_ID = "skyline"
		lateinit var langMan: LangMan<SLMSGProvider, Text>

		val client: MinecraftClient = MinecraftClient.getInstance()
		val logger: Logger = LoggerFactory.getLogger(Skyline::class.simpleName)
		val loader: FabricLoader = FabricLoader.getInstance()
		val clientDir: Path = loader.gameDir
		val modDir: Path = clientDir.resolve(MOD_ID)
		val langDir: Path = modDir.resolve("lang")

		val availableLang = listOf<String>("en", "ja")
	}

	override fun onInitializeClient() {
		extractLangFiles()
		langMan = LangMan.createNew(
			{ Text.of(it) },
			SkylineMessageKey::class,
			false
		)
		langMan.init(
			InitType.YAML,
			langDir.toFile(),
			availableLang
		)
		KeyBindingManager.register()
		ConfigManager.load()
		registerEvents()
	}

	private fun registerEvents() {
		ClientLifecycleEvents.CLIENT_STARTED.register { ChunkCache.SSBO.create() }
		ClientLifecycleEvents.CLIENT_STOPPING.register { ChunkCache.SSBO.destroy() }
	}

	override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
		return ConfigScreenFactory { parent -> ConfigManager.createConfigScreen(parent) }
	}

	private fun extractLangFiles() {
		try {
			val targetDir = Paths.get("$modDir/lang")
			if (!Files.exists(targetDir)) {
				Files.createDirectories(targetDir)
			}

			val devSourceDir = Paths.get("build/resources/main/assets/$MOD_ID/lang")
			if (Files.exists(devSourceDir)) {
				copyLanguageFiles(devSourceDir, targetDir)
				return
			}

			val langPath = "assets/$MOD_ID/lang/"
			val classLoader = this::class.java.classLoader
			val resourceUrl = classLoader.getResource(langPath)

			if (resourceUrl == null) {
				logger.error("Failed to find language directory in JAR: $langPath")
				return
			}

			val uri = resourceUrl.toURI()
			val fs = if (uri.scheme == "jar") FileSystems.newFileSystem(uri, emptyMap<String, Any>()) else null
			val langDirPath = Paths.get(uri)

			copyLanguageFiles(langDirPath, targetDir)

			fs?.close()
		} catch (e: Exception) {
			logger.error("Failed to extract language files", e)
		}
	}

	private fun copyLanguageFiles(sourceDir: Path, targetDir: Path) {
		Files.walk(sourceDir).use { paths ->
			paths.filter { Files.isRegularFile(it) && it.toString().endsWith(".yml") }.forEach { resourceFile ->
				val targetFile = targetDir.resolve(resourceFile.fileName.toString())
				if (!Files.exists(targetFile)) {
					Files.copy(resourceFile, targetFile)
				}
			}
		}
	}
}
