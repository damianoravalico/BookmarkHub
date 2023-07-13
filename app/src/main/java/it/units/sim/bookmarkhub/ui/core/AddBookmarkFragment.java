package it.units.sim.bookmarkhub.ui.core;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import it.units.sim.bookmarkhub.R;
import it.units.sim.bookmarkhub.model.Bookmark;
import it.units.sim.bookmarkhub.model.Category;
import it.units.sim.bookmarkhub.repository.FirebaseBookmarkHelper;
import it.units.sim.bookmarkhub.repository.FirebaseCategoriesHelper;

public class AddBookmarkFragment extends Fragment {
    private NavController navController;
    private EditText nameEditText;
    private EditText urlEditText;
    private ArrayAdapter<String> spinnerAdapter;
    private Button addBookmarkButton;
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            boolean isNameEditTextFilled = !nameEditText.getText().toString().isEmpty();
            boolean isUrlEditTextFilled = !urlEditText.getText().toString().isEmpty();
            boolean isSpinnerAdapterFilled = !spinnerAdapter.isEmpty();
            addBookmarkButton.setEnabled(isNameEditTextFilled && isUrlEditTextFilled && isSpinnerAdapterFilled);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_add_bookmark, container, false);
        Spinner spinner = view.findViewById(R.id.bookmarkCategorySpinner);
        spinnerAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item, new ArrayList<>());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        nameEditText = view.findViewById(R.id.bookmarkNameEditText);
        nameEditText.addTextChangedListener(textWatcher);
        urlEditText = view.findViewById(R.id.bookmarkUrlEditText);
        urlEditText.addTextChangedListener(textWatcher);
        new Thread(() -> FirebaseCategoriesHelper.getCategoriesListOfCurrentUser(
                new FirebaseCategoriesHelper.CategoriesCallback() {
                    @Override
                    public void onSuccess(List<Category> category) {
                        AddBookmarkFragment.this.setAdapterSpinnerValues(
                                category.stream()
                                        .map(c -> c.name)
                                        .collect(Collectors.toList()));
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                })).start();
        addBookmarkButton = view.findViewById(R.id.addBookmarkButton);
        addBookmarkButton.setOnClickListener(v ->
                new Thread(() -> FirebaseBookmarkHelper.addNewBookmark(
                        nameEditText.getText().toString(),
                        urlEditText.getText().toString(),
                        spinner.getSelectedItem().toString(),
                        new FirebaseBookmarkHelper.BookmarkCallback() {
                            @Override
                            public void onSuccess(List<Bookmark> bookmark) {
                                AddBookmarkFragment.this.clearViewAndOpenHomeFragment();
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        })).start());
        NavHostFragment navHostFragment =
                (NavHostFragment) requireActivity().getSupportFragmentManager().findFragmentById(R.id.main_nav_host_fragment);
        navController = Objects.requireNonNull(navHostFragment).getNavController();
        navController.addOnDestinationChangedListener((navController, navDestination, bundle) -> resetEditTextViews());
        return view;
    }

    public void clearViewAndOpenHomeFragment() {
        resetEditTextViews();
        Toast.makeText(requireActivity(), "Bookmark inserted successfully", Toast.LENGTH_SHORT).show();
        navController.navigate(AddBookmarkFragmentDirections.actionBookmarkToHomeFragment());
    }

    public void setAdapterSpinnerValues(List<String> values) {
        spinnerAdapter.clear();
        spinnerAdapter.addAll(values);
    }

    private void resetEditTextViews() {
        nameEditText.setText("");
        urlEditText.setText("");
    }

}