package com.androiddeveloper3005.git.bloodbank.ui

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.androiddeveloper3005.git.bloodbank.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.*
import java.util.concurrent.TimeUnit

private const val TAG = "VerifyOTPCodeActivity"
class VerifyOTPCodeActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var verificationID : String
    private lateinit var codeET : EditText
    private lateinit var signinBTN : Button
    private lateinit var currentUserID : String
    private lateinit var progresBar: ProgressBar
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var databaseRef : DatabaseReference
    private lateinit var  dialog : ProgressDialog
    private lateinit var  phone : String
    private lateinit var toolbar: Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_otpcode)
        phone = intent.getStringExtra("phone").toString()
        initView()
        intiFirebase()
        sendVerificationCode(phone)
        signinBTN.setOnClickListener(this)

    }



    private fun sendVerificationCode(number: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            number,
            60,
            TimeUnit.SECONDS, this, mCallBack
        )
    }
    private var mCallBack = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

        override fun onCodeSent(s: String, p1: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(s, p1)
            verificationID = s
        }

        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
            var code : String? = phoneAuthCredential.smsCode
            if (code != null){
                progresBar.setVisibility(View.VISIBLE)
                verifyCode(code)
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(getApplicationContext(), e.message, Toast.LENGTH_LONG).show();
            Log.d(TAG," : $e.message")
        }

    }

    private fun verifyCode(code: String) {
        var  phoneAuthCredential : PhoneAuthCredential = PhoneAuthProvider.getCredential(verificationID, code)
        signInWithCredential(phoneAuthCredential)


    }

    private fun signInWithCredential(phoneAuthCredential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful){
                dialog.show()
                currentUserID =firebaseAuth.currentUser ?.uid.toString()

                databaseRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.hasChild(currentUserID)) {
                            dialog.dismiss()
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        } else {
                            dialog.dismiss()
                            val intent = Intent(applicationContext, UserActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
            }else{
                Toast.makeText(
                    getApplicationContext(),
                    task.getException()?.message,
                    Toast.LENGTH_LONG
                ).show();
            }
        }

    }


    private fun intiFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference.child("Users")
    }

    private fun initView() {
        codeET = findViewById(R.id.edittext_code)
        signinBTN = findViewById(R.id.sign_in_btn)
        progresBar = findViewById(R.id.progressBar)
        toolbar = findViewById(R.id.toolBar)
        setSupportActionBar(toolbar)
        dialog = ProgressDialog(this)
        dialog.setMessage("Checking Account Information...")
    }

    override fun onClick(v: View?) {
        val code : String = codeET.getText().toString().trim()
        if (code.isEmpty() || code.length < 6) {
            codeET.setError("Enter Code...")
            codeET.requestFocus()
            return
        }
        progresBar.setVisibility(View.VISIBLE)
        verifyCode(code)
    }
}