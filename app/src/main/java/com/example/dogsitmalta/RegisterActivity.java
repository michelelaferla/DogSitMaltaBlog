package com.example.dogsitmalta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dogsitmalta.database.DatabaseHelper;
import com.example.dogsitmalta.model.User;
import com.example.dogsitmalta.sync.SyncManager;
import com.example.dogsitmalta.utils.PasswordHasher;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Arrays;

/** Registers offline account locally, or Firebase-authenticated account when configured. */
public class RegisterActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);setContentView(R.layout.activity_register);
        EditText first=findViewById(R.id.name),surname=findViewById(R.id.surname),
                emailField=findViewById(R.id.logemail),passField=findViewById(R.id.logpass);
        Button button=findViewById(R.id.regbut);
        button.setOnClickListener(v->{
            String name=first.getText().toString().trim(),last=surname.getText().toString().trim(),
                    email=emailField.getText().toString().trim().toLowerCase(java.util.Locale.ROOT);
            char[] password=passField.getText().toString().toCharArray();
            if(name.isEmpty()||last.isEmpty()||!Patterns.EMAIL_ADDRESS.matcher(email).matches()||password.length<8) {
                Toast.makeText(this,"Enter name, surname, valid email, password (8+ chars)",Toast.LENGTH_LONG).show();
                Arrays.fill(password,'\0');return;
            }
            try(DatabaseHelper db=new DatabaseHelper(this)) {
                if(db.findUser(email)!=null){
                    Toast.makeText(this,"Email already registered on this device",Toast.LENGTH_LONG).show();
                    Arrays.fill(password,'\0');return;
                }
            }
            button.setEnabled(false);
            if(SyncManager.isConfigured(this)) {
                String authPassword=new String(password);
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,authPassword)
                    .addOnCompleteListener(task->{
                        if(task.isSuccessful()) {
                            String uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
                            saveLocal(name,last,email,password,uid);
                            new com.example.dogsitmalta.sync.SyncManager(this).sync(null);
                        }else Toast.makeText(this,"Firebase registration failed: " +
                                task.getException().getMessage(),Toast.LENGTH_LONG).show();
                        Arrays.fill(password,'\0');button.setEnabled(true);
                    });
            } else {
                saveLocal(name,last,email,password,null);
                Arrays.fill(password,'\0');button.setEnabled(true);
            }
        });
    }
    private void saveLocal(String name,String surname,String email,char[] password,String uid) {
        User user=new User();user.setUserName(name);user.setUserSurname(surname);
        user.setUserEmail(email);user.setUserPassword(PasswordHasher.hash(password));
        user.setUserIsActive(true);user.setFirebaseUid(uid);
        try(DatabaseHelper db=new DatabaseHelper(this)){db.insertUser(user);}
        catch(Exception e) {Toast.makeText(this,"Could not save account",Toast.LENGTH_LONG).show();return;}
        Toast.makeText(this,uid==null?"Offline account created":"Account created",Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this,LoginActivity.class));finish();
    }
}
