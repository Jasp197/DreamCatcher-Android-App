package edu.vt.cs5254.dreamcatcher

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.core.view.doOnLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs5254.dreamcatcher.databinding.FragmentDreamDetailBinding
import kotlinx.coroutines.launch
import java.io.File


class DreamDetailFragment : Fragment() {

    private val args: DreamDetailFragmentArgs by navArgs()

    private val vm: DreamDetailViewModel by viewModels{
        DreamDetailViewModelFactory(args.dreamId)
    }

    private var _binding: FragmentDreamDetailBinding? = null
    private val binding get() = checkNotNull(_binding) { "FragmentDreamDetailBinding is null!!!" }

    private val takePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { didTakePhoto ->
        if (didTakePhoto) {
            binding.dreamPhoto.tag = null
            vm.dream.value?.let { updatePhoto(it) }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDreamDetailBinding.inflate(inflater, container, false)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_dream_detail,menu)
                val takePhotoIntent = takePhoto.contract.createIntent(
                    requireContext(),
                    Uri.EMPTY
                )
                menu.findItem(R.id.take_photo_menu).isVisible = canResolveIntent(takePhotoIntent)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.take_photo_menu -> {
                        vm.dream.value?.let { dream ->
                            val photoFile = File(
                                requireContext().applicationContext.filesDir,
                                dream.photoFileName
                            )
                            val photoUri = FileProvider.getUriForFile(
                                requireContext(),
                                "edu.vt.cs5254.dreamcatcher.fileprovider",
                                photoFile
                            )
                            takePhoto.launch(photoUri)
                        }
                        true
                    }
                    R.id.share_dream_menu ->{
                        val reportIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, vm.dream.value?.let { createDreamShare(it) })
                            putExtra(
                                Intent.EXTRA_SUBJECT,
                                getString(R.string.share_dream)
                            )
                        }
                        val chooserIntent = Intent.createChooser(
                            reportIntent,
                            getString(R.string.share_dream)
                        )
                        startActivity(chooserIntent)
                        true
                    }
                    else -> false
                }
            }

        },
            viewLifecycleOwner
        )

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.w("!!DDF!!", "Got args: ${args.dreamId}")


        binding.titleText.doOnTextChanged { text, _, _, _ ->
            vm.updateDream { oldDream ->
                oldDream.copy(title = text.toString()).apply { entries= oldDream.entries }

            }
        }

        binding.deferredCheckbox.setOnClickListener {

            vm.updateDream { oldDream ->
                oldDream.copy().apply {entries =
                    if(oldDream.isDeferred){
                        oldDream.entries.filter { it.kind != DreamEntryKind.DEFERRED }
                    } else {
                        oldDream.entries + DreamEntry (
                            kind = DreamEntryKind.DEFERRED,
                            dreamId = oldDream.id
                        )
                    }
                }
            }
        }

        binding.fulfilledCheckbox.setOnClickListener {

            vm.updateDream { oldDream ->
                oldDream.copy().apply {entries =
                    if(oldDream.isFulfilled){
                        oldDream.entries.filter { it.kind != DreamEntryKind.FULFILLED }
                    } else {
                        oldDream.entries + DreamEntry (
                            kind = DreamEntryKind.FULFILLED,
                            dreamId = oldDream.id
                        )
                    }
                }
            }
        }

        binding.addReflectionButton.setOnClickListener {
            findNavController().navigate(
                DreamDetailFragmentDirections.addReflection()
            )
        }

        setFragmentResultListener(ReflectionDialogFragment.REQUEST_KEY){
                _,bundle ->

            val entryText = bundle.getString(ReflectionDialogFragment.BUNDLE_KEY)
            entryText?.let { newText ->
                vm.updateDream { oldDream->
                    oldDream.copy().apply {
                        entries = oldDream.entries + DreamEntry(
                            kind = DreamEntryKind.REFLECTION,
                            text = newText,
                            dreamId = oldDream.id
                        )
                    }
                }
            }

        }

        binding.dreamEntryRecycler.layoutManager = LinearLayoutManager(context)
        getItemTouchHelper().attachToRecyclerView(binding.dreamEntryRecycler)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.dream.collect{ dream ->
                    dream?.let {
                        updateView(it)
                        binding.dreamEntryRecycler.adapter = DreamEntryAdapter(it.entries)

                    }
                }
            }
        }
        binding.dreamPhoto.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch{
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                    vm.dream.collect(){ dream ->
                        if (dream != null) {
                            findNavController().navigate(
                                DreamDetailFragmentDirections.showPhotoDetail(dream.photoFileName))
                        }
                    }
                }
            }
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateView(dream: Dream) {


        if(binding.titleText.text.toString() != dream.title) {
            binding.titleText.setText(dream.title)
        }

        val formattedDate = DateFormat.format("yyyy-MM-dd 'at' hh:mm:ss a", dream.lastUpdated)
        val lastUpdatedString = binding.root.context.getString(R.string.last_updated, formattedDate)
        binding.lastUpdatedText.text = lastUpdatedString

        binding.fulfilledCheckbox.isChecked = dream.isFulfilled
        binding.deferredCheckbox.isChecked = dream.isDeferred
        binding.fulfilledCheckbox.isEnabled = !dream.isDeferred
        binding.deferredCheckbox.isEnabled = !dream.isFulfilled


        if(dream.isFulfilled) {
            binding.addReflectionButton.hide()
        }
        else{
            binding.addReflectionButton.show()
        }

        updatePhoto(dream)


    }

    private fun updatePhoto(dream: Dream) {
        with(binding.dreamPhoto) {
            if (tag != dream.photoFileName) {
                val photoFile =
                    File(requireContext().applicationContext.filesDir, dream.photoFileName)
                if (photoFile.exists()) {
                    doOnLayout { imageView ->
                        val scaledBitMap = getScaledBitmap(
                            photoFile.path,
                            imageView.width,
                            imageView.height
                        )
                        setImageBitmap(scaledBitMap)
                        tag = dream.photoFileName
                        binding.dreamPhoto.isEnabled = true
                    }
                } else {
                    setImageBitmap(null)
                    tag = null
                    binding.dreamPhoto.isEnabled = false
                }
            }
        }
    }

    private fun createDreamShare(dream:Dream): String{

        val formattedDate = DateFormat.format("yyyy-MM-dd 'at' hh:mm:ss a",dream.lastUpdated)
        val lastUpdated = getString(R.string.last_updated, formattedDate)
        var status = ""
        if (dream.isDeferred){
            status = getString(R.string.share_dream_status,"Deferred.")
        }
        else if(dream.isFulfilled){
            status = getString(R.string.share_dream_status,"Fulfilled.")
        }
        var reflectionText = ""
        dream.entries.filter { it.kind == DreamEntryKind.REFLECTION }.forEach{
            reflectionText += "\n * "+ it.text
        }
        if(reflectionText!="") {
            return getString(
                R.string.share_dream, dream.title, lastUpdated,
                "Reflections:$reflectionText", status
            )
        }
        else{
            return getString(
                R.string.share_dream, dream.title, lastUpdated,
                "", status
            )
        }

    }


    private fun canResolveIntent (intent: Intent): Boolean {
        return requireActivity().packageManager
            .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null
    }



    private fun getItemTouchHelper(): ItemTouchHelper {
        return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, 0){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val deHolder = viewHolder as DreamEntryHolder
                    val swipedEntry = deHolder.boundEntry
                    vm.updateDream { oldDream ->
                        oldDream.copy().apply {
                            entries = oldDream.entries.filter { it.id != swipedEntry.id }
                        }
                    }

                }
            }

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {

                val deHolder = viewHolder as DreamEntryHolder
                val swipedEntry = deHolder.boundEntry
                return if(swipedEntry.kind == DreamEntryKind.REFLECTION) {
                    ItemTouchHelper.LEFT
                }
                else {
                    0
                }
            }

        })
    }

}