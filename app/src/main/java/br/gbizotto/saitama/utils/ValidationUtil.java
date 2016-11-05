package br.gbizotto.saitama.utils;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Gabriela on 23/09/2016.
 */
public class ValidationUtil {

    /**
     * Validate a credit card number using the Luhn Algorithm.
     * Based on https://code.google.com/archive/p/gnuc-credit-card-checker/source and http://howtodoinjava.com/regex/java-regex-validate-credit-card-numbers/.
     *
     * @param ccNumber
     * @return
     */
    public static boolean isValidCreditCard(String ccNumber){
        int sum = 0;
        boolean alternate = false;
        for (int i = ccNumber.length() - 1; i >= 0; i--){
            int n = Integer.parseInt(ccNumber.substring(i, i + 1));
            if (alternate){
                n *= 2;
                if (n > 9){
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    public static boolean isValidCreditCardExpiration(String expirationDate){
        Pattern pattern = Pattern.compile("([0-9]{2})/([0-9]{2})");
        Matcher matcher = pattern.matcher(expirationDate);

        if(TextUtils.isEmpty(expirationDate) || TextUtils.isDigitsOnly(expirationDate) || !matcher.matches()){
            return false;
        }

        String[] expirationDateArray = expirationDate.split("/");
        if(expirationDateArray.length != 2 || !TextUtils.isDigitsOnly(expirationDateArray[0]) || !TextUtils.isDigitsOnly(expirationDateArray[1])){
            return false;
        }

        int month = Integer.valueOf(expirationDateArray[0]);
        if(month<1 || month >12){
            return false;
        }

        return true;
    }
}
