package com.example.zelo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {

    @Query("SELECT * FROM pets ORDER BY nome")
    fun observarTodos(): Flow<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE tutorId = :tutorId ORDER BY nome")
    fun observarPorTutor(tutorId: Int): Flow<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE id = :id")
    suspend fun buscarPorId(id: Int): PetEntity?

    @Insert
    suspend fun inserir(pet: PetEntity): Long

    @Update
    suspend fun atualizar(pet: PetEntity): Int

    @Query("DELETE FROM pets WHERE id = :id")
    suspend fun excluir(id: Int): Int
}