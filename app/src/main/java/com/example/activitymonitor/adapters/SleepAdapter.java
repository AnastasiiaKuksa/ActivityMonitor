package com.example.activitymonitor.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.activitymonitor.R;
import com.example.activitymonitor.models.SleepRecord;
import com.example.activitymonitor.utils.DateUtils;

import java.util.List;

public class SleepAdapter extends RecyclerView.Adapter<SleepAdapter.SleepViewHolder> {

    private List<SleepRecord> sleepRecords;
    private OnSleepItemClickListener listener;

    public SleepAdapter(List<SleepRecord> sleepRecords) {
        this.sleepRecords = sleepRecords;
    }

    public void updateData(List<SleepRecord> newData) {
        this.sleepRecords = newData;
        notifyDataSetChanged();
    }

    public void setOnSleepItemClickListener(OnSleepItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SleepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sleep, parent, false);
        return new SleepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SleepViewHolder holder, int position) {
        SleepRecord sleepRecord = sleepRecords.get(position);
        holder.bind(sleepRecord);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSleepItemClick(sleepRecord);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sleepRecords != null ? sleepRecords.size() : 0;
    }

    static class SleepViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDate;
        private TextView tvDuration;
        private TextView tvQuality;
        private TextView tvEfficiency;
        private TextView tvSessionCount;

        public SleepViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvQuality = itemView.findViewById(R.id.tvQuality);
            tvEfficiency = itemView.findViewById(R.id.tvEfficiency);
            tvSessionCount = itemView.findViewById(R.id.tvSessionCount);
        }

        public void bind(SleepRecord sleepRecord) {
            // Дата
            tvDate.setText(DateUtils.formatDateForDisplay(sleepRecord.getDate()));

            // Тривалість
            String durationText = String.format("%.1f год", sleepRecord.getTotalDuration() / 60.0);
            tvDuration.setText(durationText);

            // Якість сну
            int quality = (int) Math.round(sleepRecord.getAverageQuality());
            String qualityText = getQualityText(quality);
            int qualityColor = getQualityColor(quality);
            int qualityBackground = getQualityBackground(quality);

            tvQuality.setText(qualityText);
            tvQuality.setTextColor(ContextCompat.getColor(itemView.getContext(), qualityColor));
            tvQuality.setBackgroundResource(qualityBackground);

            // Ефективність
            String efficiencyText = String.format("%.0f%%", sleepRecord.getSleepEfficiency());
            tvEfficiency.setText(efficiencyText);

            // Кількість сесій
            int sessionCount = sleepRecord.getSleepSessions().size();
            String sessionText = sessionCount + " сесі" + getSessionEnding(sessionCount);
            tvSessionCount.setText(sessionText);

            // Колір ефективності
            setEfficiencyColor(sleepRecord.getSleepEfficiency());
        }

        private String getQualityText(int quality) {
            switch (quality) {
                case 1: return "Погано";
                case 2: return "Задовільно";
                case 3: return "Добре";
                case 4: return "Відмінно";
                default: return "Немає даних";
            }
        }

        private int getQualityColor(int quality) {
            switch (quality) {
                case 1: return R.color.error;
                case 2: return R.color.warning;
                case 3: return R.color.success;
                case 4: return R.color.info;
                default: return R.color.text_secondary;
            }
        }

        private int getQualityBackground(int quality) {
            switch (quality) {
                case 1: return R.drawable.pill_background_poor;
                case 2: return R.drawable.pill_background_fair;
                case 3: return R.drawable.pill_background_good;
                case 4: return R.drawable.pill_background_excellent;
                default: return R.drawable.pill_background_good;
            }
        }

        private String getSessionEnding(int count) {
            if (count % 10 == 1 && count % 100 != 11) {
                return "я";
            } else if (count % 10 >= 2 && count % 10 <= 4 && (count % 100 < 10 || count % 100 >= 20)) {
                return "ї";
            } else {
                return "й";
            }
        }

        private void setEfficiencyColor(double efficiency) {
            int colorRes;
            if (efficiency >= 85) {
                colorRes = R.color.success;
            } else if (efficiency >= 70) {
                colorRes = R.color.warning;
            } else {
                colorRes = R.color.error;
            }
            tvEfficiency.setTextColor(ContextCompat.getColor(itemView.getContext(), colorRes));
        }
    }

    public interface OnSleepItemClickListener {
        void onSleepItemClick(SleepRecord sleepRecord);
    }
}