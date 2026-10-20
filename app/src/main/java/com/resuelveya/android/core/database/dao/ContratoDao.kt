package com.resuelveya.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.resuelveya.android.core.database.entity.ContratoEntity

@Dao
interface ContratoDao {

    @Query("SELECT * FROM contratos WHERE esProveedor = :esProveedor ORDER BY updatedAt DESC")
    fun getContratosByRol(esProveedor: Boolean): List<ContratoEntity>

    @Query("SELECT * FROM contratos WHERE id = :id LIMIT 1")
    fun getContratoById(id: Long): ContratoEntity?

    @Query("SELECT * FROM contratos WHERE syncStatus != 'SYNCED'")
    fun getPendingSyncContratos(): List<ContratoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(contratos: List<ContratoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(contrato: ContratoEntity)

    @Update
    fun update(contrato: ContratoEntity)

    @Query("UPDATE contratos SET estado = :nuevoEstado, syncStatus = :syncStatus, updatedAt = :updatedAt WHERE id = :id")
    fun updateEstado(id: Long, nuevoEstado: String, syncStatus: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM contratos WHERE esProveedor = :esProveedor AND syncStatus = 'SYNCED'")
    fun clearSyncedByRol(esProveedor: Boolean)
}
