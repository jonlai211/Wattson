package com.example.wattson;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LibraryFragment extends Fragment {

    public LibraryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);
        ExpandableListView expandableListView = view.findViewById(R.id.expandableListView);

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(getContext());
        HashMap<String, Set<String>> titlesByPart = dbHelper.getUniqueTitlesByPart();

        List<String> listDataHeader = new ArrayList<>(Arrays.asList("Part1", "Part2", "Part3"));
        HashMap<String, List<String>> listDataChild = new HashMap<>();
        for (String part : listDataHeader) {
            Set<String> titlesSet = titlesByPart.getOrDefault(part, new HashSet<>());
            if (titlesSet != null) {
                listDataChild.put(part, new ArrayList<>(titlesSet));
            } else {
                listDataChild.put(part, new ArrayList<>());
            }
        }

        CustomExpandableListAdapter expandableListAdapter = new CustomExpandableListAdapter(getContext(), listDataHeader, listDataChild);
        expandableListView.setAdapter(expandableListAdapter);

        return view;
    }

}
