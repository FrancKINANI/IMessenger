package i.imessenger.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.Navigation;
import android.os.Bundle;

import com.bumptech.glide.Glide;

import java.util.List;

import i.imessenger.R;
import i.imessenger.databinding.ItemUserBinding;
import i.imessenger.models.User;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private Context context;
    private List<User> userList;

    public UsersAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding binding = ItemUserBinding.inflate(LayoutInflater.from(context), parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.binding.tvUserName.setText(user.getFullName());
        holder.binding.tvUserEmail.setText(user.getEmail());

        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            Glide.with(context)
                    .load(user.getProfileImage())
                    .placeholder(R.drawable.logo)
                    .into(holder.binding.ivUserProfile);
        } else {
            holder.binding.ivUserProfile.setImageResource(R.drawable.logo);
        }

        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("userId", user.getUid());
            bundle.putString("userName", user.getFullName());
            bundle.putString("userImage", user.getProfileImage());
            Navigation.findNavController(v).navigate(R.id.chatFragment, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ItemUserBinding binding;

        public UserViewHolder(@NonNull ItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
