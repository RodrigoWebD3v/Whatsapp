package com.example.whatsapp.activities

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.whatsapp.R
import com.example.whatsapp.databinding.ActivityCadastroBinding
import com.example.whatsapp.databinding.ActivityMensagensBinding
import com.example.whatsapp.model.Mensagem
import com.example.whatsapp.model.Usuario
import com.example.whatsapp.utils.Constantes
import com.example.whatsapp.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso

class MensagensActivity : AppCompatActivity() {
    private val binding by lazy{
        ActivityMensagensBinding.inflate(layoutInflater)
    }
    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val fireStore by lazy {
        FirebaseFirestore.getInstance()
    }

    private lateinit var listenerRegistration : ListenerRegistration

    private var dadosDestinatario : Usuario? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        recuperarDadosDestinatario()
        inicializarToolbar()
        inicializarEventosClick()
        inicializarListiners()
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration.remove()
    }

    private fun inicializarListiners(){
        val idUsuarioRemetente = firebaseAuth.currentUser?.uid
        val idUsuarioDestinatario = dadosDestinatario?.id

        if(idUsuarioRemetente != null && idUsuarioDestinatario != null){
            listenerRegistration = fireStore.collection(Constantes.COLECAO_MENSAGENS)
               .document(idUsuarioRemetente)
               .collection(idUsuarioDestinatario)
               .orderBy("data", Query.Direction.ASCENDING)
               .addSnapshotListener{
                   querySnapshot, erro ->
                   if(erro != null){
                       exibirMensagem("Erro ao recuperar mensagens")
                   }

                   val documentos =   querySnapshot?.documents
                   val listaMensagens = mutableListOf<Mensagem>()
                   documentos?.forEach{
                       documentSnapshot -> val mensagem = documentSnapshot.toObject(Mensagem::class.java)
                       if(mensagem != null){
                            listaMensagens.add(mensagem)
                           Log.i("exibicao_mensagens", mensagem.mensagem)
                       }
                   }

                   if(listaMensagens.isNotEmpty()){

                   }
               }
        }
    }

    private fun inicializarEventosClick() {
        binding.btnEnviarMensagem.setOnClickListener{
            val mensagem = binding.editTextMensagem.text.toString()
            salvarMensagem(mensagem)
        }
    }

    private fun salvarMensagem(textoMensagem: String) {


        if(textoMensagem.isNotEmpty()){



           val idUsuarioRemetente = firebaseAuth.currentUser?.uid
           val idUsuarioDestinatario = dadosDestinatario?.id

        if(idUsuarioRemetente != null && idUsuarioDestinatario != null){
            val mensagem = Mensagem(
                idUsuarioRemetente,
                textoMensagem,
            )
            salvarMensagemFirestore(idUsuarioRemetente, idUsuarioDestinatario, mensagem)

            salvarMensagemFirestore(idUsuarioDestinatario, idUsuarioRemetente, mensagem)

            binding.editTextMensagem.setText("")
        }
        }
    }

    private fun salvarMensagemFirestore(idUsuarioRemetente : String,
                                        idUsuarioDestinatario: String,
                                        mensagem: Mensagem ) {
        fireStore.collection(Constantes.COLECAO_MENSAGENS)
            .document(idUsuarioRemetente)
            .collection(idUsuarioDestinatario)
            .add(mensagem)
            .addOnFailureListener {
                exibirMensagem("Erro ao enviar mensagem")
            }
    }


    private fun inicializarToolbar() {
        val toolbar = binding.tbMensagens
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = ""
            if(dadosDestinatario != null){

                binding.textNome.text = dadosDestinatario!!.nome
                Picasso.get()
                    .load(dadosDestinatario!!.foto)
                    .into(binding.imageFotoPerfil)
            }
            setDisplayHomeAsUpEnabled(true)
        }
    }


    private fun recuperarDadosDestinatario() {
        val extras =  intent.extras
        if(extras != null){
            val origem = extras.getString("origem")
            if(origem == Constantes.ORIGEM_CONTATO){
                dadosDestinatario = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    extras.getParcelable("dadosDestinatario", Usuario::class.java)
                }else{
                    extras.getParcelable("dadosDestinatario")
                }


            }else if(origem == Constantes.ORIGEM_CONVERSA){

            }
        }
    }
}