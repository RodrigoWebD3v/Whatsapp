package com.example.whatsapp.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatsapp.activities.MensagensActivity
import com.example.whatsapp.adapters.ContatosAdapter
import com.example.whatsapp.databinding.FragmentContatosBinding
import com.example.whatsapp.model.Usuario
import com.example.whatsapp.utils.Constantes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ContatosFragment : Fragment() {

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val fireStore by lazy {
        FirebaseFirestore.getInstance()
    }

    private lateinit var binding: FragmentContatosBinding
    private lateinit var eventoSnapshot: ListenerRegistration
    private lateinit var contatosAdapter: ContatosAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentContatosBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        contatosAdapter = ContatosAdapter { usuario ->
        val intent = Intent(context, MensagensActivity::class.java)
            intent.putExtra("dadosDestinatario", usuario)
            intent.putExtra("origem", Constantes.ORIGEM_CONTATO)
            startActivity(intent)
        }
        binding.rvContatos.adapter = contatosAdapter
        binding.rvContatos.layoutManager = LinearLayoutManager(context)
        binding.rvContatos.addItemDecoration(
            DividerItemDecoration(
                context, LinearLayout.VERTICAL
            )
        )


        return binding.root

    }

    override fun onStart() {
        super.onStart()
        adicionarListinerContatos()
    }

    private fun adicionarListinerContatos() {
       eventoSnapshot =  fireStore.collection("usuarios")
            .addSnapshotListener{
            querySnapshot, erro ->
                val documents = querySnapshot?.documents
                val listaContatos = mutableListOf<Usuario>()
                documents?.forEach{ documentsSnapshots ->

                    val usuario = documentsSnapshots.toObject(Usuario::class.java)
                    val idUsuario = firebaseAuth.currentUser?.uid
                    if(usuario != null && idUsuario != null  && idUsuario != usuario.id){
                            Log.i("Fragmento_contatos", "nome ${usuario.nome}")
                            listaContatos.add(usuario)
                    }
                    if(listaContatos.isNotEmpty()){
                        contatosAdapter.adicionarLista(listaContatos)

                    }

                }
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        eventoSnapshot.remove()
    }

}