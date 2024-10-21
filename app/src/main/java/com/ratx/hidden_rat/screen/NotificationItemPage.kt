package com.ratx.hidden_rat.screen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ratx.hidden_rat.model.NotificationModel
import com.ratx.hidden_rat.databinding.NotificationItemPageBinding

class NotificationItemPage (val list:MutableList<NotificationModel>) : Fragment()
{
    private var _binding: NotificationItemPageBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = NotificationItemPageBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }
}