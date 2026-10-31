package com.example.dogsitmalta.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogsitmalta.R;
import com.example.dogsitmalta.model.BlogPost;
import com.example.dogsitmalta.utils.MapIntentUtils;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlogPostAdapter
        extends RecyclerView.Adapter<BlogPostAdapter.PostViewHolder> {

    public interface Listener {
        void onPostClicked(BlogPost post);
        void onPostLongClicked(BlogPost post);
        void onSelectionChanged();
    }

    private final List<BlogPost> posts = new ArrayList<>();
    private final Set<Long> selectedIds = new HashSet<>();
    private final Listener listener;

    private boolean selectionMode = false;

    public BlogPostAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setPosts(List<BlogPost> newPosts) {
        posts.clear();

        if (newPosts != null) {
            posts.addAll(newPosts);
        }

        notifyDataSetChanged();
    }

    public void setSelectionMode(boolean enabled) {
        selectionMode = enabled;

        if (!enabled) {
            selectedIds.clear();
        }

        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return selectedIds.size();
    }

    public List<Long> getSelectedIds() {
        return new ArrayList<>(selectedIds);
    }

    public List<BlogPost> getSelectedPosts() {
        List<BlogPost> selectedPosts = new ArrayList<>();

        for (BlogPost post : posts) {
            if (selectedIds.contains(post.getId())) {
                selectedPosts.add(post);
            }
        }

        return selectedPosts;
    }

    public void toggleSelection(long postId) {
        if (selectedIds.contains(postId)) {
            selectedIds.remove(postId);
        } else {
            selectedIds.add(postId);
        }

        notifyDataSetChanged();
        listener.onSelectionChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_blog_post, parent, false);

        return new PostViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PostViewHolder holder,
            int position
    ) {
        BlogPost post = posts.get(position);
        boolean selected = selectedIds.contains(post.getId());

        holder.title.setText(post.getTitle());

        if (MapIntentUtils.canOpen(post)) {
            holder.location.setVisibility(View.GONE);
            holder.location.setOnClickListener(null);
            holder.location.setClickable(false);
        } else {
            holder.location.setVisibility(View.VISIBLE);
            holder.location.setText(MapIntentUtils.locationLabel(
                    holder.itemView.getContext(), post));
            holder.location.setContentDescription(holder.itemView.getContext().getString(
                    R.string.map_open_locality,
                    MapIntentUtils.locationLabel(holder.itemView.getContext(), post)));
            holder.location.setPaintFlags(holder.location.getPaintFlags()
                    | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
            holder.location.setOnClickListener(v -> {
                if (selectionMode) toggleSelection(post.getId());
                else MapIntentUtils.openSavedLocation(holder.itemView.getContext(), post);
            });
        }

        String preview =
                post.getMessage() == null ? "" : post.getMessage().trim();

        if (preview.length() > 130) {
            preview = preview.substring(0, 130) + "…";
        }

        holder.preview.setText(preview);

        holder.date.setText(
                DateFormat.getDateTimeInstance(
                        DateFormat.MEDIUM,
                        DateFormat.SHORT
                ).format(new Date(post.getCreatedAt()))
        );

        String imagePath = post.getImagePath();

        if (imagePath != null && !imagePath.trim().isEmpty()) {
            File file = new File(imagePath);

            if (file.exists()) {
                holder.image.setImageURI(null);
                holder.image.setImageURI(Uri.fromFile(file));
                holder.image.setVisibility(View.VISIBLE);
            } else {
                holder.image.setVisibility(View.GONE);
            }
        } else {
            holder.image.setVisibility(View.GONE);
        }

        holder.checkBox.setVisibility(
                selectionMode ? View.VISIBLE : View.GONE
        );
        holder.checkBox.setChecked(selected);
        holder.itemView.setActivated(selected);

        holder.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                toggleSelection(post.getId());
            } else {
                listener.onPostClicked(post);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            listener.onPostLongClicked(post);
            return true;
        });

        holder.checkBox.setOnClickListener(
                v -> toggleSelection(post.getId())
        );
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {

        final TextView title;
        final TextView location;
        final TextView preview;
        final TextView date;
        final ImageView image;
        final CheckBox checkBox;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.textPostTitle);
            location = itemView.findViewById(R.id.textPostLocation);
            preview = itemView.findViewById(R.id.textPostPreview);
            date = itemView.findViewById(R.id.textPostDate);
            image = itemView.findViewById(R.id.imagePostThumbnail);
            checkBox = itemView.findViewById(R.id.checkPostSelected);
        }
    }
}
