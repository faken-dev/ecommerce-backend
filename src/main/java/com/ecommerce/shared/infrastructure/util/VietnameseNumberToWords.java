package com.ecommerce.shared.infrastructure.util;

import java.math.BigDecimal;

/**
 * Utility to convert numbers to Vietnamese words.
 */
public class VietnameseNumberToWords {

    private static final String[] DIGITS = {"không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"};
    private static final String[] UNITS = {"", "ngàn", "triệu", "tỷ", "ngàn tỷ", "triệu tỷ"};

    public static String convert(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) return "Không đồng";
        
        long number = amount.longValue();
        if (number < 0) return "âm " + convert(amount.abs());
        
        String res = "";
        int unitIndex = 0;
        
        do {
            long temp = number % 1000;
            if (temp > 0) {
                String groupStr = convertGroup((int) temp);
                res = groupStr + " " + UNITS[unitIndex] + " " + res;
            }
            unitIndex++;
            number /= 1000;
        } while (number > 0);

        res = res.trim();
        if (res.isEmpty()) return "Không đồng";
        
        // Capitalize first letter
        res = res.substring(0, 1).toUpperCase() + res.substring(1) + " đồng chẵn.";
        return res.replaceAll("\\s+", " ");
    }

    private static String convertGroup(int n) {
        int h = n / 100;
        int t = (n % 100) / 10;
        int u = n % 10;
        
        String res = DIGITS[h] + " trăm";
        
        if (t == 0) {
            if (u == 0) return res;
            res += " lẻ " + DIGITS[u];
        } else if (t == 1) {
            res += " mười";
            if (u == 1) res += " một"; 
            else if (u == 5) res += " lăm";
            else if (u > 0) res += " " + DIGITS[u];
        } else {
            res += " " + DIGITS[t] + " mươi";
            if (u == 1) res += " mốt";
            else if (u == 5) res += " lăm";
            else if (u > 0) res += " " + DIGITS[u];
        }
        
        return res;
    }
}
