package com.example.dogsitmalta;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dogsitmalta.sync.SyncManager;
import com.google.firebase.auth.FirebaseAuth;

/** The user requests a Firebase reset email; no unverified on-device password override. */
public class ForgotPasswordActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        EditText emailField=findViewById(R.id.email);
        EditText newPass=findViewById(R.id.newPass);
        newPass.setVisibility(View.GONE); // Password is reset using a verified email link.
        String prefilled=getIntent().getStringExtra("email");
        if(prefilled!=null) emailField.setText(prefilled);
        findViewById(R.id.back).setOnClickListener(v->finish());
        findViewById(R.id.reset).setOnClickListener(v->{
            String email=emailField.getText().toString().trim();
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailField.setError("Enter a valid email address");return;
            }
            if(!SyncManager.isConfigured(this)) {
                Toast.makeText(this,"Offline accounts need a separate verified recovery process",Toast.LENGTH_LONG).show();
                return;
            }
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(result->Toast.makeText(this,
                    result.isSuccessful()?"Password reset email requested":"Unable to request reset email",
                    Toast.LENGTH_LONG).show());
        });
    }
}
