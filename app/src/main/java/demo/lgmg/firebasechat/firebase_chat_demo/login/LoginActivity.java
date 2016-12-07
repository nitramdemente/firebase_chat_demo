package demo.lgmg.firebasechat.firebase_chat_demo.login;

import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.auth.api.model.GetAccountInfoUserList;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import demo.lgmg.firebasechat.firebase_chat_demo.R;

/**
 * Created by lgmguadama on 11/29/2016.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener, ValueEventListener {

    private static final String tag = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseUser user;
    private String userPass;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    // [END declare_auth_listener]

    private GoogleApiClient mGoogleApiClient;

    private TextInputEditText txtusername, txtpassword;
    private TextView mStatusTextView,tvusername;
    private TextView mDetailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtusername = (TextInputEditText) findViewById(R.id.login_txtusername);
        txtpassword = (TextInputEditText) findViewById(R.id.login_txtpassword);
        tvusername= (TextView) findViewById(R.id.login_tvusername);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(LoginActivity.this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.i(tag, "onAuthStateChanged:signed_in: " + user.getUid());
                } else {
                    Log.i(tag, "onAuthStateChanged:signed_out");
                }
                updateUI(user);
            }
        };

        ((Button) (findViewById(R.id.login_btnsignin))).setOnClickListener(this);
        ((Button) (findViewById(R.id.login_btnsignout))).setOnClickListener(this);
        ((Button) (findViewById(R.id.login_btndisconnect))).setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                //sesion exitosa, ahora autenticar con firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                updateUI(null);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.i(tag, "firebaseAuthWithGoogle: " + acct.getId());
//        showProgressDialog();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.i(tag, "signInWIthCredential: onCOmplete: " + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.w(tag, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Autentificacion fallida!", Toast.LENGTH_LONG).show();
                        }
//                        hideProgressDialog();

                    }
                });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        mAuth.signOut(); //firebase signOut

        //google revoke access
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                updateUI(null);
            }
        });
    }

    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();

        // Google revoke access
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
//        hideProgressDialog();
        this.user=user;
        if (user != null) {
//          mStatusTextView.setText(getString(R.string.google_status_fmt, user.getEmail()));
//            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));
            Log.i(tag, user.getDisplayName());
            Log.i(tag, user.getEmail());
            Log.i(tag, user.getUid());

            tvusername.setText(user.getDisplayName());

            findViewById(R.id.login_btnsignin).setVisibility(View.GONE);
            findViewById(R.id.login_btnsignout).setVisibility(View.VISIBLE);
            findViewById(R.id.login_btndisconnect).setVisibility(View.VISIBLE);
        } else {
//            mStatusTextView.setText(R.string.signed_out);
//            mDetailTextView.setText(null);
            tvusername.setText("Username");

            findViewById(R.id.login_btnsignin).setVisibility(View.VISIBLE);
            findViewById(R.id.login_btnsignout).setVisibility(View.GONE);
            findViewById(R.id.login_btndisconnect).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.login_btnsignin) {
            signIn();
        } else if (i == R.id.login_btnsignout) {
            signOut();
        } else if (i == R.id.login_btndisconnect) {
            revokeAccess();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
// An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(tag, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    private void registerUser(){
        Task<AuthResult> task = FirebaseAuth.getInstance().createUserWithEmailAndPassword(user.getEmail(), userPass);

        user = task.getResult().getUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(user.getDisplayName())
                .build();
        user.updateProfile(profileUpdates);

    }

    private void registerUserInDatabase(){
        FirebaseDatabase database= FirebaseDatabase.getInstance();
        database.getReference().child("users").addListenerForSingleValueEvent(this);
    }


    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        ArrayList uList=new ArrayList();
        for (DataSnapshot ds: dataSnapshot.getChildren()){
            Log.i(tag  ,ds.getKey());
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
