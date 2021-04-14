package com.example.otherextraapp

import android.Manifest
import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.Manifest.permission_group.CAMERA
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    val PHONE_NO = "50177985"
    val TAG = "xyz"
    private val PERMISSION_REQUEST_CODE = 1
    val CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_BY_FILE = 101
    val CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_BY_BITMAP = 102

    var mFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions()
    }

    fun onEmail(view: View) {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type="plain/text"
        val receivers = arrayOf("nikolaivasilev208@gmail.com")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, receivers)
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Hey Hey Hey")
        emailIntent.putExtra(Intent.EXTRA_TEXT,
        "Hey, this email is send to you by the app I just created ;) :) :D")
        startActivity(emailIntent)

    }
    fun onBrowser(view: View) {
        val url="http://www.easv.dk"
        val i = Intent(Intent.ACTION_VIEW)
        i.data =Uri.parse(url)
        startActivity(i)
    }

    fun onCall(view: View) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$PHONE_NO")
        startActivity(intent)
    }

    fun onSMS(view: View) {
        showYesNoDialog()
    }

    private fun startSMSActivity(){
        val sendIntent = Intent(Intent.ACTION_VIEW)
        sendIntent.data=Uri.parse("sms:$PHONE_NO")
        sendIntent.putExtra("sms_body", "This is magic! <3")
        startActivity(sendIntent)
    }


    private fun sendSMSDirectly(){
        Toast.makeText(this,"SMS will be send", Toast.LENGTH_SHORT).show()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED) {
                Log.d(TAG, "permission denied to SEND_SMS - requesting it")
                val permissions = arrayOf(android.Manifest.permission.SEND_SMS)
                requestPermissions(permissions,PERMISSION_REQUEST_CODE)
                return
            } else Log.d(TAG, "Permission to send sms granted!")
        } else Log.d(TAG, "Runtime permission not needed")
        sendMessage()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        Log.d(TAG, "Permission:" + permissions[0] + "-grantResult:"+grantResults[0])
        if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
            sendMessage()
        }
    }
    private fun sendMessage(){
        val m = SmsManager.getDefault()
        val text = "This is magic!! <3"
        m.sendTextMessage(PHONE_NO, null,text, null, null)
    }

    private fun showYesNoDialog(){
        val alertDialogBuilder  = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("SMS Handling")
        alertDialogBuilder
            .setMessage("Click Direct if SMS should be send directly. Click start to start SMS app.")
            .setCancelable(true)
            .setPositiveButton("Direct"){dialog, id -> sendSMSDirectly() }
            .setNegativeButton("Start", {dialog, id ->startSMSActivity()})
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


    fun onTakeByFile(view: View) {
        mFile = getOutputMediaFile("Camera01") // create a file to save the image

        if (mFile == null) {
            Toast.makeText(this, "Could not create file...", Toast.LENGTH_LONG).show()
            return
        }

        // create Intent to take a picture

        // create Intent to take a picture
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val OtherExtraApp = "com.example.otherextraapp"
        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(
                this,
                "${OtherExtraApp}.provider",  //use your app signature + ".provider"
                mFile!!))

        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_BY_FILE)
        } else Log.d(TAG, "camera app could NOT be started")

    }

    fun onTakeByBitmap(view: View) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)


        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_BY_BITMAP)
        } else Log.d(TAG, "camera app could NOT be started")

    }


    //Checks if the app has the required permissions, and prompts the user with the ones missing.
    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val permissions = mutableListOf<String>()
        if ( ! isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE) ) permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if ( ! isGranted(Manifest.permission.CAMERA) ) permissions.add(Manifest.permission.CAMERA)
        if (permissions.size > 0)
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_CODE)
    }


    private fun isGranted(permission: String): Boolean =
            ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED


    // return a new file with a timestamp name in a folder named [folder] in
    // the external directory for pictures.
    // Return null if the file cannot be created
    private fun getOutputMediaFile(folder: String): File? {
        // in an emulated device you can see the external files in /sdcard/Android/data/<your app>.
        val mediaStorageDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), folder)
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory")
                return null
            }
        }

        // Create a media file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val postfix = "jpg"
        val prefix = "IMG"
        return File(mediaStorageDir.path +
                File.separator + prefix +
                "_" + timeStamp + "." + postfix)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val mImage = findViewById<ImageView>(R.id.imgView)
        val tvImageInfo = findViewById<TextView>(R.id.tvImageInfo)
        when (requestCode) {

            CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_BY_FILE ->
                if (resultCode == RESULT_OK)
                    showImageFromFile(mImage, tvImageInfo, mFile!!)
                else handleOther(resultCode)

            CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE_BY_BITMAP ->
                if (resultCode == RESULT_OK) {
                    val extras = data!!.extras
                    val imageBitmap = extras!!["data"] as Bitmap
                    showImageFromBitmap(mImage, tvImageInfo, imageBitmap)
                } else handleOther(resultCode)
        }
    }

    private fun handleOther(resultCode: Int) {
        if (resultCode == RESULT_CANCELED)
            Toast.makeText(this, "Canceled...", Toast.LENGTH_LONG).show()
        else Toast.makeText(this, "Picture NOT taken - unknown error...", Toast.LENGTH_LONG).show()
    }


    // show the image allocated in [f] in imageview [img]. Show meta data in [txt]
    private fun showImageFromFile(img: ImageView, txt: TextView, f: File) {
        img.setImageURI(Uri.fromFile(f))
        img.setBackgroundColor(Color.BLACK)
        //mImage.setRotation(90);
        txt.text = "File at:" + f.absolutePath + " - size = " + f.length()

    }

    // show the image [bmap] in the imageview [img] - and put meta data in [txt]
    private fun showImageFromBitmap(img: ImageView, txt: TextView, bmap: Bitmap) {
        img.setImageBitmap(bmap)
        img.setLayoutParams(RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        img.setBackgroundColor(Color.BLACK)
        txt.text = "bitmap - size = " + bmap.byteCount

    }
}