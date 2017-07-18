package xyz.ngoni.nm.firebasephonenumber;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mNum, mReceivedCode;
    private Button mSend, mSignIn;
    private String mVerificationId;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mNum = (EditText) findViewById(R.id.number_et);
        mReceivedCode = (EditText) findViewById(R.id.received_code_ed);
        mSend = (Button) findViewById(R.id.send_button);
        mSignIn = (Button) findViewById(R.id.sign_in_button);

        mSignIn.setOnClickListener(this);
        mSend.setOnClickListener(this);


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {

                    Toast.makeText(MainActivity.this, "you are now logged in", Toast.LENGTH_SHORT).show();

                }

            }
        };


    }

    public void requestCode(View view) throws FirebaseTooManyRequestsException {
        String phoneNumber = mNum.getText().toString();
        if (phoneNumber.isEmpty()) {
            mNum.setError("number not entered");
            return;
        }

        if(!phoneNumber.startsWith("+")){
            mNum.setError("number must begin with +");
            return;
        }

        if(phoneNumber.length() > 13 || phoneNumber.length() < 13){
            mNum.setError("number format not correct");
            return;
        }



        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber, 60, TimeUnit.SECONDS, MainActivity.this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {


                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                        signInWithCredential(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        mNum.setError("verification failed" + e.getMessage());



                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verificationId, forceResendingToken);
                        mVerificationId = verificationId;
                    }

                    @Override
                    public void onCodeAutoRetrievalTimeOut(String verificationId) {
                        super.onCodeAutoRetrievalTimeOut(verificationId);
                        Toast.makeText(MainActivity.this,
                                "verification failed" + verificationId,
                                Toast.LENGTH_LONG).show();
                    }

                }
        );


    }

    private void signInWithCredential(PhoneAuthCredential phoneAuthCredential) {

        mAuth.signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful())
                                    Toast.makeText(MainActivity.this, "signed in successfully", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(MainActivity.this, "failed to sign in with credential"
                                                    + task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                            }
                        });
    }


    @Override
    public void onClick(View v) {
        int position = v.getId();
        switch (position) {
            case R.id.send_button:
                try {
                    requestCode(v);
                } catch (FirebaseTooManyRequestsException e) {
                    e.getMessage();
                }
                break;

            case R.id.sign_in_button:
                String code = mReceivedCode.getText().toString();
                if (TextUtils.isEmpty(code))
                    return;
                signInWithCredential(PhoneAuthProvider.getCredential(mVerificationId, code));
                break;
        }

    }
}
