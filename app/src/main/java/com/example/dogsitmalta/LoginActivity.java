package com.example.dogsitmalta;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //connect the Forgot password TextView to the id in the Layout file
        TextView forgotPass = findViewById(R.id.forgotpass);

        //connect the Email EditText to the id in the Layout file
        EditText emailField = findViewById(R.id.logemail);

        forgotPass.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,
                        ForgotPasswordActivity.class);
                    intent.putExtra("email", emailField.getText().toString());
                startActivity(intent);
            }
        });
    }
}
