package com.example.whatsapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.whatsapp.R
import com.example.whatsapp.databinding.ActivityLoginBinding
import com.example.whatsapp.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : AppCompatActivity() {

    private val binding by lazy{
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private lateinit var email : String
    private lateinit var senha : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        inicializarEventosClique()
    }

    override fun onStart() {
        super.onStart()
        verificaUsuarioLogado()
    }

    private fun verificaUsuarioLogado() {
        val usuarioAtual : String?  = firebaseAuth.currentUser?.uid
        if(usuarioAtual != null){
            startActivity(
                Intent(this, MainActivity::class.java)
            )
        }
    }

    private fun inicializarEventosClique() {
        binding.textCadastro.setOnClickListener{
            startActivity(
                Intent(this, CadastroActivity::class.java)
            )
        }

        binding.btnLogar.setOnClickListener {
            logarUsuario()
        }
    }

    private fun logarUsuario() {
        email = binding.txtInputEmail.text.toString()
        senha = binding.txtInputSenha.text.toString()

        if(verificaCampos(email, senha)){
            firebaseAuth.signInWithEmailAndPassword(email, senha)
                .addOnSuccessListener {
                    exibirMensagem("Usuario logado")
                    startActivity(Intent(this, MainActivity::class.java))
                }.addOnFailureListener {
                        erro ->
                    try{
                        throw erro
                    }
                    catch (erroUsuarioInvalido : FirebaseAuthInvalidUserException){
                        erroUsuarioInvalido.printStackTrace()
                        exibirMensagem("E-mail nao cadastrado")
                    }catch (erroUsuarioInvalido : FirebaseAuthInvalidCredentialsException){
                        erroUsuarioInvalido.printStackTrace()
                        exibirMensagem("E-mail ou senha estão incorretos")
                    }
                }
        }
    }

    private fun verificaCampos(email : String, senha : String) : Boolean {
        Log.i("Variavel conteudo", "$email - $senha")

            if(email.isNotEmpty()){
                binding.textInputLayoutEmail.error = null
                if(senha.isNotEmpty()){
                    binding.textInputLayoutSenha.error = null
                    return true
                }else{
                    binding.textInputLayoutSenha.error = getString(R.string.error_empty_password)
                    return false
                }
            }else{
                binding.textInputLayoutEmail.error = getString(R.string.error_empty_email)
                return false
            }
        }
}