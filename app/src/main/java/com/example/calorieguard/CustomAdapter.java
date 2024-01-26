package com.example.calorieguard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<String> {
    private boolean isDeleteMode = false;
    private ArrayList<String> selectedItems = new ArrayList<>();

    public CustomAdapter(@NonNull Context context, int resource, ArrayList<String> arrayList) {
        super(context, resource, arrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_layout, parent, false);
        }

        TextView textViewItem = itemView.findViewById(R.id.textViewItem);
        CheckBox checkBox = itemView.findViewById(R.id.checkBox);

        String currentItem = getItem(position);

        if (currentItem != null) {
            textViewItem.setText(currentItem);
        }

        // Set CheckBox visibility based on delete mode
        if (isDeleteMode) {
            checkBox.setVisibility(View.VISIBLE);
            // Set CheckBox state based on selectedItems
            checkBox.setChecked(selectedItems.contains(currentItem));

            // Set OnClickListener to handle item selection
            checkBox.setOnClickListener(v -> {
                CheckBox cb = (CheckBox) v;
                if (cb.isChecked()) {
                    selectedItems.add(currentItem);
                } else {
                    selectedItems.remove(currentItem);
                }
            });
        } else {
            checkBox.setVisibility(View.GONE);
        }

        return itemView;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    public void setDeleteMode(boolean deleteMode) {
        isDeleteMode = deleteMode;
        selectedItems.clear(); // Clear selected items when entering/exiting delete mode
        notifyDataSetChanged();
    }

    public ArrayList<String> getSelectedItems() {
        return selectedItems;
    }
}

