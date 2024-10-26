package com.example.teachassist;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditProfileFragment extends Fragment {

    private EditText firstName, lastName, email, password, verifyPassword;
    private ImageView profileImage;
    private Button saveButton, uploadImageButton;
    private Uri imageUri;
    private ImageView backBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        // Initialize views
        backBtn = view.findViewById(R.id.btnBack);  // Fix: use view.findViewById
        firstName = view.findViewById(R.id.firstname);
        lastName = view.findViewById(R.id.lastname);
        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
        verifyPassword = view.findViewById(R.id.verify_password);
        profileImage = view.findViewById(R.id.profile_image);
        saveButton = view.findViewById(R.id.save_button);
        uploadImageButton = view.findViewById(R.id.uploadImageButton);

        // Back button functionality
        backBtn.setOnClickListener(view1 -> getActivity().onBackPressed());  // Fix: go back to the previous screen

        // Load current user info
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String[] nameParts = user.getDisplayName() != null ? user.getDisplayName().split(" ") : new String[]{"", ""};
            firstName.setText(nameParts[0]);
            lastName.setText(nameParts.length > 1 ? nameParts[1] : "");
            email.setText(user.getEmail());
        }

        // Upload profile image
        uploadImageButton.setOnClickListener(v -> selectImage());

        // Save profile changes
        saveButton.setOnClickListener(v -> saveProfile());

        return view;
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData(); // Get the image's Uri
            uploadImageToFirebase(imageUri);  // Upload the image to Firebase Storage
        }
    }


    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri == null) return;

        // Get Firebase Storage reference
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Create a reference for the profile image (using the user's UID ensures a unique file)
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference profileImageRef = storageRef.child("profile_images/" + uid + ".jpg");

        // Upload the file to Firebase Storage
        profileImageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // After the upload is successful, get the image's download URL
                    profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        // Now update the user's profile with this image URL
                        updateUserProfile(downloadUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle any errors here
                    Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProfile() {
        String fName = firstName.getText().toString().trim();
        String lName = lastName.getText().toString().trim();
        String emailText = email.getText().toString().trim();
        String passwordText = password.getText().toString().trim();
        String verifyPasswordText = verifyPassword.getText().toString().trim();

        // Validate input fields
        if (TextUtils.isEmpty(fName) || TextUtils.isEmpty(lName) || TextUtils.isEmpty(emailText)) {
            Toast.makeText(getActivity(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!passwordText.equals(verifyPasswordText)) {
            Toast.makeText(getActivity(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(fName + " " + lName)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Profile Updated", Toast.LENGTH_SHORT).show();
                        }
                    });

            user.updateEmail(emailText).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Email Updated", Toast.LENGTH_SHORT).show();
                }
            });

            // Update password if provided
            if (!TextUtils.isEmpty(passwordText)) {
                user.updatePassword(passwordText).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Password Updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Password update failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Upload the profile image if a new one was selected
            if (imageUri != null) {
                uploadProfileImage(user.getUid(), imageUri);
            }
        }
    }

    private void uploadProfileImage(String userId, Uri imageUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference profileRef = storageRef.child("users/" + userId + "/profile.jpg");

        profileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Save the image URL to Firestore
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("users").document(userId)
                            .update("profileImage", uri.toString())
                            .addOnSuccessListener(aVoid -> Toast.makeText(getActivity(), "Profile Image Updated!", Toast.LENGTH_SHORT).show());
                }))
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Image upload failed", Toast.LENGTH_SHORT).show());
    }

    private void updateUserProfile(String photoUrl) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Create a user profile change request
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(photoUrl))  // Set the new profile image URL
                    .build();

            // Update the user's profile
            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Profile updated successfully
                            Toast.makeText(getContext(), "Profile image updated successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            // Handle any failures here
                            Toast.makeText(getContext(), "Profile update failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
