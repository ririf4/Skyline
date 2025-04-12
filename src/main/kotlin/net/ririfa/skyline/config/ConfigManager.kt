package net.ririfa.skyline.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigCategory
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import net.ririfa.skyline.SL
import net.ririfa.skyline.provider
import net.ririfa.skyline.translation.SkylineMessageKey
import java.nio.file.Files
import java.nio.file.Path

object ConfigManager {
	@JvmField
	var config = Config()
	private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

	fun createConfigScreen(parent: Screen): Screen {
		load()
		val builder = ConfigBuilder.create().setParentScreen(parent).setTitle(provider.getMessage(SkylineMessageKey.Config.Title))
		val entryBuilder = builder.entryBuilder()
		val category: ConfigCategory = builder.getOrCreateCategory(provider.getMessage(SkylineMessageKey.Config.Category.General))

		category.addEntry(entryBuilder.startIntField(provider.getMessage(SkylineMessageKey.Config.Settings.RenderDistance.Name), config.renderDistance)
			.setDefaultValue(config.renderDistance)
			.setSaveConsumer { config = config.copy(renderDistance = it) }
			.setTooltip(provider.getMessage(SkylineMessageKey.Config.Settings.RenderDistance.Tooltip))
			.build()
		)

		category.addEntry(entryBuilder.startBooleanToggle(provider.getMessage(SkylineMessageKey.Config.Settings.UseLOD.Name), config.useLOD)
			.setDefaultValue(config.useLOD)
			.setSaveConsumer { config = config.copy(useLOD = it) }
			.setTooltip(provider.getMessage(SkylineMessageKey.Config.Settings.UseLOD.Tooltip))
			.build()
		)

		category.addEntry(entryBuilder.startIntField(provider.getMessage(SkylineMessageKey.Config.Settings.LODThreshold.Name), config.lodThreshold)
			.setDefaultValue(config.lodThreshold)
			.setSaveConsumer { config = config.copy(lodThreshold = it) }
			.setTooltip(
				provider.getMessage(
					SkylineMessageKey.Config.Settings.LODThreshold.Tooltip,
					provider.getMessage(
						SkylineMessageKey.Config.Settings.RenderDistance.Name
					).string
				)
			)
			.build()
		)

		category.addEntry(
			entryBuilder.startEnumSelector(
				provider.getMessage(SkylineMessageKey.Config.Settings.ChunkSizeXZ.Name),
				ChunkSize::class.java,
				ChunkSize.of(config.chunkSizeX, config.chunkSizeZ)
			)
				.setDefaultValue(ChunkSize.of(config.chunkSizeX, config.chunkSizeZ))
				.setEnumNameProvider { (it as ChunkSize).displayName }
				.setSaveConsumer { config = config.copy(chunkSizeX = it.size.x, chunkSizeZ = it.size.z) }
				.setTooltip(provider.getMessage(SkylineMessageKey.Config.Settings.ChunkSizeXZ.Tooltip))
				.build()
		)

		category.addEntry(entryBuilder.startIntField(provider.getMessage(SkylineMessageKey.Config.Settings.MaxChunks.Name), config.maxChunks)
			.setDefaultValue(config.maxChunks)
			.setSaveConsumer { config = config.copy(maxChunks = it) }
			.setTooltip(provider.getMessage(SkylineMessageKey.Config.Settings.MaxChunks.Tooltip))
			.build()
		)

		builder.setSavingRunnable { saveConfig() }
		return builder.build()
	}

	fun load() {
		ConfigFileManager.loadConfig()
	}

	private fun saveConfig() {
		ConfigFileManager.saveConfig()
	}

	object ConfigFileManager {
		private val configFile: Path = SL.modDir.resolve("config.json")

		fun loadConfig() {
			if (!Files.exists(configFile)) {
				saveConfig()
				return
			}

			try {
				Files.newBufferedReader(configFile).use {
					config = gson.fromJson(it, Config::class.java)
				}
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}

		fun saveConfig() {
			try {
				if (!Files.exists(configFile)) {
					Files.createFile(configFile)
				}
				Files.write(configFile, gson.toJson(config).toByteArray())
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
	}

	enum class ChunkSize(val size: Size) {
		SMALL(Size(16, 16)),
		SMALL_MEDIUM(Size(32, 32)),
		MEDIUM(Size(64, 64)),
		MEDIUM_LARGE(Size(128, 128)),
		LARGE(Size(256, 256)),
		HUGE(Size(512, 512));

		companion object {
			fun of(x: Int, z: Int): ChunkSize {
				return entries.firstOrNull { it.size.x == x && it.size.z == z } ?: MEDIUM
			}
		}

		val displayName: Text
			get() = Text.of("${size.x}×${size.z}").copy().styled { it.withBold(true) }
	}

	data class Size(
		val x: Int,
		val z: Int
	)

	data class Config(
		@JvmField
		val renderDistance: Int = 64,
		@JvmField
		val useLOD: Boolean = true,
		@JvmField
		val lodThreshold: Int = 128,
		@JvmField
		val chunkSizeX: Int = 16,
		@JvmField
		val chunkSizeZ: Int = 16,
		@JvmField
		val maxChunks: Int = 1024
	) {
		fun getChunksPerCell(): Int = (chunkSizeX / 16) * (chunkSizeZ / 16)
	}
}