package com.adam.lane.amortifier;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.adam.lane.amortifier.Loan.AmortizationMonth;

public class AmortizationTableBaseAdapter extends BaseAdapter
{
    private static ArrayList<AmortizationMonth> mAmortArrayList;

    private LayoutInflater mInflater;

    public AmortizationTableBaseAdapter(Context context, ArrayList<AmortizationMonth> results)
    {
        mAmortArrayList = results;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount()
    {
        return mAmortArrayList.size();
    }

    public Object getItem(int position)
    {
        return mAmortArrayList.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;
        if(convertView == null)
        {
            convertView = mInflater.inflate(R.layout.amortization_row, null);
            holder = new ViewHolder();
            holder.month = (TextView) convertView.findViewById(R.id.monthLabel);
            holder.totalPaid = (TextView) convertView.findViewById(R.id.totalPaidLabel);
            holder.principalPaid = (TextView) convertView.findViewById(R.id.principalPaidLabel);
            holder.interestPaid = (TextView) convertView.findViewById(R.id.interestPaidLabel);
            holder.remainingBalance = (TextView) convertView.findViewById(R.id.remainingBalanceLabel);

            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        // format string
        Locale currentLocale = Locale.getDefault();
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(currentLocale);

        holder.month.setText(Integer.toString(position + 1));
        holder.totalPaid.setText(currencyFormatter.format(mAmortArrayList.get(position).getTotalPaid()));
        holder.principalPaid.setText(currencyFormatter.format(mAmortArrayList.get(position).getPrincipalPaid()
                + mAmortArrayList.get(position).getAdditionalPrincipalPaid()));
        holder.interestPaid.setText(currencyFormatter.format(mAmortArrayList.get(position).getInterestPaid()));
        holder.remainingBalance.setText(currencyFormatter.format(mAmortArrayList.get(position).getBalance()));

        return convertView;
    }

    static class ViewHolder
    {
        TextView month;
        TextView totalPaid;
        TextView principalPaid;
        TextView interestPaid;
        TextView remainingBalance;
    }
}
