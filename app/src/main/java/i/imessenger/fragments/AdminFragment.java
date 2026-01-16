package i.imessenger.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import i.imessenger.R;
import i.imessenger.models.Report;
import i.imessenger.viewmodels.AdminViewModel;

public class AdminFragment extends Fragment {

    private AdminViewModel adminViewModel;
    private ReportAdapter reportAdapter;
    private RecyclerView recyclerReports, recyclerUsers;
    private View layoutStats, emptyView;
    private TabLayout tabLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adminViewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        recyclerReports = view.findViewById(R.id.recyclerReports);
        recyclerUsers = view.findViewById(R.id.recyclerUsers);
        layoutStats = view.findViewById(R.id.layoutStats);
        emptyView = view.findViewById(R.id.emptyView);
        tabLayout = view.findViewById(R.id.tabLayout);

        recyclerReports.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerUsers.setLayoutManager(new LinearLayoutManager(getContext()));

        reportAdapter = new ReportAdapter();
        recyclerReports.setAdapter(reportAdapter);

        view.findViewById(R.id.toolbar).setOnClickListener(v -> requireActivity().onBackPressed());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        loadReports();
        loadStats(view);
    }

    private void showTab(int position) {
        recyclerReports.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        recyclerUsers.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
        layoutStats.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
    }

    private void loadReports() {
        adminViewModel.getReports().observe(getViewLifecycleOwner(), reports -> {
            if (reports.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                recyclerReports.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                recyclerReports.setVisibility(View.VISIBLE);
                reportAdapter.setReports(reports);
            }
        });
    }

    private void loadStats(View view) {
        View cardUser = view.findViewById(R.id.cardUserCount);
        View cardPost = view.findViewById(R.id.cardPostCount);
        View cardReport = view.findViewById(R.id.cardReportCount);

        ((TextView) cardUser.findViewById(R.id.tvLabel)).setText("Total Users");
        ((TextView) cardPost.findViewById(R.id.tvLabel)).setText("Total Posts");
        ((TextView) cardReport.findViewById(R.id.tvLabel)).setText("Total Reports");

        adminViewModel.getUserCount().observe(getViewLifecycleOwner(),
                count -> ((TextView) cardUser.findViewById(R.id.tvValue)).setText(String.valueOf(count)));
        adminViewModel.getPostCount().observe(getViewLifecycleOwner(),
                count -> ((TextView) cardPost.findViewById(R.id.tvValue)).setText(String.valueOf(count)));
        adminViewModel.getReportCount().observe(getViewLifecycleOwner(),
                count -> ((TextView) cardReport.findViewById(R.id.tvValue)).setText(String.valueOf(count)));
    }

    class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {
        private List<Report> reports = new ArrayList<>();

        void setReports(List<Report> reports) {
            this.reports = reports;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Report report = reports.get(position);
            holder.tvReason.setText(report.getReason());
            holder.tvTarget.setText("Target ID: " + report.getTargetId());
            holder.tvStatus.setText(report.getStatus());

            holder.btnDismiss.setOnClickListener(v -> {
                adminViewModel.resolveReport(report.getId());
                Toast.makeText(getContext(), "Report Resolved", Toast.LENGTH_SHORT).show();
            });

            holder.btnTakeAction.setOnClickListener(v -> {
                if ("POST".equals(report.getTargetType())) {
                    adminViewModel.deleteTarget("posts", report.getTargetId());
                    adminViewModel.resolveReport(report.getId());
                    Toast.makeText(getContext(), "Post Deleted", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return reports.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvReason, tvTarget, tvStatus;
            Button btnDismiss, btnTakeAction;

            ViewHolder(View itemView) {
                super(itemView);
                tvReason = itemView.findViewById(R.id.tvReason);
                tvTarget = itemView.findViewById(R.id.tvTarget);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                btnDismiss = itemView.findViewById(R.id.btnDismiss);
                btnTakeAction = itemView.findViewById(R.id.btnTakeAction);
            }
        }
    }
}
