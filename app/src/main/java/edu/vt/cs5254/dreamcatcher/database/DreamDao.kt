package edu.vt.cs5254.dreamcatcher.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import edu.vt.cs5254.dreamcatcher.Dream
import edu.vt.cs5254.dreamcatcher.DreamEntry
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface DreamDao {

    //for DLF
    @Query("SELECT * FROM dream d JOIN dream_entry e ON d.id = e.dreamId ORDER BY d.lastUpdated DESC")
    fun getDreams(): Flow<Map<Dream, List<DreamEntry>>>

    //For DDF
    @Query("SELECT * FROM dream WHERE id=(:id)")
    suspend fun internalGetDream(id: UUID): Dream

    @Query("SELECT * FROM dream_entry WHERE dreamId=(:dreamId)")
    suspend fun internalGetEntriesForDream(dreamId: UUID): List<DreamEntry>

    @Transaction
    suspend fun getDreamWithEntries(id: UUID): Dream {

        return internalGetDream(id).apply { entries = internalGetEntriesForDream(id) }
    }

    @Query("DELETE FROM dream_entry WHERE dreamId = (:dreamId)")
    suspend fun internalDeleteEntriesForDream(dreamId: UUID)

    @Insert
    suspend fun internalInsertDreamEntry(dreamEntry: DreamEntry)

    @Update
    suspend fun internalUpdateDream(dream: Dream)

    @Transaction
    suspend fun updateDreamWithEntries(dream: Dream) {

        internalDeleteEntriesForDream(dream.id)
        dream.entries.forEach { internalInsertDreamEntry(it) }
        internalUpdateDream(dream)

    }

    @Insert
    suspend fun internalInsertDream(dream: Dream)

    @Transaction
    suspend fun insertDreamWithEntries(dream: Dream) {
        internalInsertDream(dream)
        dream.entries.forEach { internalInsertDreamEntry(it) }
    }

    @Delete
    suspend fun internalDeleteDream(dream: Dream)

    @Transaction
    suspend fun deleteDreamWithEntries(dream: Dream) {
        internalDeleteEntriesForDream(dream.id)
        internalDeleteDream(dream)
    }

}