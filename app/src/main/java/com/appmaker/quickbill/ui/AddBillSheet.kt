package com.appmaker.quickbill.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import coil.load
import com.appmaker.quickbill.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.util.UUID

class AddBillSheet : BottomSheetDialogFragment() {

    companion object {
        const val TAG_SHEET = "AddBillSheet"
        const val RESULT_KEY = "add_bill_result"
        const val RESULT_ID = "result_id"
        const val RESULT_STORE = "result_store"
        const val RESULT_AMOUNT = "result_amount"
        const val RESULT_CATEGORY = "result_category"
        const val RESULT_IMAGE_PATH = "result_image_path"
        const val RESULT_CREATED_AT = "result_created_at"

        fun newInstance(): AddBillSheet = AddBillSheet()
    }

    private var root: View? = null

    private lateinit var imagePreview: ImageView
    private lateinit var imagePlaceholder: View
    private lateinit var storeLayout: TextInputLayout
    private lateinit var storeInput: TextInputEditText
    private lateinit var amountLayout: TextInputLayout
    private lateinit var amountInput: TextInputEditText
    private lateinit var categoryChips: ChipGroup
    private lateinit var saveButton: MaterialButton

    private var currentImagePath: String? = null
    private var pendingCaptureUri: Uri? = null

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(context, "إذن الكاميرا مطلوب لالتقاط الفاتورة", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCaptureUri != null) {
            val path = pendingCaptureUri.toString()
            currentImagePath = path
            showImage(path)
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val path = it.toString()
            currentImagePath = path
            showImage(path)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.add_bill_sheet, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imagePreview = view.findViewById(R.id.imagePreview)
        imagePlaceholder = view.findViewById(R.id.imagePlaceholder)
        storeLayout = view.findViewById(R.id.storeLayout)
        storeInput = view.findViewById(R.id.storeInput)
        amountLayout = view.findViewById(R.id.amountLayout)
        amountInput = view.findViewById(R.id.amountInput)
        categoryChips = view.findViewById(R.id.categoryChips)
        saveButton = view.findViewById(R.id.saveButton)

        val cameraButton = view.findViewById<MaterialButton>(R.id.cameraButton)
        val galleryButton = view.findViewById<MaterialButton>(R.id.galleryButton)

        cameraButton?.setOnClickListener {
            val ctx = requireContext()
            if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        galleryButton?.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        saveButton.setOnClickListener {
            val store = storeInput.text?.toString()?.trim() ?: ""
            val amountStr = amountInput.text?.toString()?.trim() ?: ""

            if (store.isEmpty()) {
                storeLayout.error = "يرجى إدخال اسم المتجر"
                return@setOnClickListener
            }
            storeLayout.error = null

            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0.0) {
                amountLayout.error = "يرجى إدخال مبلغ صحيح"
                return@setOnClickListener
            }
            amountLayout.error = null

            val checkedChipId = categoryChips.checkedChipId
            val category = when (checkedChipId) {
                R.id.chipFood -> "FOOD"
                R.id.chipShopping -> "SHOPPING"
                R.id.chipHome -> "HOME"
                else -> "OTHER"
            }

            val bundle = bundleOf(
                RESULT_ID to UUID.randomUUID().toString(),
                RESULT_STORE to store,
                RESULT_AMOUNT to amount,
                RESULT_CATEGORY to category,
                RESULT_IMAGE_PATH to (currentImagePath ?: ""),
                RESULT_CREATED_AT to System.currentTimeMillis()
            )

            parentFragmentManager.setFragmentResult(RESULT_KEY, bundle)
            dismiss()
        }
    }

    private fun showImage(path: String) {
        imagePlaceholder.visibility = View.GONE
        imagePreview.visibility = View.VISIBLE
        imagePreview.load(Uri.parse(path)) {
            crossfade(true)
        }
    }

    private fun launchCamera() {
        try {
            val ctx = requireContext()
            val file = File(ctx.cacheDir, "bill_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
            pendingCaptureUri = uri
            takePictureLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "تعذر فتح الكاميرا: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        root = null
    }
}
