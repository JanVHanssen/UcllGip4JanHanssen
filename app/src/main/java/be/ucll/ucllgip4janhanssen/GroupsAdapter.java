package be.ucll.ucllgip4janhanssen;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupsViewHolder> {

    private List<String> groupNames = new ArrayList<>();
    private OnGroupsClickListener onGroupsClickListener;

    public GroupsAdapter(OnGroupsClickListener listener) {
        this.onGroupsClickListener = listener;
    }

    @NonNull
    @Override
    public GroupsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.groups_item, parent, false);
        return new GroupsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupsViewHolder holder, int position) {
        String groupName = groupNames.get(position);
        holder.bind(groupName);
    }

    @Override
    public int getItemCount() {
        return groupNames.size();
    }

    public void setGroupNames(List<String> groupNames) {
        this.groupNames = groupNames;
        notifyDataSetChanged();
    }

    class GroupsViewHolder extends RecyclerView.ViewHolder {
        private TextView groupNameTextView;

        public GroupsViewHolder(View itemView) {
            super(itemView);
            groupNameTextView = itemView.findViewById(R.id.text_view_group_name);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onGroupsClickListener != null) {
                    onGroupsClickListener.onGroupsClick(groupNames.get(position));
                }
            });
        }

        public void bind(String groupName) {
            groupNameTextView.setText(groupName);
        }
    }

    public interface OnGroupsClickListener {
        void onGroupsClick(String groupName);
    }
}