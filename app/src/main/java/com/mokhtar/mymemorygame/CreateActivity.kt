package com.mokhtar.mymemorygame

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.recyclerview.widget.GridLayoutManager
import com.mokhtar.mymemorygame.databinding.ActivityCreateBinding
import com.mokhtar.mymemorygame.databinding.ActivityMainBinding
import com.mokhtar.mymemorygame.models.BoardSize
import com.mokhtar.mymemorygame.utils.EXTRA_BOARD_SIZE
import com.mokhtar.mymemorygame.utils.isPermissionGranted
import com.mokhtar.mymemorygame.utils.requestPermission

class CreateActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCreateBinding

    private lateinit var boardSize: BoardSize
    private var numImagesRequired = -1
    private var chosenImageUris = mutableListOf<Uri>()
    private lateinit var adapter :ImagePickerAdapter

    companion object{
        private const val TAG="CreateActivity"
        private const val PICK_PHOTO_REQUEST_CODE= 655
        private const val READ_EXTERNAL_PHOTO_REQUEST_CODE= 248
        private const val READ_PHOTO_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        boardSize = intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize
        numImagesRequired=boardSize.getNumPairs()
        supportActionBar?.title="Choose pics (0 / $numImagesRequired)"


        adapter=ImagePickerAdapter(this,chosenImageUris,boardSize,object: ImagePickerAdapter.ImageClickListener{
            override fun onPlaceholderClicked() {
                if (isPermissionGranted(this@CreateActivity,READ_PHOTO_PERMISSION)){
                    launchIntentForPhotos()
                }else{
                    requestPermission(this@CreateActivity, READ_PHOTO_PERMISSION,
                        READ_EXTERNAL_PHOTO_REQUEST_CODE)
                }

            }

        })
        binding.rvImagePicker.adapter=adapter
        binding.rvImagePicker.setHasFixedSize(true)
        binding.rvImagePicker.layoutManager= GridLayoutManager(this,boardSize.getWidth())




    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == PICK_PHOTO_REQUEST_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                launchIntentForPhotos()
            }else{
                Toast.makeText(this,"In order to create a custom game , you need to provide acces to your photos",Toast.LENGTH_LONG).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if( requestCode != PICK_PHOTO_REQUEST_CODE || resultCode != Activity.RESULT_OK || data == null){
            Log.w(TAG, "Did not get data back from the launch activity , user lickely canceled flow")
            return
        }
        val selectedUri = data.data
        val clipData = data.clipData
        if(clipData != null ){
            Log.i(TAG,"clipData  numImages ${clipData.itemCount}: $clipData")
            for(i in 0 until clipData.itemCount){
                val clipItem = clipData.getItemAt(i)
                if(chosenImageUris.size<numImagesRequired){
                    chosenImageUris.add(clipItem.uri)
                }

            }
        } else if ( selectedUri != null ){
            Log.i(TAG,"data : $selectedUri")
            chosenImageUris.add((selectedUri))
        }
        adapter.notifyDataSetChanged()
        supportActionBar?.title = "Choose pics (${chosenImageUris.size} / $numImagesRequired)"


    }

    private fun launchIntentForPhotos() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type="image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
        startActivityForResult(Intent.createChooser(intent,"Choose pics"),PICK_PHOTO_REQUEST_CODE)
    }



}