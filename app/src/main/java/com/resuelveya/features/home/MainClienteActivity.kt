package com.resuelveya.features.home

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView
import com.resuelveya.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class MainClienteActivity : AppCompatActivity() {

    private lateinit var busqueda: EditText
    private lateinit var estado: TextView
    private lateinit var lista: LinearLayout
    private lateinit var scrollServicios: ScrollView
    private lateinit var botonMas: Button

    private var paginaActual = 0
    private var totalPaginas = 1
    private var textoBusqueda = ""
    private var solicitudEnCurso = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_cliente)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { vista, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            vista.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        busqueda = findViewById(R.id.etBuscarServicio)
        estado = findViewById(R.id.tvEstadoServicios)
        lista = findViewById(R.id.contenedorServicios)
        scrollServicios = findViewById(R.id.scrollServicios)
        botonMas = findViewById(R.id.btnCargarMas)

        findViewById<Button>(R.id.btnBuscarServicio).setOnClickListener {
            buscarDesdePrimeraPagina()
        }

        botonMas.setOnClickListener {
            cargarPagina(paginaActual + 1)
        }

        busqueda.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                buscarDesdePrimeraPagina()
                true
            } else {
                false
            }
        }

        cargarPagina(0)
    }

    private fun buscarDesdePrimeraPagina() {
        textoBusqueda = busqueda.text.toString().trim()
        paginaActual = 0
        totalPaginas = 1
        lista.removeAllViews()
        cargarPagina(0)
    }

    private fun cargarPagina(pagina: Int) {
        if (solicitudEnCurso) return

        solicitudEnCurso = true
        estado.text = "Cargando servicios…"
        botonMas.visibility = View.GONE

        ResuelveYaApi.servicios.listarServicios(
            busqueda = textoBusqueda.ifBlank { null },
            pagina = pagina,
            tamano = 10
        ).enqueue(object : Callback<ServiciosPaginados> {

            override fun onResponse(
                call: Call<ServiciosPaginados>,
                response: Response<ServiciosPaginados>
            ) {
                solicitudEnCurso = false
                val resultado = response.body()

                if (response.isSuccessful && resultado != null) {
                    paginaActual = resultado.number
                    totalPaginas = resultado.totalPages

                    resultado.content.forEach { servicio ->
                        lista.addView(crearTarjeta(servicio))
                    }

                    if (pagina == 0) {
                        scrollServicios.post {
                            scrollServicios.scrollTo(0, 0)
                        }
                    }

                    estado.text = if (lista.childCount == 0) {
                        "No se encontraron servicios. Prueba otra búsqueda."
                    } else {
                        "${resultado.totalElements} servicios disponibles"
                    }

                    botonMas.visibility =
                        if (paginaActual + 1 < totalPaginas) View.VISIBLE else View.GONE
                } else {
                    estado.text =
                        "No se pudo cargar el catálogo (HTTP ${response.code()}). Toca Buscar para reintentar."
                }
            }

            override fun onFailure(
                call: Call<ServiciosPaginados>,
                error: Throwable
            ) {
                solicitudEnCurso = false
                estado.text =
                    "No se pudo conectar. Revisa tu conexión y toca Buscar para reintentar."
            }
        })
    }

    private fun crearTarjeta(servicio: ServicioDto): View {
        val tarjeta = MaterialCardView(this).apply {
            radius = dp(14).toFloat()
            cardElevation = dp(2).toFloat()
            setCardBackgroundColor(Color.WHITE)
            strokeColor = Color.rgb(232, 236, 242)
            strokeWidth = dp(1)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }
        }

        val contenido = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
        }

        val categoria =
            servicio.categoriaNombre?.takeIf { it.isNotBlank() } ?: "Servicio"

        contenido.addView(
            texto(
                categoria.uppercase(Locale.getDefault()),
                12f,
                Color.rgb(35, 91, 160),
                true
            )
        )

        contenido.addView(
            texto(servicio.titulo, 17f, Color.rgb(30, 41, 59), true, 4)
        )

        servicio.descripcion?.takeIf { it.isNotBlank() }?.let {
            contenido.addView(
                texto(it, 14f, Color.rgb(95, 105, 119), false, 6)
            )
        }

        val tecnico =
            servicio.tecnicoNombre?.takeIf { it.isNotBlank() } ?: "Técnico"
        val lugar = servicio.tecnicoCiudad
            ?.takeIf { it.isNotBlank() }
            ?.let { " · $it" } ?: ""

        contenido.addView(
            texto(
                "Ofrecido por $tecnico$lugar",
                13f,
                Color.rgb(75, 85, 99),
                false,
                10
            )
        )

        val precio = servicio.precioEstimado?.let {
            NumberFormat.getCurrencyInstance(Locale("es", "PE")).format(it)
        } ?: "Precio por consultar"

        val duracion = servicio.tiempoEstimado
            ?.takeIf { it.isNotBlank() } ?: "Duración por coordinar"

        val puntaje =
            servicio.calificacionTecnico ?: servicio.calificacionPromedio
        val resenias = servicio.totalResenias ?: 0

        val calificacion = puntaje?.let {
            if (resenias > 0) {
                "  ·  ★ ${"%.1f".format(Locale.US, it)} ($resenias)"
            } else {
                "  ·  ★ ${"%.1f".format(Locale.US, it)}"
            }
        } ?: ""

        contenido.addView(
            texto(
                "$precio  ·  $duracion$calificacion",
                14f,
                Color.rgb(38, 73, 120),
                true,
                8
            )
        )

        tarjeta.addView(contenido)
        return tarjeta
    }

    private fun texto(
        valor: String,
        tamano: Float,
        color: Int,
        negrita: Boolean,
        margenSuperior: Int = 0
    ): TextView {
        return TextView(this).apply {
            text = valor
            textSize = tamano
            setTextColor(color)

            if (negrita) {
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }

            if (margenSuperior > 0) {
                setPadding(0, dp(margenSuperior), 0, 0)
            }
        }
    }

    private fun dp(valor: Int): Int =
        (valor * resources.displayMetrics.density).toInt()
}