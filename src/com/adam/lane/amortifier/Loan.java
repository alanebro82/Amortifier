package com.adam.lane.amortifier;

import java.util.ArrayList;

public class Loan
{
    private final Double PAYMENTS_PER_YEAR = new Double(12);

    private Double mPrincipal;
    private Double mRate;
    private Double mTerm;
    private Double mExtraPayment;

    // amortization table row class
    public class AmortizationMonth
    {
        private final Double mBalance;
        private final Double mTotalPaid;
        private final Double mPrincipalPaid;
        private final Double mInterestPaid;
        private final Double mAdditionalPrincipalPaid;

        // class used for each row of the amortization table
        AmortizationMonth(Double balance, Double principalPaid, Double interestPaid, Double additionalPrincipalPaid)
        {
            mBalance = balance;
            mPrincipalPaid = principalPaid;
            mInterestPaid = interestPaid;
            mAdditionalPrincipalPaid = additionalPrincipalPaid;
            mTotalPaid = mPrincipalPaid + mInterestPaid + mAdditionalPrincipalPaid;
        }

        // getters to access data
        public Double getBalance()
        {
            return mBalance;
        }

        public Double getTotalPaid()
        {
            return mTotalPaid;
        }

        public Double getPrincipalPaid()
        {
            return mPrincipalPaid;
        }

        public Double getInterestPaid()
        {
            return mInterestPaid;
        }

        public Double getAdditionalPrincipalPaid()
        {
            return mAdditionalPrincipalPaid;
        }
    }

    public Loan()
    {
    }

    public Loan(Double principal, Double rate, Double term)
    {
        mPrincipal = principal;
        mRate = rate;
        mTerm = term;
        mExtraPayment = new Double(0);
    }

    public Loan(Double principal, Double rate, Double term, Double extraPayment)
    {
        mPrincipal = principal;
        mRate = rate;
        mTerm = term;
        mExtraPayment = extraPayment;
    }

    public Double getMonthlyPayment()
    {
        if(0 == mRate)
        {
            return mPrincipal / mTerm;
        }
        else if(0 == mPrincipal)
        {
            return new Double(0);
        }
        else if(0 == mTerm)
        {
            return new Double(0);
        }
        else
        {
            Double tempPow = Math.pow((1 + mRate / PAYMENTS_PER_YEAR), mTerm);
            return mPrincipal * (mRate / PAYMENTS_PER_YEAR * tempPow) / (tempPow - 1);
        }
    }

    public ArrayList<AmortizationMonth> getAmortizationTable()
    {
        ArrayList<AmortizationMonth> amortTable = new ArrayList<AmortizationMonth>();

        Double balance = mPrincipal;
        Double monthlyRate = mRate / PAYMENTS_PER_YEAR;
        Double monthlyPayment = getMonthlyPayment();
        Double extraPayment = mExtraPayment;

        while(balance > 0)
        {
            Double interestForMonth = balance * monthlyRate;
            Double principalForMonth = monthlyPayment - interestForMonth;

            // make sure we don't go negative
            if(balance < (principalForMonth + extraPayment))
            {
                if(balance < principalForMonth)
                {
                    principalForMonth = balance;
                    extraPayment = new Double(0);
                }
                else
                {
                    extraPayment = balance - principalForMonth;
                }
            }
            else if(balance < principalForMonth)
            {
                principalForMonth = balance;
                extraPayment = new Double(0);
            }

            balance -= (principalForMonth + extraPayment);

            amortTable.add(new AmortizationMonth(balance, principalForMonth, interestForMonth, extraPayment));
        }

        return amortTable;
    }

    // /Getters and Setters
    public Double getPrincipal()
    {
        return mPrincipal;
    }

    public void setPrincipal(Double principal)
    {
        mPrincipal = principal;
    }

    public Double getRate()
    {
        return mRate;
    }

    public void setRate(Double rate)
    {
        mRate = rate;
    }

    public Double getTerm()
    {
        return mTerm;
    }

    public void setTerm(Double term)
    {
        mTerm = term;
    }
}
