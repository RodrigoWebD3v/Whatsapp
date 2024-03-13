package com.example.whatsapp.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.whatsapp.fragments.ContatosFragment
import com.example.whatsapp.fragments.ConversasFragment

class ViewPagerAdapter(
    private val abas: List<String>,
    fragmentManager : FragmentManager,
    lifeCycle : Lifecycle
): FragmentStateAdapter(fragmentManager,lifeCycle ) {
    override fun getItemCount(): Int {
        return abas.size
    }

    override fun createFragment(position: Int): Fragment {
        when(position){
           1 ->  return ContatosFragment()
        }
        return ConversasFragment()
    }


}