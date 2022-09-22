package com.omerakcinar.kotlinlandmarkbook_basic

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.omerakcinar.kotlinlandmarkbook_basic.databinding.ActivityNewLandmarkBinding
import com.omerakcinar.kotlinlandmarkbook_basic.databinding.EachRowBinding

class RecyclerAdapter (val landmarkList : ArrayList<Landmark>) : RecyclerView.Adapter<RecyclerAdapter.LandmarkHolder>() {
    class LandmarkHolder (val binding: EachRowBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LandmarkHolder {
        val binding = EachRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return LandmarkHolder(binding)
    }

    override fun onBindViewHolder(holder: LandmarkHolder, position: Int) {
        holder.binding.recyclerRowName.text = landmarkList.get(position).landmarkName
        holder.binding.recyclerRowCity.text = landmarkList.get(position).landmarkCity
        val selectedBitmap = BitmapFactory.decodeByteArray(landmarkList.get(position).image,0,landmarkList.get(position).image.size)
        holder.binding.recyclerRowImage.setImageBitmap(selectedBitmap)
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context,NewLandmark::class.java)
            intent.putExtra("id",landmarkList.get(position).id)
            intent.putExtra("target","showItem")
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return landmarkList.size
    }
}