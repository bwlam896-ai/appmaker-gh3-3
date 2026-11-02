package com.appmaker.quickbill.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import coil.load
import com.appmaker.quickbill.R
import com.appmaker.quickbill.data.ImageStore
import com.appmaker.quickbill.model.BillCategory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

// @Permissions: android.permission.CAMERA
class AddBillSheet : BottomSheetDialogFragment() {

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
    private var pendingCapture