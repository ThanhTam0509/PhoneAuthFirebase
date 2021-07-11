package com.thanhtam.phoneauthfirebase

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.thanhtam.phoneauthfirebase.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

//    view binding
    private lateinit var binding: ActivityMainBinding

    //    if code sending failed, will used to resend
    private var forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null

    private var mCallBack: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null
    private var mVerificationId: String? = null
    private lateinit var firebaseAuth: FirebaseAuth

    private val TAG = "MAIN_TAG"

//    progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lnPhone.visibility = View.VISIBLE
        binding.lnCode.visibility = View.GONE

//      init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        mCallBack = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
//                Hàm này sẽ có 2 trường hợp:
//                1 - Instant verification. In some cases the phone number can be instantly
//                verified without needing to send or enter a verfication code
//                2- Auto-reteval. On some device Google Play service can automatically
//                detect the incoming verification SMS and perform verification without user action
                Log.d(TAG, "onVerificationCompleted: ")
                signInWithPhoneAuthCredential(phoneAuthCredential)

            }

            override fun onVerificationFailed(e: FirebaseException) {
//                This callback is invoked in a invalid request for verification is nade
//                for instance if the phone number format is not valid
                progressDialog.dismiss()
                Log.d(TAG, "onVerificationFailed: ${e.message}")
                Toast.makeText(this@MainActivity, "${e.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
//                The SMS verification code has been sent to the provided phone number
//                we now need to ask the user to enter the code and then construct a credential
//                by conbining the code with a verification ID
                Log.d(TAG, "onCodeSent: $verificationId")
                mVerificationId = verificationId
                forceResendingToken = token
                progressDialog.dismiss()

                Log.d(TAG, "onCodeSent: $verificationId")

                //hide phone layout, show code layout
                binding.lnPhone.visibility = View.GONE
                binding.lnCode.visibility = View.VISIBLE
                Toast.makeText(this@MainActivity, "Verification code sent...", Toast.LENGTH_SHORT).show()
                binding.txtResendCode.text = "Please type the verification code we sent to ${binding.edtPhone.text.toString().trim()}"


            }
        }
//        phoneContinue btn Click: input phone number, validate, start phone authentication/login
        binding.btnPhone.setOnClickListener {
//            input phone number
            val phone = binding.edtPhone.text.toString().trim()
//            validate phone number
            if (TextUtils.isEmpty(phone)){
                Toast.makeText(this@MainActivity, "Please enter phone number", Toast.LENGTH_SHORT).show()
            } else {
                startPhoneNumberVerification(phone)
            }

        }
//        resendCode txt click (if code didn't recive) resend verification code/OTP
        binding.txtResendCode.setOnClickListener{
            val phone = binding.edtPhone.text.toString().trim()
//            validate phone number
            if (TextUtils.isEmpty(phone)){
                Toast.makeText(this@MainActivity, "Please enter phone number", Toast.LENGTH_SHORT).show()
            } else {
                resendVerificationCode(phone, forceResendingToken)
            }
        }
//        codeSubmit btn click: input verification code, validate, verify phone number with verification code
        binding.btnCodeSubmit.setOnClickListener {
//            input verification code
            val code = binding.edtCode.text.toString().trim()
            if (TextUtils.isEmpty(code)){
                Toast.makeText(this@MainActivity, "Please enter verification code", Toast.LENGTH_SHORT).show()
            } else {
                verifyPhoneNumberWithCode(mVerificationId, code)
            }
        }
    }
    private fun startPhoneNumberVerification(phone: String) {
        Log.d(TAG, "startPhoneNumberVerification: $phone")
        progressDialog.setMessage("Verifying Phone Number...")
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallBack!!)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    private fun resendVerificationCode(phone: String, token: PhoneAuthProvider.ForceResendingToken?) {
        progressDialog.setMessage("Resending Code...")
        progressDialog.show()

        Log.d(TAG, "resendVerificationCode: $phone")

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallBack!!)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    private fun verifyPhoneNumberWithCode(verificationId: String?, code:String) {
        Log.d(TAG, "verifyphoneNumberWithCode: $verificationId $code")

        progressDialog.setMessage("Verifying Code...")
        progressDialog.show()

        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Log.d(TAG, "signInWithPhoneAuthCredential: ")

        progressDialog.setMessage("Logging in")

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                //login success
                progressDialog.dismiss()
                val phone = firebaseAuth.currentUser?.phoneNumber
                Toast.makeText(this, "Logged In as $phone", Toast.LENGTH_SHORT).show()

//                start porfile activity
                startActivity(Intent(this, BlankFragment::class.java))
                finish()

            }
            .addOnFailureListener {e ->
                progressDialog.dismiss()
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()

            }
    }
}