package i.imessenger.utils;

import android.view.View;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;

import i.imessenger.R;

public class BindingAdapters {

    @BindingAdapter("imageProfile")
    public static void loadImage(ImageView view, String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(view.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.logo)
                    .into(view);
        } else {
            view.setImageResource(R.drawable.logo);
        }
    }

    @BindingAdapter("android:visibility")
    public static void setVisibility(View view, boolean isVisible) {
        view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }
}
