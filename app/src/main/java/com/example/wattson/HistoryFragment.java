package com.example.wattson;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HistoryFragment extends Fragment {
    private ExpandableListView expandableListView;
    private CustomExpandableListAdapter adapter;
    private HashMap<String, List<String>> recordingsByPart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        expandableListView = view.findViewById(R.id.history_expandable_list_view);

        recordingsByPart = getRecordingsGroupedByPart();

        adapter = new CustomExpandableListAdapter(getContext(), new ArrayList<>(recordingsByPart.keySet()), recordingsByPart);
        expandableListView.setAdapter(adapter);

        return view;
    }

    public HashMap<String, List<String>> getRecordingsGroupedByPart() {
        HashMap<String, List<String>> recordingsByPart = new HashMap<>();
        Context context = getContext();
        assert context != null;
        File directory = context.getExternalFilesDir(null);
        if (directory != null && directory.listFiles() != null) {
            for (File file : directory.listFiles()) {
                String fileName = file.getName();
                if (fileName.endsWith(".3gp")) {
                    String[] parts = fileName.split("_");
                    if (parts.length >= 3) {
                        String part = parts[0];
                        // String title = parts[1];

                        if (!recordingsByPart.containsKey(part)) {
                            recordingsByPart.put(part, new ArrayList<>());
                        }
                        recordingsByPart.get(part).add(fileName);
                    }
                }
            }
        }
        return recordingsByPart;
    }

}

