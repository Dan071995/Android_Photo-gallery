package com.example.cameraroomrecyclerview.presentation

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cameraroomrecyclerview.App
import com.example.cameraroomrecyclerview.R
import com.example.cameraroomrecyclerview.databinding.FragmentMainBlankBinding
import com.example.cameraroomrecyclerview.entity.FileInfo
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainBlankFragment : Fragment() {

    companion object {
        private val REQUEST_PERMISSIONS: Array<String> = buildList {
            add(android.Manifest.permission.CAMERA)
            //Разрешение на работу с файловой системой ОПАСНОЕ, начиная с Android 9
            //=> нам необходимо добавить проверку версии SDK
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    private var _binding: FragmentMainBlankBinding? = null
    private val binding get() = _binding!!

    private val myAdapter = MyAdapter{Uri,itemPosition ->
        onItemClick(Uri,itemPosition)
    }
    private val viewModel:MainViewModel by activityViewModels{
        object : ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val fileInfoDAO = (requireContext().applicationContext as App).dataBase.fileInfoDao()
                return MainViewModel(fileInfoDAO,requireContext()) as T
            }
        }
    }


    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            if (!map.values.all { it }) Toast.makeText(requireContext(), "Permission is: NOT Granted =(", Toast.LENGTH_LONG).show()
        }

    private val makePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()){ saveInFileSuccess ->
        if (saveInFileSuccess){
            Toast.makeText(requireContext(),"Фото сохранено!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBlankBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.myRecyclerView.adapter = myAdapter

        binding.buttonMakePhoto.setOnClickListener {
            if (checkPermissions()){
                createFileAndSaveInDB().let {
                    makePhotoLauncher.launch(it) //Сохраняем фото в созданный файл
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.allFilesFromDB.collect{
                myAdapter.setData(it.toMutableList())
            }
        }

    }

    private fun checkPermissions(): Boolean {
        val isAllGranted = REQUEST_PERMISSIONS.all { currentPermission ->
            ContextCompat.checkSelfPermission(
                requireContext(),
                currentPermission
            ) == PackageManager.PERMISSION_GRANTED
        }
        if (isAllGranted) {
            Toast.makeText(requireContext(), "Permission is Granted", Toast.LENGTH_SHORT).show()
            return true
        }
        else {
            permissionLauncher.launch(REQUEST_PERMISSIONS) //Передаем разрешение в лаунчер
            return false
        }
    }

    // создание временного файла и получаем его URI
    // для работы с файловой системой необходимо описать FileProvider в файле AndroidManifest
    // конфигурация FileProvider находится в файле /res/xml/provider_paths
    private fun createFileAndSaveInDB(): Uri? {

        val fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy\n(HH:mm:ss)"))
        val myFile = File(requireContext().applicationContext.filesDir, "$fileName.png") //Создаем файл в постоянной директории внутреннего хранилища
        Log.d("123", requireContext().applicationContext.filesDir.toString())
        val fileUri = FileProvider.getUriForFile( //получаем uri файла
            requireContext().applicationContext,
            "com.example.cameraroomrecyclerview" + ".provider",
            myFile
        )
        viewModel.addFileInDB(FileInfo(fileUri.toString(),fileName)) //сохраняем файл в БД
        return fileUri
    }

    //Функция перехода на другой фрагмент при клике по элементу списка
    private fun onItemClick(itemUri:Uri, itemPosition:Int){
        val bundle = Bundle().apply {
            putString("UriAsString_KEY",itemUri.toString())
            putInt("ItemPosition_KEY",itemPosition)
        }
        findNavController().navigate(R.id.action_mainBlankFragment_to_itemClickBlankFragment,bundle)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}