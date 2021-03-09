package com.daily.partyline;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.daily.partyline.databinding.FragmentJoinBinding;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class JoinFragment extends Fragment {

    FragmentJoinBinding binding;

    public JoinFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentJoinBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    // Post view initialization logic
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        binding.learnMore.setMovementMethod(LinkMovementMethod.getInstance());

        NavController navController = Navigation.findNavController(view);
        JoinFragmentDirections.ActionJoinFragmentToRoomFragment toRoom = JoinFragmentDirections.actionJoinFragmentToRoomFragment();

        binding.roomUrl.getEditText().addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (s.length() != 0) {
                    binding.joinButton.setText("Join room");
                }
                else {
                    binding.joinButton.setText("Create and join room");
                }
            }
        });

        binding.joinButton.setOnClickListener((sender) -> {
            String userName = getUserName();
            String roomUrl = getRoomCode();

            if (!userName.matches("") && roomUrl.matches("")) {
                toRoom.setUserName(userName);
                toRoom.setRoomUrl(null);
            }
            else if (!userName.matches("")) {
                toRoom.setUserName(userName);
                toRoom.setRoomUrl(roomUrl);
            }

            dismissKeyboard();
            navController.navigate(toRoom);
        });
    }

    //region utils
    private String getUserName() {
        String firstName = binding.firstName.getEditText().getText().toString().replace(" ","");
        String lastName = binding.lastName.getEditText().getText().toString().replace(" ","");;
        return (firstName.equals("") ? "Test" : firstName) + (lastName.equals("") ? "" : " " + lastName);
    }

    private String getRoomCode() {
        return binding.roomUrl.getEditText().getText().toString().trim();
    }

    private void dismissKeyboard() {
        requireActivity().runOnUiThread(() -> {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(INPUT_METHOD_SERVICE);
            if(imm.isAcceptingText()) { // verify if the soft keyboard is open
                imm.hideSoftInputFromWindow(requireActivity().getCurrentFocus().getWindowToken(), 0);
            }
        });
    }
    //endregion utils
}