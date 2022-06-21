package com.mokhtar.mymemorygame

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.recyclerview.widget.GridLayoutManager
import com.mokhtar.mymemorygame.databinding.ActivityCreateBinding
import com.mokhtar.mymemorygame.databinding.ActivityMainBinding
import com.mokhtar.mymemorygame.models.BoardSize
import com.mokhtar.mymemorygame.utils.EXTRA_BOARD_SIZE

class CreateActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCreateBinding

    private lateinit var boardSize: BoardSize
    private var numImagesRequired = -1
    private var chosenImageUris = mutableListOf<Uri>()

    companion object{
        private const val TAG="CreateActivity"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        boardSize = intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize
        numImagesRequired=boardSize.getNumPairs()
        supportActionBar?.title="Choose pics (0 / $numImagesRequired)"

        binding.rvImagePicker.adapter=ImagePickerAdapter(this,chosenImageUris,boardSize)
        binding.rvImagePicker.setHasFixedSize(true)
        binding.rvImagePicker.layoutManager= GridLayoutManager(this,boardSize.getWidth())




    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }



}