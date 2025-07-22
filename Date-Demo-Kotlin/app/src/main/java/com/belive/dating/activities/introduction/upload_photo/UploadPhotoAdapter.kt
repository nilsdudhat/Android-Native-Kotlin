package com.belive.dating.activities.introduction.upload_photo

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.databinding.ItemUploadPhotoBinding
import com.belive.dating.extensions.catchLog
import com.belive.dating.extensions.getKoinContext
import com.belive.dating.extensions.gsonString
import com.google.firebase.crashlytics.FirebaseCrashlytics

class UploadPhotoAdapter(
    private var callBack: OnUploadImageListener,
    val viewModel: UploadPhotoViewModel,
) :
    RecyclerView.Adapter<UploadPhotoAdapter.ViewHolder>() {

    interface OnUploadImageListener {
        fun onAddClickCallBack(pos: Int)
        fun onDeleteClickCallBack(pos: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUploadPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.photo = viewModel.uploadList.get()?.get(position)
        holder.binding.isProfileVisible = position == 0
        holder.binding.executePendingBindings()

        holder.binding.imgRemove.setOnClickListener {
            viewModel.uploadList.set(viewModel.uploadList.get()?.apply {
                removeAt(holder.absoluteAdapterPosition)
                add(PhotoValidationModel(null))
            })
            notifyItemRangeChanged(holder.absoluteAdapterPosition, viewModel.uploadList.get()!!.size)
            callBack.onDeleteClickCallBack(position)
        }

        holder.binding.main.setOnClickListener {
            if (viewModel.uploadList.get()?.get(position)?.reject != null) {
                when (viewModel.uploadList.get()?.get(position)?.reject) {
                    Reject.TOO_LOW_RESOLUTION -> {
                        Toast.makeText(getKoinContext(), "Low resolution image, use a higher quality one", Toast.LENGTH_SHORT).show()
                    }

                    Reject.SOMETHING_WRONG -> {
                        Toast.makeText(getKoinContext(), "Something went wrong while processing your image", Toast.LENGTH_SHORT).show()
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

                    else -> {
                        Toast.makeText(getKoinContext(), "Something went wrong, try again with different one", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                callBack.onAddClickCallBack(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return viewModel.uploadList.get()?.size ?: 0
    }

    fun updateItem(photoValidationModel: PhotoValidationModel) {
        try {
            val pos = viewModel.uploadList.get()?.indexOfFirst { it.path == null }
            viewModel.uploadList.set(viewModel.uploadList.get()?.apply {
                pos?.let { set(it, photoValidationModel) }
            })
            pos?.let { notifyItemChanged(it) }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)

            catchLog("updateItem: ${gsonString(e)}")
        }
    }

    class ViewHolder(val binding: ItemUploadPhotoBinding) : RecyclerView.ViewHolder(binding.root)
}