package com.example.teachassist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {
    private FirebaseAuth mAuth;
    private ImageView backButton, profileImage, nightModeIcon, notificationIcon, securityIcon, textSizeIcon, languageIcon, messageIcon, aboutUsIcon, faqIcon, logOutIcon;
    private SwitchCompat nightModeSwitch, notificationSwitch;
    private RelativeLayout editProfile, securityPrivacy, textSize, languages, messages, aboutUs, faqs, logOut;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize views
        backButton = view.findViewById(R.id.backButton); // Change to: R.id.backbutton
        profileImage = view.findViewById(R.id.profileImage); // Change to: R.id.file (from the src drawable in the ImageView)
        nightModeIcon = view.findViewById(R.id.nightModeIcon); // Correct
        nightModeSwitch = view.findViewById(R.id.nightModeSwitch); // Correct
        notificationIcon = view.findViewById(R.id.notificationIcon); // Correct
        notificationSwitch = view.findViewById(R.id.notificationSwitch); // Change to: No switch ID defined, add switch ID in XML (e.g., android:id="@+id/notificationSwitch")
        securityIcon = view.findViewById(R.id.securityIcon); // Correct
        textSizeIcon = view.findViewById(R.id.textSizeIcon); // Correct
        languageIcon = view.findViewById(R.id.languageIcon); // Correct
        messageIcon = view.findViewById(R.id.messageIcon); // Correct
        aboutUsIcon = view.findViewById(R.id.aboutUsIcon); // Correct
        faqIcon = view.findViewById(R.id.faqIcon); // Correct
        logOutIcon = view.findViewById(R.id.logOutIcon); // Correct

// Buttons functionality
        editProfile = view.findViewById(R.id.editProfileButton); // Change to: R.id.edit_profile (ID not present, add ID in Button XML e.g., android:id="@+id/edit_profile")
        securityPrivacy = view.findViewById(R.id.securityLayout); // Change to: No layout ID defined for security layout, add ID in XML (e.g., android:id="@+id/securityLayout")
        textSize = view.findViewById(R.id.textSizeLayout); // Change to: No layout ID defined for text size layout, add ID in XML (e.g., android:id="@+id/textSizeLayout")
        languages = view.findViewById(R.id.languagesLayout); // Change to: No layout ID defined for languages layout, add ID in XML (e.g., android:id="@+id/languagesLayout")
        messages = view.findViewById(R.id.messagesLayout); // Change to: No layout ID defined for messages layout, add ID in XML (e.g., android:id="@+id/messagesLayout")
        aboutUs = view.findViewById(R.id.aboutUsLayout); // Change to: No layout ID defined for about us layout, add ID in XML (e.g., android:id="@+id/aboutUsLayout")
        faqs = view.findViewById(R.id.faqLayout); // Change to: No layout ID defined for FAQ layout, add ID in XML (e.g., android:id="@+id/faqLayout")
        logOut = view.findViewById(R.id.logOutLayout); // Change to: No layout ID defined for log out layout, add ID in XML (e.g., android:id="@+id/logOutLayout")


        // Handle click events
        setupClickListeners();

        return view;
    }

    private void setupClickListeners() {
        // Back Button
        backButton.setOnClickListener(v -> getActivity().onBackPressed());

        // Edit Profile Button
        editProfile.setOnClickListener(v -> {
            // Implement your logic for editing the profile
            Toast.makeText(getContext(), "Edit Profile Clicked", Toast.LENGTH_SHORT).show();
        });

        // Night Mode Toggle
        nightModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Enable Night Mode
                Toast.makeText(getContext(), "Night Mode Enabled", Toast.LENGTH_SHORT).show();
            } else {
                // Disable Night Mode
                Toast.makeText(getContext(), "Night Mode Disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // Notifications Toggle
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Enable Notifications
                Toast.makeText(getContext(), "Notifications Enabled", Toast.LENGTH_SHORT).show();
            } else {
                // Disable Notifications
                Toast.makeText(getContext(), "Notifications Disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // Security & Privacy
        securityPrivacy.setOnClickListener(v -> {
            // Implement your logic for Security & Privacy settings
            Toast.makeText(getContext(), "Security & Privacy Clicked", Toast.LENGTH_SHORT).show();
        });

        // Text Size
        textSize.setOnClickListener(v -> {
            // Implement your logic for Text Size settings
            Toast.makeText(getContext(), "Text Size Clicked", Toast.LENGTH_SHORT).show();
        });

        // Languages
        languages.setOnClickListener(v -> {
            // Implement your logic for Language settings
            Toast.makeText(getContext(), "Languages Clicked", Toast.LENGTH_SHORT).show();
        });

        // Messages
        messages.setOnClickListener(v -> {
            // Implement your logic for Messages
            Toast.makeText(getContext(), "Messages Clicked", Toast.LENGTH_SHORT).show();
        });

        // About Us
        aboutUs.setOnClickListener(v -> {
            // Implement your logic for About Us
            Toast.makeText(getContext(), "About Us Clicked", Toast.LENGTH_SHORT).show();
        });

        // FAQs
        faqs.setOnClickListener(v -> {
            // Implement your logic for FAQs
            Toast.makeText(getContext(), "FAQs Clicked", Toast.LENGTH_SHORT).show();
        });

        // Log Out
        logOut.setOnClickListener(v -> {
            // Implement your logic for logging out
            Toast.makeText(getContext(), "Log Out Clicked", Toast.LENGTH_SHORT).show();
        });
    }
}