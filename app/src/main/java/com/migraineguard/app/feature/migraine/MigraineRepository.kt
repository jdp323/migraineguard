package com.migraineguard.app.feature.migraine

import com.migraineguard.app.core.data.db.MigraineDao
import com.migraineguard.app.core.data.db.MigraineEpisodeEntity
import com.migraineguard.app.core.domain.MigraineEpisode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MigraineRepository(private val dao: MigraineDao) {
    fun observeEpisodes(): Flow<List<MigraineEpisode>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getEpisode(id: Long): MigraineEpisode? = dao.getById(id)?.toDomain()

    suspend fun save(episode: MigraineEpisode): Long =
        dao.insert(MigraineEpisodeEntity.fromDomain(episode.copy(id = 0)))

    suspend fun delete(id: Long) = dao.delete(id)

    suspend fun inRange(startMillis: Long, endMillis: Long): List<MigraineEpisode> =
        dao.inRange(startMillis, endMillis).map { it.toDomain() }
}
