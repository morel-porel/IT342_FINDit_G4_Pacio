package com.example.mobile.feature.claim

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.mobile.R
import com.example.mobile.shared.network.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

/**
 * MOB-09 — Claim Submission
 *
 * Read-only item context card (thumbnail, name, location, date),
 * proof description text area, optional proof photo upload,
 * and full-width Submit Claim button.
 */
class ClaimSubmissionActivity : AppCompatActivity() {

    private lateinit var ivBack: ImageView
    private lateinit var ivItemThumbnail: ImageView
    private lateinit var tvItemName: TextView
    private lateinit var tvItemLocation: TextView
    private lateinit var tvItemDate: TextView
    private lateinit var etProofDescription: EditText
    private lateinit var btnPickProofPhoto: Button
    private lateinit var ivProofPreview: ImageView
    private lateinit var tvProofPhotoName: TextView
    private lateinit var btnSubmitClaim: Button

    private var selectedProofUri: Uri? = null
    private var itemId: Long = -1L

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedProofUri = it
            ivProofPreview.setImageURI(it)
            ivProofPreview.visibility = View.VISIBLE
            tvProofPhotoName.text = getFileName(it) ?: "proof.jpg"
            tvProofPhotoName.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_claim_submission)

        ivBack = findViewById(R.id.ivBack)
        ivItemThumbnail = findViewById(R.id.ivItemThumbnail)
        tvItemName = findViewById(R.id.tvItemName)
        tvItemLocation = findViewById(R.id.tvItemLocation)
        tvItemDate = findViewById(R.id.tvItemDate)
        etProofDescription = findViewById(R.id.etProofDescription)
        btnPickProofPhoto = findViewById(R.id.btnPickProofPhoto)
        ivProofPreview = findViewById(R.id.ivProofPreview)
        tvProofPhotoName = findViewById(R.id.tvProofPhotoName)
        btnSubmitClaim = findViewById(R.id.btnSubmitClaim)

        ivBack.setOnClickListener { finish() }

        // Populate item context from intent extras
        itemId = intent.getLongExtra("item_id", -1L)
        val itemName = intent.getStringExtra("item_name") ?: ""
        val itemLocation = intent.getStringExtra("item_location") ?: ""
        val itemDate = intent.getStringExtra("item_date") ?: ""
        val itemImageUrl = intent.getStringExtra("item_image_url") ?: ""

        tvItemName.text = itemName
        tvItemLocation.text = itemLocation
        tvItemDate.text = itemDate

        if (itemImageUrl.isNotBlank()) {
            val url = if (itemImageUrl.startsWith("http")) itemImageUrl
                      else "http://10.0.2.2:8080/$itemImageUrl"
            Glide.with(this).load(url)
                .placeholder(R.drawable.placeholder_image)
                .centerCrop()
                .into(ivItemThumbnail)
        }

        btnPickProofPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnSubmitClaim.setOnClickListener { submitClaim() }
    }

    private fun submitClaim() {
        val proof = etProofDescription.text.toString().trim()
        if (proof.isEmpty()) {
            etProofDescription.error = "Please describe your proof of ownership"
            return
        }
        if (itemId == -1L) {
            Toast.makeText(this, "Invalid item", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("findit_prefs", MODE_PRIVATE)
        val token = prefs.getString("token", null)
        if (token == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show()
            return
        }

        btnSubmitClaim.isEnabled = false
        btnSubmitClaim.text = "Submitting..."

        lifecycleScope.launch {
            try {
                val itemIdPart = itemId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val proofPart = proof.toRequestBody("text/plain".toMediaTypeOrNull())

                val proofImagePart: MultipartBody.Part? = selectedProofUri?.let { uri ->
                    val file = uriToFile(uri)
                    val reqBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("proofImage", file.name, reqBody)
                }

                val response = RetrofitClient.instance.submitClaim(
                    token = "Bearer $token",
                    itemId = itemIdPart,
                    proofDescription = proofPart,
                    proofImage = proofImagePart
                )

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@ClaimSubmissionActivity,
                        "Claim submitted! An admin will review it shortly.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    val msg = when (response.code()) {
                        409 -> "This item already has a pending claim"
                        403 -> "You cannot claim your own item"
                        400 -> "CLAIM-002: Item is not available to claim"
                        401 -> "Session expired. Please log in again."
                        else -> "Failed to submit claim (HTTP ${response.code()})"
                    }
                    Toast.makeText(this@ClaimSubmissionActivity, msg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ClaimSubmissionActivity, "Connection error", Toast.LENGTH_SHORT).show()
            } finally {
                btnSubmitClaim.isEnabled = true
                btnSubmitClaim.text = "Submit Claim"
            }
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)!!
        val fileName = getFileName(uri) ?: "proof_${System.currentTimeMillis()}.jpg"
        val tempFile = File(cacheDir, fileName)
        FileOutputStream(tempFile).use { out -> inputStream.copyTo(out) }
        inputStream.close()
        return tempFile
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        contentResolver.query(uri, null, null, null, null)?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = it.getString(idx)
            }
        }
        return name
    }
}
