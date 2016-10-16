package com.example.sabbir.chatting;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.sabbir.chatting.model.User;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginFragment;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GithubAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthCredential;
import com.google.firebase.crash.FirebaseCrash;

import java.util.Arrays;

public class LoginActivity extends CommonActivity implements GoogleApiClient.OnConnectionFailedListener{
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;

    private User user;
    private CallbackManager callbackManager;
    private GoogleApiClient googleApiClient;
    private TwitterAuthCredential twitterAuthCredential;

    private static final int RC_SIGN_IN_GOOGLE = 7859;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
//FACEBOOK
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager=CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                accessFacebookLoginData(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                FirebaseCrash.report(error);
                showSnackbar(error.getMessage());

            }
        });


        //GOOGLE SIGN IN
        GoogleSignInOptions googleSignInOptions=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("312301797686-1bkt0nbecnbctpfoflanjr3sp4fi0aec.apps.googleusercontent.com")
                .requestEmail()
                .build();

        googleApiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,googleSignInOptions)
                .build();

        firebaseAuth=FirebaseAuth.getInstance();
        listener=getFirebaseAuthResultHandler();
        initViews();
        initUser();
    }

   


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == RC_SIGN_IN_GOOGLE ){

            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent( data );
            GoogleSignInAccount account = googleSignInResult.getSignInAccount();

            if( account == null ){
                showSnackbar("Google login falhou, tente novamente");
                return;
            }

            accessGoogleLoginData( account.getIdToken() );
        }
        else{
            //twitterAuthClient.onActivityResult( requestCode, resultCode, data );
            callbackManager.onActivityResult( requestCode, resultCode, data );
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void accessFacebookLoginData(AccessToken accessToken) {
        accessLoginData(
                "facebook",
                (accessToken != null ? accessToken.getToken() : null)
        );
    }

    private void accessGoogleLoginData(String accessToken){
        accessLoginData(
                "google",
                accessToken
        );
    }

    private void accessTwitterLoginData(String token, String secret, String id){
        accessLoginData(
                "twitter",
                token,
                secret
        );
    }

    private void accessGithubLoginData(String accessToken){
        accessLoginData(
                "github",
                accessToken
        );
    }

    public void sendLoginFacebookData( View view ){
        FirebaseCrash.log("LoginActivity:clickListener:button:sendLoginFacebookData()");
        LoginManager
                .getInstance()
                .logInWithReadPermissions(
                        this,
                        Arrays.asList("public_profile", "user_friends", "email")
                );
    }

    private void accessLoginData( String provider, String... tokens ){
        if( tokens != null
                && tokens.length > 0
                && tokens[0] != null ){

            AuthCredential credential = FacebookAuthProvider.getCredential( tokens[0]);
            credential = provider.equalsIgnoreCase("google") ? GoogleAuthProvider.getCredential( tokens[0], null) : credential;
            //credential = provider.equalsIgnoreCase("twitter") ? TwitterAuthProvider.getCredential( tokens[0], tokens[1] ) : credential;
           // credential = provider.equalsIgnoreCase("github") ? GithubAuthProvider.getCredential( tokens[0] ) : credential;

            user.saveProviderSP( LoginActivity.this, provider );
            firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if( !task.isSuccessful() ){
                                showSnackbar("Login social falhou");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            FirebaseCrash.report( e );
                        }
                    });
        }
        else{
            firebaseAuth.signOut();
        }
    }

    private FirebaseAuth.AuthStateListener getFirebaseAuthResultHandler(){
        FirebaseAuth.AuthStateListener callback = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser userFirebase = firebaseAuth.getCurrentUser();

                if( userFirebase == null ){
                    return;
                }

                if( user.getId() == null
                        && isNameOk( user, userFirebase ) ){

                    user.setId( userFirebase.getUid() );
                    user.setNameIfNull( userFirebase.getDisplayName() );
                    user.setEmailIfNull( userFirebase.getEmail() );
                    user.saveDB();
                }

                callMainActivity();
            }
        };
        return( callback );
    }

    private boolean isNameOk( User user, FirebaseUser firebaseUser ){
        return(
                user.getName() != null
                        || firebaseUser.getDisplayName() != null
        );
    }

    private void verifyLogin(){

        FirebaseCrash.log("LoginActivity:verifyLogin()");
        user.saveProviderSP( LoginActivity.this, "" );
        firebaseAuth.signInWithEmailAndPassword(
                user.getEmail(),
                user.getPassword()
        )
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if( !task.isSuccessful() ){
                            showSnackbar("Login falhou");
                            return;
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FirebaseCrash.report( e );
            }
        });
    }

    private void callMainActivity() {
        Intent intent = new Intent( this, MainActivity.class );
        startActivity(intent);
        finish();
    }

    private void verifyLogged(){
        if( firebaseAuth.getCurrentUser() != null ){
            callMainActivity();
        }
        else{
            firebaseAuth.addAuthStateListener( listener );
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        FirebaseCrash
                .report(
                        new Exception(
                                connectionResult.getErrorCode()+": "+connectionResult.getErrorMessage()
                        )
                );
        showSnackbar( connectionResult.getErrorMessage() );
    }


    @Override
    protected void initViews() {
        email = (AutoCompleteTextView) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.login_progress);

    }

    @Override
    protected void initUser() {
        user = new User();
        user.setEmail( email.getText().toString() );
        user.setPassword( password.getText().toString() );

    }

    public void callSignUp(View view){
        Intent intent = new Intent( this, SignUpActivity.class );
        startActivity(intent);
    }

    public void callReset(View view){
        Intent intent = new Intent( this, ResetActivity.class );
        startActivity(intent);
    }

    public void sendLoginData( View view ){
        FirebaseCrash.log("LoginActivity:clickListener:button:sendLoginData()");
        openProgressBar();
        initUser();
        verifyLogin();
    }
}
