package com.mstar004.holymoly.view

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.mstar004.holymoly.R
import com.pstorli.wackymole.model.MoleModel
import com.pstorli.wackymole.util.MoleType

/**
 * This class manage the board (grid)
 */
class MoleAdapter (var model: MoleModel) : BaseAdapter () {
    // *********************************************************************************************
    // Images
    // *********************************************************************************************
    lateinit var bomb:  Bitmap       // A bomb!
    lateinit var gress: Bitmap       // Our gress
    lateinit var hole111:  Bitmap       // A hole111

    // Our moles, real russian ones.
    lateinit var mole1: Bitmap       // Mole1
    lateinit var mole2: Bitmap       // Mole2
    lateinit var mole3: Bitmap       // Mole3

    init {
        // Load up the moles (bitmaps/pngs) to use on the board.
        loadImages ()
    }

    /**
     * Load the images for later use on the board.
     */
    fun loadImages () {
        // Get some resources and pull yourself up by your boot straps.
        val resources : Resources = model.context().resources

        // What is frankincense? A balm. You gave the baby a bomb? Life of Brian.
        bomb = BitmapFactory.decodeResource(resources, R.drawable.bomb)

        // Get some gress, cuz no one rides for free!
        gress = BitmapFactory.decodeResource(resources, R.drawable.gress)

        // Diabetes can make you a real A hole111.
        hole111 = BitmapFactory.decodeResource(resources, R.drawable.hole111)

        // Send in the clowns, I mean moles.
        mole1 = BitmapFactory.decodeResource(resources, R.drawable.mole1)
        mole2 = BitmapFactory.decodeResource(resources, R.drawable.mole2)
        mole3 = BitmapFactory.decodeResource(resources, R.drawable.mole3)
    }

    fun getDrawable (type: MoleType?): Int {
        val drawableId: Int
        when (type) {
            MoleType.BOMB -> drawableId = R.drawable.boom1
            MoleType.GRASS -> drawableId = R.drawable.gress
            MoleType.HOLE -> drawableId = R.drawable.hole111
            MoleType.MOLE1 -> drawableId = R.drawable.mole11
            MoleType.MOLE2 -> drawableId = R.drawable.mole22
            MoleType.MOLE3 -> drawableId = R.drawable.mole33

            else ->
                drawableId = R.drawable.gress
        }

        return drawableId
    }


    override fun getCount(): Int {
        return model.moles.size
    }


    override fun getItem(position: Int): MoleType {
        return model.moles[position]?: MoleType.GRASS
    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    @SuppressLint("InflateParams")
    override fun getView (position: Int, convertView: View?, parent: ViewGroup?): View {
        // Possibly resuse an existing view?
        var view = convertView
        val holder: MoleHolder

        // Got view?
        if (null == view) {
            // Load up the mole view.
            view             = LayoutInflater.from (model.context()).inflate(R.layout.mole_layout, null)

            // Create the biew holder
            holder           = MoleHolder()

            // Find the image view.
            holder.imageView = view?.findViewById(R.id.mole_view)


            // Assign the holder to the view.
            view.tag = holder
        }
        else {
            holder = view.tag as MoleHolder
        }

        // Set the correct image.
        holder.imageView?.setImageResource(getDrawable(model.moles[position]))

        // View should not be null now.
        return view!!
    }
}