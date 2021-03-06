package com.example.tracknowdemo.ui.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tracknowdemo.DashboardMain;
import com.example.tracknowdemo.R;
import com.example.tracknowdemo.databinding.ActivityLoginBinding;
import com.example.tracknowdemo.models.Users;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    ImageView image;
    TextView welcomeText, logo_desc;
    TextInputLayout username, pass;
    Button signupButton, loginButton;
    ProgressDialog progressDialog;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();

//        getSupportActionBar().hide();
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configure Google Sign In
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso);
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Logging Into Account...");
        binding.googleLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        binding.signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginToSignupScreenTransition();
            }
        });

        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.emailTextField.getText().toString();
                String pass = binding.passwordTextField.getText().toString();
                if (email.matches("") && pass.matches("")) {
                    binding.email.setError("You need to enter a valid email.");
                    binding.password.setError("You have to enter correct password.");
                } else if (email.matches("")) {
                    binding.email.setError("You need to enter a valid email.");
                } else if (pass.matches("") || pass.length() < 6) {
                    binding.password.setError("You have to enter correct password(at least 6 characters).");
                } else {
                    progressDialog.show();
                    auth.signInWithEmailAndPassword(email, pass).
                            addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        progressDialog.dismiss();
                                        Intent intent = new Intent(LoginActivity.this, DashboardMain.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(LoginActivity.this, "User is not valid.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }

            }
        });

    }
    int RC_SIGN_IN=65;
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
                // ...
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        progressDialog.show();
        database = FirebaseDatabase.getInstance();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            user = auth.getCurrentUser();
                            Users users=new Users();
                            users.setUserId(user.getUid());
                            users.setProfilePic(user.getPhotoUrl().toString());
                            users.setFullName(user.getDisplayName());
                            users.setEmail(user.getEmail());
                            database.getReference("Users").child(user.getUid()).setValue(users);
                            progressDialog.dismiss();
                            Intent intent = new Intent(LoginActivity.this, DashboardMain.class);
                            startActivity(intent);
                            finish();
//                            updateUI(user);
                        } else {
                            progressDialog.dismiss();
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            Snackbar.make(binding.getRoot(), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
//                            updateUI(null);
                        }

                        // ...
                    }
                });
    }
    void loginToSignupScreenTransition() {
        pass = binding.password;
        image = binding.logoImage;
        welcomeText = binding.welcomeText;
        logo_desc = binding.logoDescription;
        signupButton = binding.signupButton;
        loginButton = binding.loginButton;

        Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
        Pair[] pairs = new Pair[6];
        pairs[0] = new Pair<View, String>(image, "logo_image");
        pairs[1] = new Pair<View, String>(welcomeText, "welcomeText");
        pairs[2] = new Pair<View, String>(logo_desc, "logo_desc");
        pairs[3] = new Pair<View, String>(pass, "password_tran");
        pairs[4] = new Pair<View, String>(loginButton, "button_tran");
        pairs[5] = new Pair<View, String>(signupButton, "login_signup_tran");
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this, pairs);
        startActivity(intent, options.toBundle());
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}