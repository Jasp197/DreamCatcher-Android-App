package edu.vt.cs5254.dreamcatcher

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.doOnLayout
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import edu.vt.cs5254.dreamcatcher.databinding.FragmentPhotoDialogBinding
import java.io.File

class PhotoDialogFragment : DialogFragment() {

    private val args: PhotoDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val binding = FragmentPhotoDialogBinding.inflate(layoutInflater)

        if (binding.photoDetail.tag != args.dreamPhotoFilename) {
            val photoFile = args.dreamPhotoFilename?.let {
                File(requireContext().applicationContext.filesDir, it)
            }

            if (photoFile?.exists() == true) {
                binding.root.doOnLayout { measuredView ->
                    val scaledBitmap = getScaledBitmap(
                        photoFile.path,
                        measuredView.width,
                        measuredView.height
                    )
                    binding.photoDetail.setImageBitmap(scaledBitmap)
                    binding.photoDetail.tag = args.dreamPhotoFilename
                }
            } else {
                binding.photoDetail.setImageBitmap(null)
                binding.photoDetail.tag = null
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .show()
    }

}