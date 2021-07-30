package com.androiddeveloper3005.git.bloodbank.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.androiddeveloper3005.git.bloodbank.R
import com.androiddeveloper3005.git.bloodbank.util.CountryData
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "MyActivity"
class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var countrySpinner : Spinner
    private lateinit var phoneET : EditText
    private lateinit var continueBTN : Button
    private lateinit var toolbar: Toolbar
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mAuthListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        //authState listener
        mAuthListener = FirebaseAuth.AuthStateListener {
            if (mAuth.currentUser != null){
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
        //view
        setContentView(R.layout.activity_login)
        //init
        initView()
        //set toolBar
        setSupportActionBar(toolbar)
        countrySpinner.setAdapter(ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                CountryData.countryNames))
        //onclick
        continueBTN.setOnClickListener(this)


    }

    private fun initView() {
        countrySpinner = findViewById(R.id.spinnerCountries)
        phoneET = findViewById(R.id.edittext_phone)
        continueBTN = findViewById(R.id.next_btn)
        toolbar = findViewById(R.id.toolbar)
    }

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthListener)
    }

    override fun onClick(v: View?) {
        var code = CountryData.countryAreaCodes[countrySpinner.selectedItemPosition]
        var number = phoneET.text.toString().trim()
        if (number.isEmpty() || number.length < 10){
            phoneET.setError("Valid number is required")
            phoneET.requestFocus()
            return
        }

        var phoneNumber : String = "+$code$number"
        Toast.makeText(getApplicationContext(), phoneNumber, Toast.LENGTH_LONG).show();
        Log.d(TAG," Phone Number : $phoneNumber")
        var intent : Intent = Intent(this,VerifyOTPCodeActivity::class.java)
        intent.putExtra("phone",phoneNumber)
        startActivity(intent)

    }


}