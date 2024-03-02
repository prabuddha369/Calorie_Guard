package com.example.calorieguard;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private final List<DataModel> dataList;
    private final double[] initialCalories; // Array to store initial calories
    private final double[] initialWeights; // Array to store initial calories
    private final String[] initialMacros; // Array to store initial calories

    public RecyclerViewAdapter(List<DataModel> dataList) {
        this.dataList = dataList;
        this.initialCalories = new double[dataList.size()]; // Initialize the array size
        Arrays.fill(initialCalories, -1); // Set initial values to -1
        this.initialMacros = new String[dataList.size()];
        Arrays.fill(initialMacros,":");
        this.initialWeights = new double[dataList.size()]; // Initialize the array size
        Arrays.fill(initialCalories, -1); // Set initial values to -1
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scanned_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataModel data = dataList.get(position);
        holder.Calorie.setText(data.getCalorieScan());
        holder.Macros.setText(data.getMacrosScan());
        holder.Weight.setText(data.getWeightScan());
        holder.FoodName.setText(data.getNameScan());

        // Set a tag to identify the position for the EditText
        holder.Weight.setTag(position);

        // Set the initial calorie and macros value in the array if not already set
        if (initialCalories[position] == -1) {
            initialCalories[position] = Double.parseDouble(data.getCalorieScan());
            initialWeights[position] = Double.parseDouble(data.getWeightScan().replaceAll("g$", "").trim());
            initialMacros[position] = data.getMacrosScan();
        }

        // Set a text change listener on the EditText to track changes
        holder.Weight.addTextChangedListener(new SimpleTextWatcher(holder));

        holder.Weight.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                notifyItemChanged(holder.getAdapterPosition());
                holder.Weight.clearFocus();
                hideSoftKeyboard(holder.Weight);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView Macros;
        TextView Calorie;
        TextView FoodName;
        EditText Weight;
        SimpleTextWatcher textWatcher;

        ViewHolder(View itemView) {
            super(itemView);
            Calorie = itemView.findViewById(R.id.calorie);
            Macros = itemView.findViewById(R.id.macros);
            Weight = itemView.findViewById(R.id.editText_weight);
            FoodName = itemView.findViewById(R.id.food_name);
            textWatcher = new SimpleTextWatcher(this);
        }
    }

    private class SimpleTextWatcher implements TextWatcher {
        private final ViewHolder viewHolder;

        SimpleTextWatcher(ViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            try {
                // Update the DataModel when the text changes
                int adapterPosition = viewHolder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    DataModel data = dataList.get(adapterPosition);
                    String weightString = editable.toString().replaceAll("g$", "").trim();

                    // Calculate new calorie based on the changed weight
                    double weight = Double.parseDouble(weightString);

                    // Calculate new calorie using unitary method
                    int newCalorie = (int) Math.round((weight / 100.0) * initialCalories[adapterPosition]);
                    String updatedMacros=calculateUpdatedNutrition(initialMacros[adapterPosition],weightString);

                    // Update weight
                    data.setWeightScan(weightString + " g");

                    // Update the Calorie value in DataModel
                    data.setCalorieScan(String.valueOf(newCalorie));
                    data.setMacros(updatedMacros);

                    // Update the TextView for Calorie
                    viewHolder.Calorie.setText(String.valueOf(newCalorie));
                    viewHolder.Macros.setText(updatedMacros);
                }

            } catch (NumberFormatException e) {
                // Handle the exception if parsing fails
                e.printStackTrace();
            }
        }
    }

    private void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String calculateUpdatedNutrition(String originalNutritionInfo, String weight) {
        double factor = Double.parseDouble(weight) / 100.0;

        StringBuilder updatedNutritionInfo = new StringBuilder();

        String[] lines = originalNutritionInfo.split("\n");

        for (String line : lines) {
            String[] parts = line.split(":");
            if (parts.length == 2) {
                String nutrient = parts[0].trim();
                if(nutrient.trim().equals("Glycemic Index")){
                    int originalValue = Integer.parseInt(parts[1].replaceAll("g", "").trim());

                    updatedNutritionInfo.append(nutrient)
                            .append(": ")
                            .append(Integer.toString(originalValue))
                            .append(" g\n");
                }
                else {
                    double originalValue = Double.parseDouble(parts[1].replaceAll("g", "").trim());
                    double updatedValue = originalValue * factor;

                    updatedNutritionInfo.append(nutrient)
                            .append(": ")
                            .append(String.format("%.1f", updatedValue))
                            .append(" g\n");
                }
            }
        }

        return updatedNutritionInfo.toString().trim();
    }
}
