package net.ririfa.skyline.translation

import net.minecraft.text.Text
import net.ririfa.langman.MessageKey

sealed class SkylineMessageKey : MessageKey<SLMSGProvider, Text> {
	sealed class Config : SkylineMessageKey() {
		object OpenConfig : Config()
		object CategoryInSettings : Config()

		object Title : Config()

		sealed class Category : Config() {
			object General : Category()
		}

		sealed class Settings : Config() {
			sealed class RenderDistance : Settings() {
				object Name : RenderDistance()
				object Tooltip : RenderDistance()
			}

			sealed class UseLOD : Settings() {
				object Name : UseLOD()
				object Tooltip : UseLOD()
			}

			sealed class LODThreshold : Settings() {
				object Name : LODThreshold()
				object Tooltip : LODThreshold()
			}

			sealed class ChunkSizeXZ : Settings() {
				object Name : ChunkSizeXZ()
				object Tooltip : ChunkSizeXZ()
			}

			sealed class MaxChunks : Settings() {
				object Name : MaxChunks()
				object Tooltip : MaxChunks()
			}
		}
	}
}