package com.example.calorieguard;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

public class NumberRangeAdapter extends ArrayAdapter<String> implements Filterable {
    private List<String> dataList;
    private List<String> filteredList;

    public NumberRangeAdapter(Context context) {
        super(context, android.R.layout.simple_dropdown_item_1line);
        dataList = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            dataList.add(String.valueOf(i) + " g");
        }
        filteredList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Override
    public String getItem(int position) {
        return filteredList.get(position);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                filteredList.clear();

                if (constraint != null) {
                    for (String item : dataList) {
                        if (item.startsWith(constraint.toString())) {
                            filteredList.add(item);
                        }
                    }
                }

                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }
}