package com.example.kioskapp

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class HelpDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_help, null)
        return AlertDialog.Builder(requireContext())
            .setTitle("📘 사용 방법")
            .setView(view)
            .setPositiveButton("확인", null)
            .create()
    }
}
