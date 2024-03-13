package com.example.whatsapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.whatsapp.R
import com.example.whatsapp.data_fields.UsuarioDF
import com.example.whatsapp.databinding.ActivityCadastroBinding
import com.example.whatsapp.model.Usuario
import com.example.whatsapp.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

class CadastroActivity : AppCompatActivity() {
    private val binding by lazy{
        ActivityCadastroBinding.inflate(layoutInflater)
    }

    private lateinit var nome: String
    private lateinit var email: String
    private lateinit var senha: String

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val fireStore by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        inicializarToolbar()
        inicializarEventosClique()
    }

    private fun inicializarToolbar() {
        val toolbar = binding.includeToolbar.tbPrincipal
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Faca o seu cadastro"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun inicializarEventosClique() {
        binding.btnCadastrar.setOnClickListener {
            if(validarCampos()){
                cadastrarUsuario(nome, email, senha)
            }
        }
    }

    private fun cadastrarUsuario(nome: String, email: String, senha: String) {

        firebaseAuth.createUserWithEmailAndPassword(email, senha).addOnCompleteListener{
            resultado -> if(resultado.isSuccessful){

                val idUsuario = resultado.result.user?.uid
                if(idUsuario != null){
                    val usuario = Usuario(
                        idUsuario, nome, email
                    )
                    salvarUsuarioFirestore(usuario)
                }


            }
        }.addOnFailureListener {erro ->
            try{
                throw erro
            }catch (erroSenhaFraca : FirebaseAuthWeakPasswordException){
                erroSenhaFraca.printStackTrace()
                exibirMensagem(getString(R.string.error_weak_password))
            }catch (erroCredenciaisInvalidas : FirebaseAuthInvalidCredentialsException){
                erroCredenciaisInvalidas.printStackTrace()
                exibirMensagem(getString(R.string.error_invalid_email))
            }catch (erroUsuarioExistente : FirebaseAuthUserCollisionException){
                erroUsuarioExistente.printStackTrace()
                exibirMensagem(getString(R.string.error_existing_user))
            }
        }
    }

    private fun salvarUsuarioFirestore(usuario: Usuario) {
        fireStore.collection(UsuarioDF.USUARIOS_COLLECTION)
            .document(usuario.id)
            .set(usuario)
            .addOnSuccessListener{
                exibirMensagem(getString(R.string.success_registration))
                startActivity(Intent(applicationContext, MainActivity::class.java))
            }.addOnFailureListener {
                exibirMensagem(getString(R.string.error_registration))
            }
    }

    private fun validarCampos(): Boolean {

        nome = binding.inputNome.text.toString()
        email = binding.inputEmail.text.toString()
        senha = binding.inputSenha.text.toString()

        if(nome.isNotEmpty()){
            binding.textInputNome.error = null
            if(email.isNotEmpty()){
                binding.textInputEmail.error = null
                if(senha.isNotEmpty()){
                    binding.textInputSenha.error = null
                    return true
                }else{
                    binding.textInputSenha.error = getString(R.string.error_empty_password)
                    return false
                }
            }else{
                binding.textInputEmail.error = getString(R.string.error_empty_email)
                return false
            }
        }else{
            binding.textInputNome.error = getString(R.string.error_empty_name)
            return false
        }
    }


}