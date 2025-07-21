package com.example.numeriser

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class UploadFile : AppCompatActivity() {
    private var imageView: ImageView? = null // pour afficher l'image sélectionnée

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_upload_file)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            v.setPadding(
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            )
            insets
        }

        imageView = findViewById(R.id.imageView2) // Assure-toi que imageView2 existe dans le layout

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            if (item.itemId == R.id.carnet) {
                if (checkAndRequestPermissions()) {
                    showImageOptionDialog()
                }
                return@setOnItemSelectedListener true
            }
            false
        }
    }

    private fun showImageOptionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choisir une option")
            .setItems(
                arrayOf<CharSequence>("Prendre une photo", "Choisir depuis la galerie")
            ) { dialog: DialogInterface?, which: Int ->
                if (which == 0) {
                    openCamera()
                } else {
                    openGallery()
                }
            }
        builder.create().show()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.setType("image/*")
        startActivityForResult(
            Intent.createChooser(intent, "Sélectionner une image"),
            REQUEST_GALLERY
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_CAMERA) {
                val photo = data.extras!!["data"] as Bitmap?
                imageView!!.setImageBitmap(photo) // Affiche la photo capturée
                Toast.makeText(this, "Photo capturée", Toast.LENGTH_SHORT).show()
            } else if (requestCode == REQUEST_GALLERY) {
                val selectedImageUri = data.data
                imageView!!.setImageURI(selectedImageUri) // Affiche l'image choisie
                Toast.makeText(this, "Image choisie depuis la galerie", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val readPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

        if (!cameraPermission || !readPermission) {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            } else {
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }

            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
            return false
        }

        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            var allGranted = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false
                    break
                }
            }

            if (allGranted) {
                showImageOptionDialog()
            } else {
                Toast.makeText(this, "Les permissions sont nécessaires", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val REQUEST_CAMERA = 100
        private const val REQUEST_GALLERY = 200
        private const val PERMISSION_REQUEST_CODE = 300
    }
}