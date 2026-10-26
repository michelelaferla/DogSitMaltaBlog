package com.example.dogsitmalta;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dogsitmalta.database.DatabaseHelper;
import com.example.dogsitmalta.model.User;
import com.example.dogsitmalta.sync.SyncManager;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Arrays;

/** Local offline demo login; Firebase Auth verifies online identity when configured. */
public class LoginActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);setContentView(R.layout.activity_login);
        EditText emailField=findViewById(R.id.logemail), passwordField=findViewById(R.id.logpass);
        Button login=findViewById(R.id.logbut);
        login.setOnClickListener(v->{
            String email=emailField.getText().toString().trim().toLowerCase(java.util.Locale.ROOT);
            char[] password=passwordField.getText().toString().toCharArray();
            if(SyncManager.isConfigured(this)) {
                login.setEnabled(false);
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email,new String(password))
                    .addOnCompleteListener(result->{
                        if(result.isSuccessful()) {
                            try(DatabaseHelper db=new DatabaseHelper(this)) {
                                db.attachFirebaseUid(email,FirebaseAuth.getInstance().getUid());
                            }
                            openMain();
                        }else Toast.makeText(this,"Invalid Firebase credentials",Toast.LENGTH_LONG).show();
                        Arrays.fill(password,'\0');login.setEnabled(true);
                    });
            }else {
                try(DatabaseHelper db=new DatabaseHelper(this)) {
                    User user=db.authenticateLocal(email,password);
                    if(user!=null)openMain();
                    else Toast.makeText(this,"Invalid local credentials",Toast.LENGTH_LONG).show();
                }
                Arrays.fill(password,'\0');
            }
        });
        TextView forgot=findViewById(R.id.forgotpass);
        forgot.setOnClickListener(v->{
            Intent intent=new Intent(this,ForgotPasswordActivity.class);
            intent.putExtra("email",emailField.getText().toString());startActivity(intent);
        });
    }
    private void openMain() {startActivity(new Intent(this,MainActivity.class));finish();}
}
