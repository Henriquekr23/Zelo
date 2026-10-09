package com.example.zelo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AgendamentoEntity::class,
        PetEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class ZeloDatabase : RoomDatabase() {

    // DAOs
    abstract fun agendamentoDao(): AgendamentoDao

    abstract fun petDao(): PetDao

    companion object {

        @Volatile
        private var INSTANCE: ZeloDatabase? = null

        // Migração da versão 1 para a versão 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `pets` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `nome` TEXT NOT NULL,
                        `especie` TEXT NOT NULL,
                        `raca` TEXT NOT NULL,
                        `tutorId` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        // Migração da versão 2 para a versão 3
        private val MIGRATION_2_3 = object : Migration(2, 3) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                    ALTER TABLE pets
                    ADD COLUMN anoNascimento INTEGER NOT NULL DEFAULT 2022
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    ALTER TABLE pets
                    ADD COLUMN emoji TEXT NOT NULL DEFAULT '🐾'
                    """.trimIndent()
                )
            }
        }

        // Singleton do banco de dados
        fun getInstance(context: Context): ZeloDatabase {

            return INSTANCE ?: synchronized(this) {

                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ZeloDatabase::class.java,
                    "zelo_database"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3
                    )
                    .build()
                    .also {
                        INSTANCE = it
                    }
            }
        }
    }
}