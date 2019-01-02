package com.tiza.support.util;

import org.apache.commons.lang.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Description: DateUtil
 * Author: DIYILIU
 * Update: 2016-03-21 16:03
 */
public class DateUtil {

    public static Date stringToDate(String datetime) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date = null;
        if (StringUtils.isNotEmpty(datetime)) {
            try {
                date = format.parse(datetime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return date;
    }

    public static Date stringToDate(String datetime, String format) {
        DateFormat df = new SimpleDateFormat(format);

        Date date = null;
        if (StringUtils.isNotEmpty(datetime)) {
            try {
                date = df.parse(datetime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return date;
    }


    public static String dateToString(Date date) {

        if (date == null) {

            return null;
        }

        return String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", date);
    }

    public static String dateToString(Date date, String format) {

        if (date == null) {

            return null;
        }

        return String.format(format, date);
    }

    public static Long getTimeMillis(String time) throws Exception {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return df.parse(time).getTime();
    }

    /**
     * 日历转换成数字
     *
     * @param cal
     * @return yyyyMM
     */
    public static int getYM(Calendar cal) {
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH) + 1;
        return y * 100 + m;
    }

    /**
     * 日历转换成数字
     *
     * @param cal
     * @return yyyyMMdd
     */
    public static int getYMD(Calendar cal) {
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH) + 1;
        int d = cal.get(Calendar.DAY_OF_MONTH);
        return y * 10000 + m * 100 + d;
    }
}
