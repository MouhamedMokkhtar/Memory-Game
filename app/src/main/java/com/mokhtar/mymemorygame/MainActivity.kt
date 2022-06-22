package com.mokhtar.mymemorygame

import android.animation.ArgbEvaluator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayoutStates
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import com.google.android.material.snackbar.Snackbar
import com.mokhtar.mymemorygame.databinding.ActivityMainBinding
import com.mokhtar.mymemorygame.models.BoardSize
import com.mokhtar.mymemorygame.models.MemoryGame
import com.mokhtar.mymemorygame.utils.EXTRA_BOARD_SIZE

class MainActivity : AppCompatActivity() {


    private lateinit var binding :ActivityMainBinding

    private lateinit var clRoot : ConstraintLayout


    private lateinit var memoryGame: MemoryGame
    private lateinit var adapter: MemoryBoardAdapter
    private var boardSize:BoardSize = BoardSize.EASY

    companion object{
        private const val TAG="MainActivity"
        private const val CREATE_REQUEST_CODE = 248
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clRoot=findViewById(R.id.clRoot)

        val intent =Intent(this,CreateActivity::class.java)
        intent.putExtra(EXTRA_BOARD_SIZE,BoardSize.MEDIUM)
        startActivity(intent)

        setupBoard()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.mi_refresh ->{
                if (memoryGame.getNumMoves() > 0 && !memoryGame.haveWonGame()){
                    showAlertDialog("Quit your current game?", null, View.OnClickListener {
                        setupBoard()
                    })
                }else{
                    setupBoard()
                }

            }
            R.id.mi_new_size->{
                showNewSizeDialog()
                return true
            }

            R.id.mi_custom -> {
                showCreationDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCreationDialog() {
        val boardSizeView=LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize=boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        showAlertDialog("Create your memory board", boardSizeView, View.OnClickListener {
            // set a new value for the board size
            val desiredBoradSize :BoardSize =when(radioGroupSize.checkedRadioButtonId){
                R.id.rbEasy ->BoardSize.EASY
                R.id.rbMeduim ->BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            // navigate user to a new activity for to create a new memory game board
            val intent = Intent (this,CreateActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE,desiredBoradSize)
            startActivityForResult(intent, CREATE_REQUEST_CODE)

        })

    }

    private fun showNewSizeDialog() {
        val boardSizeView=LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize=boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        when (boardSize){
            BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMeduim)
            BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)

        }
        showAlertDialog("Choose new size", boardSizeView, View.OnClickListener {
            // set a new value for the board size
            boardSize=when(radioGroupSize.checkedRadioButtonId){
                R.id.rbEasy ->BoardSize.EASY
                R.id.rbMeduim ->BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            setupBoard()

        })
    }

    private fun showAlertDialog(title:String,view:View?,positiveButtonClickListener: View.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel",null)
            .setPositiveButton("ok"){_,_ ->
                positiveButtonClickListener.onClick(null)

            }.show()
    }

    private fun setupBoard() {

        when(boardSize){
            BoardSize.EASY -> {
                binding.tvNumMoves.text="Easy: 4 x 2"
                binding.tvNumPairs.text="Pairs: 0 / 4"
            }
            BoardSize.MEDIUM -> {
                binding.tvNumMoves.text="Meduim: 6 x 3"
                binding.tvNumPairs.text="Pairs: 0 / 9"
            }
            BoardSize.HARD -> {
                binding.tvNumMoves.text="Hard: 6 x 4"
                binding.tvNumPairs.text="Pairs: 0 / 12"
            }
        }

        memoryGame = MemoryGame(boardSize)
        adapter= MemoryBoardAdapter(this, boardSize, memoryGame.cards, object :MemoryBoardAdapter.CardClickListerner{
            override fun onCardClicked(position: Int) {
                //Log.i(TAG, "Clicked on position $position")
                updateGameWithFlipPosition(position)
            }

        })
        binding.rvBoard.adapter=adapter
        binding.rvBoard.setHasFixedSize(true)
        // every recycle view has an adapter and a layout manager ,so we are gonna use the GridLayoutManager
        // because we need to specify the number of columns we need
        binding.rvBoard.layoutManager= GridLayoutManager(this,boardSize.getWidth())
    }

    private fun updateGameWithFlipPosition(position: Int) {
        // error cheking
        if (memoryGame.haveWonGame()){
            // alert user of invalid move
            Snackbar.make(clRoot, "you already won !!!",Snackbar.LENGTH_LONG).show()
            return
        }
        if (memoryGame.isCardFaceUp(position)){
            // alert user of invalid move
            Snackbar.make(clRoot, "invalide move !!! ",Snackbar.LENGTH_SHORT).show()
            return
        }

        // flipping the card
        if (memoryGame.flipCard(position)){
            Log.i(TAG, "Found a match! Num pairs found: ${memoryGame.numPairsFound}")
            val color= ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat() / boardSize.getNumPairs(),
                ContextCompat.getColor(this,R.color.color_progress_none),
                ContextCompat.getColor(this,R.color.color_progress_full),
                ) as Int
            binding.tvNumPairs.setTextColor(color)
            binding.tvNumPairs.text="Pairs:${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"
            if (memoryGame.haveWonGame()){
                Snackbar.make(clRoot,"You won !!! Congratulations ",Snackbar.LENGTH_LONG).show()
            }
        }
        binding.tvNumMoves.text="Moves: ${memoryGame.getNumMoves()}"
        adapter.notifyDataSetChanged()
    }
}