package com.example.whatsapp.activities

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.whatsapp.databinding.ActivityPerfilBinding
import com.example.whatsapp.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class PerfilActivity : AppCompatActivity() {

    private var temPermissaoCamera = false
    private var temPermissaoGaleria = false


    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val fireStore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val binding by lazy{
        ActivityPerfilBinding.inflate(layoutInflater)
    }

    private val storage by lazy{
        FirebaseStorage.getInstance()
    }


    private val gerenciadorGaleria = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ){
        uri -> if(uri != null){
        binding.imagePerfil.setImageURI(uri)
        uploadImageStorage(uri)
    }else{
        exibirMensagem("Nenhuma imagem selecionada")
    }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        inicializarToolbar()
        solicitarPermissoes()
        inicializarEventosClique()

    }

    override fun onStart() {
        super.onStart()
        recupearDadosIniciais()
    }

    private fun recupearDadosIniciais() {
        val idUsuario : String? = firebaseAuth.currentUser?.uid
        if(idUsuario != null){
            fireStore
                .collection("usuarios")
                .document(idUsuario)
                .get()
                .addOnSuccessListener {
                    documentSnapshot -> val dadosUsuarios = documentSnapshot.data
                    if(dadosUsuarios != null){
                        val nome = dadosUsuarios["nome"] as String
                        val foto = dadosUsuarios["foto"] as String

                        binding.edtTextNome.setText(nome)
                        if(foto.isNotEmpty()){
                            Picasso.get()
                                .load(foto)
                                .into(binding.imagePerfil)
                        }
                    }
                }
        }


    }

    private fun uploadImageStorage(uri: Uri) {

       val idUsuario : String? = firebaseAuth.currentUser?.uid

        if(idUsuario != null){
            storage
                .getReference("fotos")
                .child("usuarios")
                .child(idUsuario)
                .child("perfil.jpg")
                .putFile(uri)
                .addOnSuccessListener {task ->
                    exibirMensagem("Sucesso ao fazer upload da imagem")
                    task.metadata?.reference?.downloadUrl
                        ?.addOnSuccessListener {uri ->
                            val dados = mapOf(
                                "foto" to uri.toString()
                            )
                            atualizarDadosPerfil(dados, idUsuario)
                        }?.addOnFailureListener {
                            exibirMensagem("Erro ao fazer upload da imagem")
                        }
                }.addOnFailureListener {exception ->

                    exibirMensagem("Erro ao fazer upload da imagem" )
                    Log.i("ERRO_IMAGEM", "$exception")
                }
        }


    }

    private fun atualizarDadosPerfil(dados: Map<String, String>, idUsuario: String) {
        fireStore
            .collection("usuarios")
            .document(idUsuario)
            .update(dados)
            .addOnSuccessListener {
                exibirMensagem("Sucesso ao atualizar perfil")
            }
            .addOnFailureListener {
                exibirMensagem("Erro ao atualizar perfil")
            }
    }

    private fun inicializarEventosClique() {

        binding.btnGaleria.setOnClickListener{
            if(temPermissaoGaleria){
                gerenciadorGaleria.launch("image/*")

            }else{
                exibirMensagem("Nao tem permissão para abrir a galeria ")
                solicitarPermissoes()
            }
        }

        binding.btnAtualizar.setOnClickListener {
            val nomeUsuario = binding.edtTextNome.text.toString()

            if(nomeUsuario.isNotEmpty()){

                val  idUsuario = firebaseAuth.currentUser?.uid

                if(idUsuario != null){
                    val dados = mapOf(
                        "nome" to nomeUsuario
                    )
                    atualizarDadosPerfil(dados, idUsuario)
                }
            }
        }
    }

    private fun solicitarPermissoes() {

        temPermissaoCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        temPermissaoGaleria = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED

        val listaPermissoesNegadas = mutableListOf<String>()
        if(!temPermissaoCamera){
            listaPermissoesNegadas.add( Manifest.permission.CAMERA)
        }
        if(!temPermissaoGaleria){
            listaPermissoesNegadas.add( Manifest.permission.READ_MEDIA_IMAGES)
        }

        if(listaPermissoesNegadas.isNotEmpty()){
            val gerenciadorPermissoes = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions())
            {
                    permissoes ->

                temPermissaoCamera = permissoes[Manifest.permission.CAMERA] ?: temPermissaoCamera
                temPermissaoGaleria = permissoes[Manifest.permission.READ_MEDIA_IMAGES] ?: temPermissaoGaleria

            }

            gerenciadorPermissoes.launch(listaPermissoesNegadas.toTypedArray())
        }
    }

    private fun inicializarToolbar() {
        val toolbar = binding.IncludeToolbarPerfil.tbPrincipal
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(true)
        }
    }
}