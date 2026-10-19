package com.resuelveya.android.features.auth.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.resuelveya.android.R
import com.resuelveya.android.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment(),View.OnClickListener {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        auth= FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupListeners()
        binding.btnRegistrar.setOnClickListener (this)
        binding.btnIrLogin.setOnClickListener (this)
    }

    private fun setupViews() {
        // Inicializar vista con el rol Cliente por defecto
        updateRoleView(isProvider = false)
    }

    private fun setupListeners() {
        binding.toggleRol.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val isProvider = checkedId == R.id.btnRolProveedor
            updateRoleView(isProvider)
        }

    }

    private fun updateRoleView(isProvider: Boolean) {
        if (isProvider) {
            binding.layoutCamposProveedor.visibility = View.VISIBLE
            binding.tvRolInfo.setText(R.string.auth_subtitulo_proveedor)
            binding.btnRegistrar.text = getString(R.string.auth_btn_registrar) + " (PROVEEDOR)"
            return
        }

        binding.layoutCamposProveedor.visibility = View.GONE
        binding.tvRolInfo.setText(R.string.auth_subtitulo_cliente)
        binding.btnRegistrar.text = getString(R.string.auth_btn_registrar) + " (CLIENTE)"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun registerUser(email: String,password:String){
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(requireActivity()){task ->
                if (task.isSuccessful){
                    Toast.makeText(requireContext(),"OK", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(requireContext(),"Error", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onClick(v: View?) {
        when(v){
            binding.btnRegistrar->{
                registerUser(binding.etCorreoReg.toString().trim(),
                    binding.etPasswordReg.text.toString().trim()
                )
            }
            binding.btnIrLogin->{
                findNavController().popBackStack()
            }
        }
    }
}