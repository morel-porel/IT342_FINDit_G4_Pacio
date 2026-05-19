package com.example.mobile.feature.item

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mobile.R
import com.example.mobile.shared.network.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

/**
 * MOB-05 — Report Item
 *
 * Found / Lost toggle, all fields, camera/gallery image picker.
 * Photo is required for FOUND, optional for LOST.
 */
class ReportItemActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnFound: Button
    private lateinit var btnLost: Button
    private lateinit var etItemName: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etDescription: EditText
    private lateinit var etDate: EditText
    private lateinit var etLocation: EditText
    private lateinit var tvPhotoLabel: TextView
    private lateinit var tvPhotoHint: TextView
    private lateinit var btnPickPhoto: Button
    private lateinit var ivPhotoPreview: ImageView
    private lateinit var tvPhotoName: TextView
    private lateinit var btnSubmit: Button

    private var selectedType = "FOUND"
    private var selectedPhotoUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedPhotoUri = it
            ivPhotoPreview.setImageURI(it)
            ivPhotoPreview.visibility = View.VISIBLE
            val name = getFileName(it) ?: "photo.jpg"
            tvPhotoName.text = name
            tvPhotoName.visibility = View.VISIBLE
        }
    }

    private val categories = listOf(
        "Electronics", "Clothing", "ID/Cards", "Keys",
        "Bag", "Books/Documents", "Accessories", "Other"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_item)

        btnBack = findViewById(R.id.ivBack)
        btnFound = findViewById(R.id.btnTypeFound)
        btnLost = findViewById(R.id.btnTypeLost)
        etItemName = findViewById(R.id.etItemName)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        etDescription = findViewById(R.id.etDescription)
        etDate = findViewById(R.id.etDate)
        etLocation = findViewById(R.id.etLocation)
        tvPhotoLabel = findViewById(R.id.tvPhotoLabel)
        tvPhotoHint = findViewById(R.id.tvPhotoHint)
        btnPickPhoto = findViewById(R.id.btnPickPhoto)
        ivPhotoPreview = findViewById(R.id.ivPhotoPreview)
        tvPhotoName = findViewById(R.id.tvPhotoName)
        btnSubmit = findViewById(R.id.btnSubmit)

        btnBack.setOnClickListener { finish() }

        // Category spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        // Type toggle
        setType("FOUND")
        btnFound.setOnClickListener { setType("FOUND") }
        btnLost.setOnClickListener { setType("LOST") }

        // Date picker
        etDate.isFocusable = false
        etDate.setOnClickListener { showDatePicker() }

        // Photo picker
        btnPickPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnSubmit.setOnClickListener { submitReport() }
    }

    private fun setType(type: String) {
        selectedType = type
        if (type == "FOUND") {
            btnFound.backgroundTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#1D4ED8"))
            btnFound.setTextColor(android.graphics.Color.WHITE)
            btnLost.backgroundTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#E2E8F0"))
            btnLost.setTextColor(android.graphics.Color.parseColor("#334155"))
            tvPhotoLabel.text = "Photo (Required)"
            tvPhotoHint.text = "JPG/PNG, max 5MB — required for found items"
        } else {
            btnLost.backgroundTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#1D4ED8"))
            btnLost.setTextColor(android.graphics.Color.WHITE)
            btnFound.backgroundTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#E2E8F0"))
            btnFound.setTextColor(android.graphics.Color.parseColor("#334155"))
            tvPhotoLabel.text = "Photo (Optional)"
            tvPhotoHint.text = "JPG/PNG, max 5MB"
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                etDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
            },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun submitReport() {
        val name = etItemName.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()
        val description = etDescription.text.toString().trim()
        val date = etDate.text.toString().trim()
        val location = etLocation.text.toString().trim()

        if (name.isEmpty()) { etItemName.error = "Item name is required"; return }
        if (date.isEmpty()) { etDate.error = "Date is required"; return }
        if (location.isEmpty()) { etLocation.error = "Location is required"; return }
        if (selectedType == "FOUND" && selectedPhotoUri == null) {
            Toast.makeText(this, "Photo is required for found items", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("findit_prefs", MODE_PRIVATE)
        val token = prefs.getString("token", null)
        if (token == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show()
            return
        }

        btnSubmit.isEnabled = false
        btnSubmit.text = "Submitting..."

        lifecycleScope.launch {
            try {
                val typePart = selectedType.toRequestBody("text/plain".toMediaTypeOrNull())
                val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
                val categoryPart = category.toRequestBody("text/plain".toMediaTypeOrNull())
                val descPart = if (description.isNotEmpty())
                    description.toRequestBody("text/plain".toMediaTypeOrNull()) else null
                val datePart = date.toRequestBody("text/plain".toMediaTypeOrNull())
                val locationPart = location.toRequestBody("text/plain".toMediaTypeOrNull())

                val photoPart: MultipartBody.Part? = selectedPhotoUri?.let { uri ->
                    val file = uriToFile(uri)
                    val reqBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("photo", file.name, reqBody)
                }

                val response = RetrofitClient.instance.reportItem(
                    token = "Bearer $token",
                    type = typePart,
                    name = namePart,
                    category = categoryPart,
                    description = descPart,
                    dateLostFound = datePart,
                    location = locationPart,
                    photo = photoPart
                )

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@ReportItemActivity,
                        "Report submitted successfully!",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    val code = response.code()
                    val msg = when (code) {
                        400 -> "Please check your inputs"
                        401 -> "Session expired. Please log in again."
                        413 -> "FILE-001: File size must not exceed 5MB"
                        415 -> "FILE-002: Only JPG and PNG files are accepted"
                        else -> "Failed to submit report (HTTP $code)"
                    }
                    Toast.makeText(this@ReportItemActivity, msg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ReportItemActivity, "Connection error. Try again.", Toast.LENGTH_SHORT).show()
            } finally {
                btnSubmit.isEnabled = true
                btnSubmit.text = "Submit Report"
            }
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)!!
        val fileName = getFileName(uri) ?: "upload_${System.currentTimeMillis()}.jpg"
        val tempFile = File(cacheDir, fileName)
        val outputStream = FileOutputStream(tempFile)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        return tempFile
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = it.getString(idx)
            }
        }
        return name
    }
}
