package com.example.wattson;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

public class HistoryFragment extends Fragment {
    private ExpandableListView expandableListView;
    private CustomExpandableListAdapter adapter;
    private LinkedHashMap<String, List<String>> recordingsByPart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        expandableListView = view.findViewById(R.id.history_expandable_list_view);

        recordingsByPart = getRecordingsGroupedByPart();

        adapter = new CustomExpandableListAdapter(getContext(), new ArrayList<>(recordingsByPart.keySet()), recordingsByPart);
        expandableListView.setAdapter(adapter);

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String selectedRecording = recordingsByPart.get(adapter.getGroup(groupPosition)).get(childPosition);
                File directory = getContext().getExternalFilesDir(null);
                if (directory != null) {
                    String filePath = new File(directory, selectedRecording).getAbsolutePath();
                    Log.d("HistoryFragment", "Recording file path: " + filePath);
                    Intent intent = new Intent(getContext(), RecordingPlaybackActivity.class);
                    intent.putExtra("RECORDING_FILE_NAME", filePath);
                    startActivity(intent);
                }
                return true;

            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecordings();
    }

    private void loadRecordings() {
        recordingsByPart = getRecordingsGroupedByPart();
        List<String> listDataHeader = new ArrayList<>(Arrays.asList("Part1", "Part2", "Part3")); // Static headers to ensure order
        HashMap<String, List<String>> listDataChild = new LinkedHashMap<>(); // Use LinkedHashMap to preserve the order

        for (String part : listDataHeader) {
            if (recordingsByPart.containsKey(part)) {
                listDataChild.put(part, recordingsByPart.get(part));
            }
        }

        adapter = new CustomExpandableListAdapter(getContext(), listDataHeader, listDataChild);
        expandableListView.setAdapter(adapter);
    }

    private LinkedHashMap<String, List<String>> getRecordingsGroupedByPart() {
        Context context = getContext();
        assert context != null;
        File directory = context.getExternalFilesDir(null);
        LinkedHashMap<String, List<String>> recordingsByPart = new LinkedHashMap<>();
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
        for (List<String> recordings : recordingsByPart.values()) {
            recordings.sort(Collections.reverseOrder());
        }

        return recordingsByPart;
    }

}

