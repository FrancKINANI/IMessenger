package i.imessenger.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import i.imessenger.R;
import i.imessenger.models.Project;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private List<Project> projects = new ArrayList<>();
    private OnProjectClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public interface OnProjectClickListener {
        void onProjectClick(Project project);
    }

    public ProjectAdapter(OnProjectClickListener listener) {
        this.listener = listener;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projects.get(position);

        holder.textProjectName.setText(project.getName());
        holder.textProjectDescription.setText(project.getDescription());

        // Project type
        String typeDisplay = "Project";
        if (project.getProjectType() != null) {
            switch (project.getProjectType()) {
                case "CLUB":
                    typeDisplay = "Club";
                    break;
                case "STUDY_GROUP":
                    typeDisplay = "Study Group";
                    break;
                default:
                    typeDisplay = "Project";
            }
        }
        holder.textProjectType.setText(typeDisplay);

        // Member count
        int memberCount = project.getMembers() != null ? project.getMembers().size() : 0;
        holder.textMemberCount.setText(memberCount + " member" + (memberCount != 1 ? "s" : ""));

        // Deadline
        if (project.getDeadline() > 0) {
            holder.textDeadline.setText(dateFormat.format(new Date(project.getDeadline())));
        } else {
            holder.textDeadline.setText("No deadline");
        }

        // Status
        if (project.getStatus() != null) {
            holder.textStatus.setText(project.getStatus());
            switch (project.getStatus()) {
                case "ACTIVE":
                    holder.textStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.ismagi_accent));
                    break;
                case "COMPLETED":
                    holder.textStatus.setTextColor(0xFF10B981);
                    break;
                case "ARCHIVED":
                    holder.textStatus.setTextColor(0xFF64748B);
                    break;
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProjectClick(project);
            }
        });
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView textProjectName, textProjectType, textProjectDescription;
        TextView textMemberCount, textDeadline, textStatus;
        ImageView imageProject;

        ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            textProjectName = itemView.findViewById(R.id.textProjectName);
            textProjectType = itemView.findViewById(R.id.textProjectType);
            textProjectDescription = itemView.findViewById(R.id.textProjectDescription);
            textMemberCount = itemView.findViewById(R.id.textMemberCount);
            textDeadline = itemView.findViewById(R.id.textDeadline);
            textStatus = itemView.findViewById(R.id.textStatus);
            imageProject = itemView.findViewById(R.id.imageProject);
        }
    }
}

