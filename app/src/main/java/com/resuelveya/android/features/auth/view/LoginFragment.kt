package com.resuelveya.android.features.auth.view


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.resuelveya.android.R
import com.resuelveya.android.databinding.FragmentLoginBinding

class LoginFragment : Fragment(),View.OnClickListener {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        auth= FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener (this)
        binding.btnIrRegistro.setOnClickListener (this)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun loginUser(email: String,password:String){
        auth.signInWithEmailAndPassword(email,password)
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
            binding.btnLogin->{
                loginUser(binding.etCorreo.toString().trim(),
                    binding.etPassword.text.toString().trim()
                )
            }
            binding.btnIrRegistro->{
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }
        }
    }
}