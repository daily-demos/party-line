package com.daily.partyline

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.daily.partyline.databinding.FragmentJoinBinding

class JoinFragment : Fragment() {
    lateinit var binding : FragmentJoinBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentJoinBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Post view initialization logic
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.learnMore.movementMethod = LinkMovementMethod.getInstance()
        val navController = Navigation.findNavController(view)
        val toRoom: JoinFragmentDirections.ActionJoinFragmentToRoomFragment = JoinFragmentDirections.actionJoinFragmentToRoomFragment()
        binding.roomUrl.editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int,
                                       before: Int, count: Int) {
                if (s?.length != 0) {
                    binding.joinButton.text = "Join room"
                } else {
                    binding.joinButton.text = "Create and join room"
                }
            }
        })
        binding.joinButton.setOnClickListener { sender: View? ->
            val userName = getUserName()
            val roomUrl = getRoomCode()
            if (userName?.isEmpty() == false && roomUrl?.isEmpty() == true) {
                toRoom.setUserName(userName)
                toRoom.setRoomUrl(null)
                dismissKeyboard()
                navController.navigate(toRoom)
            } else if (userName?.isEmpty() == false && roomUrl?.isEmpty() == false) {
                toRoom.setUserName(userName)
                toRoom.setRoomUrl(roomUrl)
                dismissKeyboard()
                navController.navigate(toRoom)
            }
        }
    }

    //region utils
    private fun getUserName(): String? {
        val firstName = binding.firstName.editText?.getText().toString().replace(" ", "")
        val lastName = binding.lastName.editText?.getText().toString().replace(" ", "")
        return (if (firstName == "") "Test" else firstName) + if (lastName == "") "" else " $lastName"
    }

    private fun getRoomCode(): String? {
        return binding.roomUrl.editText?.getText().toString().trim()
    }

    private fun dismissKeyboard() {
        requireActivity().runOnUiThread {
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (imm.isAcceptingText) { // verify if the soft keyboard is open
                imm.hideSoftInputFromWindow(requireActivity().currentFocus.windowToken, 0)
            }
        }
    } //endregion utils
}