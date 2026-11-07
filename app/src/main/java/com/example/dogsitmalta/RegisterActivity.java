package com.example.dogsitmalta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dogsitmalta.database.repos.RoomUserRepository;
import com.example.dogsitmalta.model.User;
import com.example.dogsitmalta.sync.SyncManager;
import com.example.dogsitmalta.utils.PasswordHasher;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Registers an offline Room account or a Firebase-backed account. */
public class RegisterActivity extends AppCompatActivity {
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state); setContentView(R.layout.activity_register);
        EditText first=findViewById(R.id.name), surname=findViewById(R.id.surname),
                emailField=findViewById(R.id.logemail), passField=findViewById(R.id.logpass);
        Button button=findViewById(R.id.regbut);
        RoomUserRepository users = new RoomUserRepository(this);
        button.setOnClickListener(v -> {
            String name=first.getText().toString().trim(), last=surname.getText().toString().trim(),
                    email=emailField.getText().toString().trim().toLowerCase(java.util.Locale.ROOT);
            char[] password=passField.getText().toString().toCharArray();
            if (name.isEmpty() || last.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()
                    || password.length < 8) {
                Toast.makeText(this,"Enter name, surname, valid email, password (8+ chars)",Toast.LENGTH_LONG).show();
                Arrays.fill(password, '\0'); return;
            }
            button.setEnabled(false);
            worker.execute(() -> {
                try {
                    if (users.findUser(email) != null) {
                        Arrays.fill(password, '\0');
                        runOnUiThread(() -> { Toast.makeText(this,"Email already registered on this device",
                                Toast.LENGTH_LONG).show(); button.setEnabled(true); });
                        return;
                    }
                    if (SyncManager.isConfigured(this)) {
                        String authPassword = new String(password);
                        runOnUiThread(() -> FirebaseAuth.getInstance()
                                .createUserWithEmailAndPassword(email, authPassword)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        String uid = FirebaseAuth.getInstance().getUid();
                                        saveLocal(users, name, last, email, password, uid, button);
                                    } else {
                                        Arrays.fill(password, '\0');
                                        Toast.makeText(this,"Firebase registration failed",Toast.LENGTH_LONG).show();
                                        button.setEnabled(true);
                                    }
                                }));
                    } else saveLocal(users, name, last, email, password, null, button);
                } catch (Exception e) {
                    Arrays.fill(password, '\0');
                    runOnUiThread(() -> { Toast.makeText(this,"Could not check local accounts",
                            Toast.LENGTH_LONG).show(); button.setEnabled(true); });
                }
            });
        });
    }
    private void saveLocal(RoomUserRepository users, String name, String surname,
                           String email, char[] password, String uid, Button button) {
        worker.execute(() -> {
            try {
                User user=new User(); user.setUserName(name); user.setUserSurname(surname);
                user.setUserEmail(email); user.setUserPassword(PasswordHasher.hash(password));
                user.setUserIsActive(true); user.setFirebaseUid(uid);
                users.insertUser(user);
                runOnUiThread(() -> {
                    Toast.makeText(this,uid==null?"Offline account created":"Account created",
                            Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this,LoginActivity.class));finish();
                    if (uid != null) new SyncManager(this).sync(null);
                });
            } catch (Exception e) {
                runOnUiThread(() -> { Toast.makeText(this,"Could not save account",
                        Toast.LENGTH_LONG).show(); button.setEnabled(true); });
            } finally { Arrays.fill(password, '\0'); }
        });
    }
    @Override protected void onDestroy() {super.onDestroy();worker.shutdown();}
}
