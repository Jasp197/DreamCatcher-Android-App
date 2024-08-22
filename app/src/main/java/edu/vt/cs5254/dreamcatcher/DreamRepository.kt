package edu.vt.cs5254.dreamcatcher

import android.content.Context
import androidx.room.Room
import edu.vt.cs5254.dreamcatcher.database.DreamDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID


private const val DATABASE_NAME = "dream-database"


@OptIn(DelicateCoroutinesApi::class)
class DreamRepository private constructor(
    context: Context,
    private val coroutineScope: CoroutineScope = GlobalScope
) {


    private val database = Room.databaseBuilder(
        context,
        DreamDatabase::class.java,
        DATABASE_NAME
    ).createFromAsset(DATABASE_NAME)
        .build()


    fun getDreams(): Flow<List<Dream>> {
        val dreamMapFlow = database.dreamDao().getDreams()
        return dreamMapFlow.map { dreamMap ->
            dreamMap.keys.map { dream ->
                dream.apply { entries = dreamMap.getValue(dream) }
            }
        }
    }

    suspend fun getDream(id: UUID): Dream {
        return database.dreamDao().getDreamWithEntries(id)
    }

    fun updateDream(dream: Dream){
        coroutineScope.launch {
            database.dreamDao().updateDreamWithEntries(dream)
        }
    }

    suspend fun insertDream(dream: Dream) {
        database.dreamDao().insertDreamWithEntries(dream)
    }

    suspend fun deleteDream(dream: Dream) {
        database.dreamDao().deleteDreamWithEntries(dream)
    }

    companion object {

        private var INSTANCE: DreamRepository? =null

        fun initialize(context: Context) {
            check(INSTANCE == null) {"DreamRepository ALREADY initialized!!!"}
            INSTANCE = DreamRepository(context)
        }

        fun get(): DreamRepository {
            return checkNotNull(INSTANCE) {"DreamRepository NOT YET initialized!!!"}
        }
    }
}