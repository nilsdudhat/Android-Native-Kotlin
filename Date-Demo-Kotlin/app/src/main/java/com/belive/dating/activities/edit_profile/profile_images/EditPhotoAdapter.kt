package com.belive.dating.activities.edit_profile.profile_images

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.belive.dating.activities.introduction.upload_photo.PhotoValidationModel
import com.belive.dating.activities.introduction.upload_photo.Reject
import com.belive.dating.databinding.ItemEditProfilePhotoBinding
import com.belive.dating.extensions.getKoinContext

/**
 * Adapter for displaying and managing a list of photos in the edit profile screen.
 *
 * This adapter handles displaying placeholders for new photos, existing photos,
 * and allows users to add, delete, and reorder photos. It also provides feedback
 * to the user regarding image processing failures through Toast messages.
 *
 * The adapter utilizes a `PhotoValidationModel` to represent each photo, which includes
 * the photo's path (if available) and a rejection reason (if the photo was rejected during processing).
 * Placeholders are represented by `PhotoValidationModel` instances with a null path.
 *
 * @property viewModel The [EditPhotosViewModel] that holds and manages the photo data
 *                   as a LiveData list of [PhotoValidationModel]s.  This ViewModel is
 *                   responsible for providing the data and handling photo operations
 *                   (e.g., adding, deleting).
 * @property callback An [OnImageClickListener] interface for handling user interactions,
 *                    specifically clicks on placeholders to add new photos and clicks on
 *                    delete buttons to remove existing photos.
 */
class EditPhotoAdapter(
    val viewModel: EditPhotosViewModel,
    val callback: OnImageClickListener,
) : RecyclerView.Adapter<EditPhotoAdapter.ViewHolder>() {

    interface OnImageClickListener {
        fun onPlaceHolderClick(pos: Int)
        fun onDeleteClick(pos: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEditProfilePhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
                        Toast.makeText(getKoinContext(), "The photo contains inappropriate or sensitive content", Toast.LENGTH_SHORT).show()
                    }

                    Reject.BLURRED -> {
                        Toast.makeText(getKoinContext(), "Your photo looks blurry or plain", Toast.LENGTH_SHORT).show()
                    }

                    else -> {
                        Toast.makeText(getKoinContext(), "Something went wrong, try again with different one", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                callback.onPlaceHolderClick(holder.bindingAdapterPosition)
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

    fun insertPlaceholder() {
        viewModel.photoList.get()?.add(0, PhotoValidationModel(null))
        notifyItemRangeChanged(0, viewModel.photoList.get()!!.size)
    }

    class ViewHolder(val binding: ItemEditProfilePhotoBinding) : RecyclerView.ViewHolder(binding.root)
}