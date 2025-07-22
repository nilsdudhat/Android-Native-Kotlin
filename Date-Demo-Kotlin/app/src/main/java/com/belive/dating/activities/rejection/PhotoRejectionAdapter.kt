package com.belive.dating.activities.rejection

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.activities.introduction.upload_photo.PhotoValidationModel
import com.belive.dating.activities.introduction.upload_photo.Reject
import com.belive.dating.databinding.ItemPhotoRejectionBinding
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.getKoinContext
import com.belive.dating.extensions.gsonString
import com.google.firebase.crashlytics.FirebaseCrashlytics

class PhotoRejectionAdapter(
    val viewModel: PhotosRejectionViewModel,
    val callback: OnImageClickListener,
) : RecyclerView.Adapter<PhotoRejectionAdapter.ViewHolder>() {

    interface OnImageClickListener {
        fun onPlaceHolderClick(pos: Int)
        fun onDeleteClick(pos: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPhotoRejectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.photo = viewModel.photoList.get()?.get(position)
        holder.binding.isProfileVisible = position == 0
        holder.binding.executePendingBindings()

        holder.binding.imgRemove.setOnClickListener {
            viewModel.photoList.set(viewModel.photoList.get()?.apply {
                removeAt(holder.absoluteAdapterPosition)
                add(PhotoValidationModel(null))
            })
            notifyItemRangeChanged(holder.bindingAdapterPosition, viewModel.photoList.get()!!.size)
            callback.onDeleteClick(holder.bindingAdapterPosition)
        }

        holder.binding.main.setOnClickListener {
            try {
                if (viewModel.photoList.get()?.get(holder.bindingAdapterPosition)?.reject != null) {
                    when (viewModel.photoList.get()?.get(holder.bindingAdapterPosition)?.reject) {
                        Reject.TOO_LOW_RESOLUTION -> {
                            Toast.makeText(getKoinContext(), "Low resolution photo, use a higher quality one", Toast.LENGTH_SHORT).show()
                        }

                        Reject.SOMETHING_WRONG -> {
                            Toast.makeText(getKoinContext(), "Something went wrong while processing your photo", Toast.LENGTH_SHORT).show()
                        }

                        Reject.FACE_NOT_FOUND -> {
                            Toast.makeText(getKoinContext(), "No face found in the photo, or having blurred face", Toast.LENGTH_SHORT).show()
                        }

                        Reject.NOT_SAFE -> {
                            Toast.makeText(getKoinContext(), "The photo contains inappropriate or sensitive content.", Toast.LENGTH_SHORT).show()
                        }

                        Reject.BLURRED -> {
                            Toast.makeText(getKoinContext(), "Your photo looks blurry or plain", Toast.LENGTH_SHORT).show()
                        }

                        Reject.ADMIN_REJECTED -> {
                            Toast.makeText(
                                getKoinContext(),
                                viewModel.photoList.get()?.get(holder.bindingAdapterPosition)?.reason
                                    ?: "Admin has rejected this photo because of policy violation",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }

                        else -> {
                            Toast.makeText(getKoinContext(), "Something went wrong, try again with different one", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    callback.onPlaceHolderClick(holder.bindingAdapterPosition)
                }
            } catch (e: Exception) {
                catchLog("PhotoRejectionAdapter: ${gsonString(e)}")

                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }

    fun updateItem(photoValidationModel: PhotoValidationModel) {
        val pos = viewModel.photoList.get()?.indexOfFirst { it.path == null }
        viewModel.photoList.set(viewModel.photoList.get()?.apply {
            pos?.let { set(it, photoValidationModel) }
        })
        pos?.let { notifyItemChanged(it) }
    }

    override fun getItemCount(): Int {
        return viewModel.photoList.get()?.size ?: 0
    }

    class ViewHolder(val binding: ItemPhotoRejectionBinding) : RecyclerView.ViewHolder(binding.root)
}