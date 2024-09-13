package com.example.fftrak;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private List<UserLocation> usersList;
    private final OnUserClickListener onUserClickListener;
    private UserLocation selectedUser;

    public UsersAdapter(List<UserLocation> usersList, OnUserClickListener onUserClickListener) {
        this.usersList = usersList;
        this.onUserClickListener = onUserClickListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserLocation user = usersList.get(position);
        holder.userNameTextView.setText(user.getId());
        holder.userAnimationView.playAnimation();

        // Set the CheckBox state and listener
        holder.userSelectCheckBox.setOnCheckedChangeListener(null); // Clear previous listener
        holder.userSelectCheckBox.setChecked(user.equals(selectedUser));

        holder.userSelectCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Deselect the previously selected user
                if (selectedUser != null && !selectedUser.equals(user)) {
                    int previousIndex = usersList.indexOf(selectedUser);
                    if (previousIndex != -1) {
                        notifyItemChanged(previousIndex);
                    }
                }
                selectedUser = user;
                onUserClickListener.onUserClick(user);
            } else {
                if (selectedUser != null && selectedUser.equals(user)) {
                    selectedUser = null;
                }
            }
            notifyItemChanged(position); // Update current item
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public UserLocation getSelectedUser() {
        return selectedUser;
    }

    public void updateUsers(List<UserLocation> newUsersList) {
        this.usersList = newUsersList;
        selectedUser = null; // Reset selected user when updating the list
        notifyDataSetChanged();
    }

    // New method to clear the users list
    public void clearUsers() {
        this.usersList.clear();
        selectedUser = null;
        notifyDataSetChanged();
    }

    public interface OnUserClickListener {
        void onUserClick(UserLocation user);
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView;
        LottieAnimationView userAnimationView;
        CheckBox userSelectCheckBox;

        UserViewHolder(View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            userAnimationView = itemView.findViewById(R.id.scanningAnimation2);
            userSelectCheckBox = itemView.findViewById(R.id.userSelectCheckBox);
        }
    }
}
