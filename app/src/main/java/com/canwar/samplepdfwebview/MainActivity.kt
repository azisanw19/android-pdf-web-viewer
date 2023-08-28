package com.canwar.samplepdfwebview

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.canwar.pdfwebview.PdfWebViewActivity
import com.canwar.samplepdfwebview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult? ->
        if (result == null) return@registerForActivityResult
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        val resultData = result.data
        if (resultData != null) {
            showPDF(result.data!!.data!!)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.openDocument.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "application/pdf"
            openDocumentLauncher.launch(intent)
        }

    }

    private fun showPDF(uri: Uri) {
        val intent = Intent(this, PdfWebViewActivity::class.java)
        intent.putExtra(PdfWebViewActivity.EXTRA_STRING_URI, uri.toString())
        startActivity(intent)
    }
}