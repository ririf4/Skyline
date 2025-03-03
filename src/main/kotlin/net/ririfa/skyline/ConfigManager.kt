package net.ririfa.skyline

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigCategory
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import net.ririfa.skyline.translation.SkylineMessageKey
import java.nio.file.Files
import java.nio.file.Path

object ConfigManager {
	@JvmField
	var config = Config()
	private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

	fun createConfigScreen(parent: Screen): Screen {
		val builder = ConfigBuilder.create().setParentScreen(parent).setTitle(provider.getMessage(SkylineMessageKey.Config.Title))
		val entryBuilder = builder.entryBuilder()
		val category: ConfigCategory = builder.getOrCreateCategory(provider.getMessage(SkylineMessageKey.Config.Category.General))

		category.addEntry(entryBuilder.startIntField(provider.getMessage(SkylineMessageKey.Config.Settings.RenderDistance.Name), config.renderDistance)
			.setDefaultValue(64)
			.setSaveConsumer { config = config.copy(renderDistance = it) }
			.setTooltip(provider.getMessage(SkylineMessageKey.Config.Settings.RenderDistance.Tooltip))
			.build()
		)

		category.addEntry(entryBuilder.startDoubleField(provider.getMessage(SkylineMessageKey.Config.Settings.CacheMaxDistance.Name), config.ssboChunkMultiplier)
			.setDefaultValue(2.0)
			.setSaveConsumer { config = config.copy(ssboChunkMultiplier = it) }
			.setTooltip(provider.getMessage(SkylineMessageKey.Config.Settings.CacheMaxDistance.Tooltip))
			.build()
		)

		category.addEntry(entryBuilder.startBooleanToggle(provider.getMessage(SkylineMessageKey.Config.Settings.UseLOD.Name), config.useLOD)
			.setDefaultValue(true)
			.setSaveConsumer { config = config.copy(useLOD = it) }
			.setTooltip(provider.getMessage(SkylineMessageKey.Config.Settings.UseLOD.Tooltip))
			.build()
		)

		category.addEntry(entryBuilder.startIntField(provider.getMessage(SkylineMessageKey.Config.Settings.LODThreshold.Name), config.lodThreshold)
			.setDefaultValue(128)
			.setSaveConsumer { config = config.copy(lodThreshold = it) }
			.setTooltip(provider.getMessage(SkylineMessageKey.Config.Settings.LODThreshold.Tooltip))
			.build()
		)

		category.addEntry(
			entryBuilder.startEnumSelector(
				provider.getMessage(SkylineMessageKey.Config.Settings.ChunkSizeXZ.Name),
				ChunkSize::class.java,
				ChunkSize.MEDIUM
			)
				.setDefaultValue(ChunkSize.MEDIUM)
				.setEnumNameProvider { (it as ChunkSize).displayName }
				.setSaveConsumer { config = config.copy(chunkSizeX = it.size[0], chunkSizeZ = it.size[1]) }
				.setTooltip(provider.getMessage(SkylineMessageKey.Config.Settings.ChunkSizeXZ.Tooltip))
				.build()
		)

		category.addEntry(entryBuilder.startIntField(provider.getMessage(SkylineMessageKey.Config.Settings.MaxChunks.Name), config.maxChunks)
			.setDefaultValue(1024)
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
				val reader = Files.newBufferedReader(configFile)
				config = gson.fromJson(reader, Config::class.java)
				reader.close()
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

	enum class ChunkSize(val size: IntArray) {
		SMALL(intArrayOf(8, 8)),
		MEDIUM(intArrayOf(16, 16)),
		LARGE(intArrayOf(32, 32));

		val displayName: Text
			get() = Text.of("${size[0]}×256×${size[1]}")
	}


	data class Config(
		@JvmField
		val renderDistance: Int = 64,
		@JvmField
		val ssboChunkMultiplier: Double = 2.0,
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
	)
}