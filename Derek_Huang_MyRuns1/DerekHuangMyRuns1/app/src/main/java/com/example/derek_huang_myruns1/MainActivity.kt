package com.example.derek_huang_myruns1

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import com.example.derek_huang_myruns1.Util
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    // Declare variables
    private lateinit var imageProfile: ImageView
    private lateinit var photoButton: Button
    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editTextPhone: EditText
    private lateinit var radioGroupGender: RadioGroup
    private lateinit var editClass: EditText
    private lateinit var editMajor: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var tempImgUri: Uri
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var newImgUri: Uri

    private var saveCheck: Int = 0


    private val fileName = "profile_pic.jpg"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize
        imageProfile = findViewById(R.id.imageProfile)
        photoButton = findViewById(R.id.photoButton)
        editName = findViewById(R.id.editName)
        editEmail = findViewById(R.id.editEmail)
        editTextPhone = findViewById(R.id.editTextPhone)
        radioGroupGender = findViewById(R.id.radioGroupGender)
        editClass = findViewById(R.id.editClass)
        editMajor = findViewById(R.id.editMajor)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
        sharedPreferences = getSharedPreferences("appPrefs", Context.MODE_PRIVATE)

        Util.checkPermissions(this)

        //Initialize file provider
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (storageDirectory != null) {
            if (!storageDirectory.exists()) {
                storageDirectory.mkdirs()
            }
        }
        val profilePicFile = File(storageDirectory, fileName)
        val newImageFile = File(storageDirectory, "new_pic.jpg")
        tempImgUri = FileProvider.getUriForFile(this, "com.example.derek_huang_myruns1.fileprovider", profilePicFile)
        newImgUri = FileProvider.getUriForFile(this, "com.example.derek_huang_myruns1.fileprovider", newImageFile)

        // Initialize saveCheck from SharedPreferences
        saveCheck = sharedPreferences.getInt("saveCheck", 0)
        if (saveCheck == 1){
            loadSavedData()
        }

        //Initialize cameraResult launcher (FROM LECTURE)
        cameraResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val bitmap = Util.getBitmap(this, newImgUri)
                    imageProfile.setImageBitmap(bitmap)
                }
            }

        //Set click listener for photoButton (FROM LECTURE)
        photoButton.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, newImgUri)
            cameraResult.launch(cameraIntent)
        }

        //Set click listener for saveButton
        saveButton.setOnClickListener {
            copyFile(newImgUri, profilePicFile)
            saveData()
            finish()
        }

        //Set click listener for cancelButton
        cancelButton.setOnClickListener {
            finish()
        }

    }

    private fun saveData() {
        saveCheck = 1
        val editor = sharedPreferences.edit()

        editor.putString("name", editName.text.toString())
        editor.putString("email", editEmail.text.toString())
        editor.putString("phone", editTextPhone.text.toString())
        editor.putString("class", editClass.text.toString())
        editor.putString("major", editMajor.text.toString())
        editor.putInt("saveCheck", saveCheck)

        // Save radio button selection
        val selectedGender = getSelectedGender(radioGroupGender)
        editor.putString("selectedGender", selectedGender)
        editor.apply()
    }

    private fun loadSavedData() {
        val name = sharedPreferences.getString("name", "")
        val email = sharedPreferences.getString("email", "")
        val phone = sharedPreferences.getString("phone", "")
        val gClass = sharedPreferences.getString("class", "")
        val major = sharedPreferences.getString("major", "")
        val selectedGender = sharedPreferences.getString("selectedGender", "")

        // Set the loaded data to the EditText fields
        editName.setText(name)
        editEmail.setText(email)
        editTextPhone.setText(phone)
        editClass.setText(gClass)
        editMajor.setText(major)
        setRadioGroupSelection(radioGroupGender, selectedGender)

        val tempImgFile = File(tempImgUri.path)
        if (tempImgFile.exists()){
            val bitmap = Util.getBitmap(this, tempImgUri)
            imageProfile.setImageBitmap(bitmap)
        }
    }

    //Copy function taken from ChatGPT
    private fun copyFile(sourceUri: Uri, destinationFile: File) {
        val inputStream = contentResolver.openInputStream(sourceUri)
        val outputStream = FileOutputStream(destinationFile)

        inputStream?.use { input ->
            outputStream.use { output ->
                val buffer = ByteArray(4 * 1024) // 4KB buffer size
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
            }
        }
    }


    private fun getSelectedGender(radioGroup: RadioGroup): String {
        val selectedRadioButtonId = radioGroup.checkedRadioButtonId
        return when (selectedRadioButtonId) {
            R.id.radioButtonMale -> "Male"
            R.id.radioButtonFemale -> "Female"
            else -> ""
        }
    }
    private fun setRadioGroupSelection(radioGroup: RadioGroup, selectedGender: String?) {
        when (selectedGender) {
            "Male" -> radioGroup.check(R.id.radioButtonMale)
            "Female" -> radioGroup.check(R.id.radioButtonFemale)
            else -> radioGroup.clearCheck()
        }
    }
}