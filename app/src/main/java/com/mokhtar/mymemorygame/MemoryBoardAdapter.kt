package com.mokhtar.mymemorygame

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.mokhtar.mymemorygame.models.BoardSize
import com.mokhtar.mymemorygame.models.MemoryCard
import kotlin.math.min

class MemoryBoardAdapter(
    private val context: Context,
    private val boardSize: BoardSize,
    private val cards: List<MemoryCard>,
    private val cardClickListerner: CardClickListerner
) :
    RecyclerView.Adapter<MemoryBoardAdapter.ViewHolder>() {

    companion object{
        private const val MARGIN_SIZE = 10
        private const val TAG = "MemoryBoardAdapter"
    }

    interface CardClickListerner{
        fun onCardClicked(position: Int)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val cardWidh= parent.width/boardSize.getWidth() - (2 * MARGIN_SIZE)
        val cardheight= parent.height/boardSize.getHeight() - (2 * MARGIN_SIZE)
        val cardSideLength= min(cardWidh,cardheight) /** i didn't understand yett !!!! */
        val view :View =LayoutInflater.from(context).inflate(R.layout.memory_card,parent,false)
        val layoutParams= view.findViewById<CardView>(R.id.cardView).layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.width=cardSideLength
        layoutParams.height=cardSideLength
        layoutParams.setMargins(MARGIN_SIZE , MARGIN_SIZE , MARGIN_SIZE , MARGIN_SIZE)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int =boardSize.numCards

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageButton =itemView.findViewById<ImageButton>(R.id.imageButton)

        fun bind(position: Int) {
            val memoryCard = cards[position]
            imageButton.setImageResource( if (memoryCard.isFaceUp) memoryCard.identifier else R.drawable.ic_default)

            imageButton.alpha = if (memoryCard.isMatch) .4f else 1.0f
            val colorStateList = if (memoryCard.isMatch) ContextCompat.getColorStateList(context,R.color.color_grey) else null
            ViewCompat.setBackgroundTintList(imageButton,colorStateList)

            imageButton.setOnClickListener {
                Log.i(TAG, "Clicked on position $position")
                cardClickListerner.onCardClicked(position)

            }
        }
    }
}
