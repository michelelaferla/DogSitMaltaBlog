package com.example.dogsitmalta;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dogsitmalta.database.repos.RoomUserRepository;
import com.example.dogsitmalta.model.User;
import com.example.dogsitmalta.sync.SyncManager;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Local Room login for teaching; Firebase Auth handles online identity when configured. */
public class LoginActivity extends AppCompatActivity {
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state); setContentView(R.layout.activity_login);
        EditText emailField=findViewById(R.id.logemail), passwordField=findViewById(R.id.logpass);
        Button login=findViewById(R.id.logbut);
        RoomUserRepository users = new RoomUserRepository(this);
        login.setOnClickListener(v -> {
            String email=emailField.getText().toString().trim().toLowerCase(java.util.Locale.ROOT);
            char[] password=passwordField.getText().toString().toCharArray();
            login.setEnabled(false);
            if (SyncManager.isConfigured(this)) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, new String(password))
                    .addOnCompleteListener(result -> {
                        Arrays.fill(password, '\0');
                        if (!result.isSuccessful()) {
                            Toast.makeText(this,"Invalid Firebase credentials",Toast.LENGTH_LONG).show();
                            login.setEnabled(true);
                            return;
                        }
                        String uid = FirebaseAuth.getInstance().getUid();
                        worker.execute(() -> {
                            try { users.attachFirebaseUid(email, uid); runOnUiThread(this::openMain); }
                            catch (Exception e) { runOnUiThread(() -> {
                                Toast.makeText(this,"Could not update local account",Toast.LENGTH_LONG).show();
                                login.setEnabled(true);
                            }); }
                        });
                    });
            } else {
                worker.execute(() -> {
                    boolean valid = false;
                    try { User user = users.authenticateLocal(email, password); valid = user != null; }
                    finally { Arrays.fill(password, '\0'); }
                    boolean success = valid;
                    runOnUiThread(() -> {
                        if (success) openMain();
                        else { Toast.makeText(this,"Invalid local credentials",Toast.LENGTH_LONG).show();
                            login.setEnabled(true); }
                    });
                });
            }
        });
        TextView forgot=findViewById(R.id.forgotpass);
        forgot.setOnClickListener(v -> {
            Intent intent=new Intent(this,ForgotPasswordActivity.class);
            intent.putExtra("email",emailField.getText().toString());startActivity(intent);
        });
    }
    private void openMain() {startActivity(new Intent(this,MainActivity.class));finish();}
    @Override protected void onDestroy() {super.onDestroy();worker.shutdown();}
}
