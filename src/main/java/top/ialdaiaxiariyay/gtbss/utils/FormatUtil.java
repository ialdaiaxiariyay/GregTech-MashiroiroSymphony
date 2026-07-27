package top.ialdaiaxiariyay.gtbss.utils;

import java.math.BigDecimal;
import java.math.BigInteger;

import static com.gregtechceu.gtceu.utils.FormattingUtil.DECIMAL_FORMAT_SIC_2F;
import static com.gregtechceu.gtceu.utils.FormattingUtil.formatNumberReadable;

public class FormatUtil {

    public static String formatBigDecimalNumberOrSic(BigDecimal number) {
        return number.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) > 0 ? DECIMAL_FORMAT_SIC_2F.format(number) :
                formatNumberReadable(number.longValue());
    }

    public static String formatBigIntegerNumberOrSic(BigInteger number) {
        return number.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0 ? DECIMAL_FORMAT_SIC_2F.format(number) :
                formatNumberReadable(number.longValue());
    }
}
