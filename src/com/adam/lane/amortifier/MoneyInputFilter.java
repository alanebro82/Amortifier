package com.adam.lane.amortifier;

import android.text.InputFilter;
import android.text.Spanned;

public class MoneyInputFilter implements InputFilter
{
    static final int DECIMAL_DIGITS = 2;

    /**
     * Constructor.
     * 
     * @param decimalDigits
     *            maximum decimal digits
     */
    public MoneyInputFilter()
    {
    }

    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend)
    {
        int dotPos = -1;
        int len = dest.length();
        for(int i = 0; i < len; i++)
        {
            char c = dest.charAt(i);
            if(c == '.' || c == ',')
            {
                dotPos = i;
                break;
            }
        }
        if(dotPos > 0)
        {
            // if the text is entered before the dot
            if(dend <= dotPos)
            {
                return null;
            }
            if(len - dotPos > DECIMAL_DIGITS)
            {
                return "";
            }
        }

        return null;
    }

}
