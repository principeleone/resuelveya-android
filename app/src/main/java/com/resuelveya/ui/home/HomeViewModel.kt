package com.resuelveya.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resuelveya.model.Especialidad
import com.resuelveya.network.RetrofitClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _especialidades = MutableLiveData<List<Especialidad>>()
    val especialidades: LiveData<List<Especialidad>> = _especialidades

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var cargando = false

    fun cargarEspecialidades(forzar: Boolean = false) {
        // Ya hay datos: no llamar al servidor
        if (!forzar && _especialidades.value != null) return
        // Ya hay una petición en curso: no lanzar otra
        if (cargando) return

        cargando = true
        viewModelScope.launch {
            try {
                _especialidades.value = RetrofitClient.api.getEspecialidades()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error: ${e::class.simpleName} - ${e.message}", e)
                _error.value = "No se pudo conectar con el servidor"
            } finally {
                cargando = false
            }
        }
    }

    // El fragment lo llama después de mostrar el mensaje, para no repetirlo al volver
    fun errorMostrado() {
        _error.value = null
    }
}