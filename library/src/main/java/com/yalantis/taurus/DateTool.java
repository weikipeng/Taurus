package com.yalantis.taurus;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.text.TextUtils;
import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTool {
    private DateTool() {

    }

    public static final int    SECOND_VALUE                  = 1000;
    public static final int    MINUTE_VALUE                  = 60 * SECOND_VALUE;
    public static final String PATTERN_UTC                   = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String PATTERN_DETAIL                = "yyyy-MM-dd HH:mm:ss";
    public static final String PATTERN_MONTH_DAY_HH_MM       = "MM-dd HH:mm";
    public static final String PATTERN_HH_MM                 = "HH:mm";
    public static final String PATTERN_YEAR_MONTH_DAY_2      = "yyyyMMdd";
    public static final String PATTERN_YEAR_MONTH_DAY_3      = "yyyy年MM月dd日";
    public static final String PATTERN_YEAR_MONTH_DAY        = "yyyy-MM-dd";
    public static final String PATTERN_YEAR_MONTH_DAY_HH_MM  = "yyyy-MM-dd HH:mm";
    public static final String PATTERN_MONTH_DAY             = "MM月dd日";
    public static final String PATTERN_MONTH_DAY_2           = "MM-dd";
    public static final String PATTERN_MONTH_DAY_3           = "MM/dd";
    public static final String PATTERN_MONTH_DAY_HOUR        = "MM月dd日HH时";
    public static final String PATTERN_MONTH_CN_DAY_CN_HH_MM = "MM月dd日 HH:mm";
    public static final String PATTERN_HH_MM_CN              = "HH小时mm分钟";
    public static final String PATTERN_ALL_CN_STYLE          = "yyyy年MM月dd日HH时mm分ss秒";

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public static String getRelativeTime(long value) {
        String result   = "";
        long   nowTime  = System.currentTimeMillis();
        long   relavite = nowTime - value;
        if (relavite < MINUTE_VALUE) {
            result = relavite / SECOND_VALUE + "秒前";
            return result;
        }

        result = String.valueOf(DateUtils.getRelativeTimeSpanString(value, nowTime, 0L, DateUtils.FORMAT_ABBREV_ALL));

        return result;
    }

    public static String getTimeText(String pattern, long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());

        return dateFormat.format(new Date(time));
    }

    public static Date parseUtcTime(String time) {
        Date date = null;
        try {
            if (TextUtils.isEmpty(time)) {
                date = new Date();
            } else {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_UTC);
                date = sdf.parse(time);
            }

            return date;
        } catch (Exception e) {
            e.printStackTrace();
            date = new Date();
        }

        return date;
    }


    public static String getYearMonthDay(long milliseconds) {
        Date date = new Date(milliseconds);
        return getYearMonthDay(date);
    }

    public static String getYearMonthDay(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(PATTERN_YEAR_MONTH_DAY, Locale.getDefault());

        return dateFormat.format(date);
    }

    public static String getYearMonthDay2(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(PATTERN_YEAR_MONTH_DAY_2, Locale.getDefault());
        return dateFormat.format(date);
    }

    public static String getCurrentMonthDay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(PATTERN_MONTH_DAY, Locale.getDefault());
        return dateFormat.format(new Date());
    }

    public static String getDetailTimeText(long time) {
        SimpleDateFormat format = new SimpleDateFormat(PATTERN_DETAIL, Locale.getDefault());
        return format.format(new Date(time));
    }

    public static Date parseDetailTime(String timeText) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(PATTERN_DETAIL, Locale.getDefault());
        return format.parse(timeText);
    }

    public static Date parseTime(String pattern, String timeText) {
        //        PATTERN_YEAR_MONTH_DAY
        Date date = null;
        try {
            if (TextUtils.isEmpty(timeText)) {
                date = new Date();
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                date = sdf.parse(timeText);
            }

            return date;
        } catch (Exception e) {
            e.printStackTrace();
            date = new Date();
        }

        return date;
    }

    public static String getMonthDayHourUTC(String utcTime) {
        Date             date       = parseUtcTime(utcTime);
        SimpleDateFormat dateFormat = new SimpleDateFormat(PATTERN_MONTH_DAY_HOUR, Locale.getDefault());
        return dateFormat.format(date);
    }

    public static String getMonthDayHour(String time) throws ParseException {
        Date             date       = parseDetailTime(time);
        SimpleDateFormat dateFormat = new SimpleDateFormat(PATTERN_MONTH_DAY_HOUR, Locale.getDefault());
        return dateFormat.format(date);
    }

    public static String getMonthCNDayCNHourMinute(String time) throws ParseException {
        Date             date       = parseDetailTime(time);
        SimpleDateFormat dateFormat = new SimpleDateFormat(PATTERN_MONTH_CN_DAY_CN_HH_MM, Locale.getDefault());
        return dateFormat.format(date);
    }

    public static String getMonthCNDayCNHourMinute(long time) throws ParseException {
        Date             date       = new Date(time);
        SimpleDateFormat dateFormat = new SimpleDateFormat(PATTERN_MONTH_CN_DAY_CN_HH_MM, Locale.getDefault());
        return dateFormat.format(date);
    }

    public static String getYearMonthDayHHMM(long time) {
        SimpleDateFormat format = new SimpleDateFormat(PATTERN_YEAR_MONTH_DAY_HH_MM, Locale.getDefault());
        return format.format(new Date(time));
    }

    public static String getMonthDayHHmmText(long time) {
        SimpleDateFormat format = new SimpleDateFormat(PATTERN_MONTH_DAY_HH_MM, Locale.getDefault());
        return format.format(new Date(time));
    }

    public static String getAllChinaStyle(long time) {
        SimpleDateFormat format = new SimpleDateFormat(PATTERN_ALL_CN_STYLE, Locale.getDefault());
        return format.format(new Date(time));
    }

    public static int getCurrentDayInYear() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_YEAR);
    }

    public static String dateFormat(long milliseconds) {
        String           timeText;
        long             now         = System.currentTimeMillis();
        long             timePass    = now / 1000 - milliseconds / 1000;
        SimpleDateFormat format      = new SimpleDateFormat(PATTERN_YEAR_MONTH_DAY);
        SimpleDateFormat hourFormat  = new SimpleDateFormat(PATTERN_HH_MM);
        SimpleDateFormat dateFormat  = new SimpleDateFormat(PATTERN_MONTH_DAY_HH_MM);
        String           nowFormat   = format.format(new Date(now));
        String           stampFormat = format.format(new Date(milliseconds));
        if (timePass <= 60) {// 一分钟内
            timeText = "刚刚";
        } else if (timePass < 600) {
            timeText = "十分钟内";
        } else if (timePass < 3600) {
            timeText = "一小时内";
        } else if (timePass < 86400 && stampFormat.equals(nowFormat)) {
            timeText = "今天" + hourFormat.format(new Date(milliseconds));
        } else {
            timeText = dateFormat.format(new Date(milliseconds));
        }
        return timeText;
    }

    public static String dateFormat4TopNewsList(long seconds) {
        String           timeText;
        long             now        = System.currentTimeMillis();
        long             timePass   = now / 1000 - seconds;
        SimpleDateFormat dateFormat = new SimpleDateFormat(PATTERN_MONTH_DAY_2);
        if (timePass < 3600) {//一小时内
            timeText = "刚刚";
        } else if (isCurrentDay(seconds)) {//一天内
            int hourAgo = (int) (timePass / 3600);
            timeText = hourAgo + "小时前";
        } else if (isYesterday(seconds)) {//昨天
            timeText = "昨天";
        } else {
            timeText = dateFormat.format(new Date(seconds * 1000));
        }
        return timeText;
    }

    public static String getHHMMCN(long timeMillis) {
        StringBuilder stringBuilder = new StringBuilder();

        long durationMinutes = timeMillis / (60 * 1000);

        stringBuilder.append(durationMinutes / 60);
        stringBuilder.append("小时");
        stringBuilder.append(durationMinutes % 60);
        stringBuilder.append("分钟");

        return stringBuilder.toString();
        //        SimpleDateFormat dateFormat = new SimpleDateFormat(PATTERN_HH_MM_CN);
        //        return dateFormat.format(new Date(timeMillis));
    }

    public static long getTimeYearMonthDayHHMM(String yearMonthDayHourMinute) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat(PATTERN_YEAR_MONTH_DAY_HH_MM);
        Date date = null;
        try {
            date = dateFormat.parse(yearMonthDayHourMinute);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date == null ? 0 : date.getTime();
    }

    /**
     * 将时间转化为utc时间 2016-08-29 16:57 ===>
     */
    public static String parseTimeToUTCTime(String localTime) {
        long time = getTimeYearMonthDayHHMM(localTime);
        //1、取得本地时间：
        Calendar cal = Calendar.getInstance();

        cal.setTimeInMillis(time);

        //2、取得时间偏移量：
        int zoneOffset = cal.get(Calendar.ZONE_OFFSET);

        //3、取得夏令时差：
        int dstOffset = cal.get(Calendar.DST_OFFSET);

        //4、从本地时间里扣除这些差量，即可以取得UTC时间：
        cal.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));

        //之后调用cal.get(int x)或cal.getTimeInMillis()方法所取得的时间即是UTC标准时间。
        //        System.out.println("UTC:" + new Date(cal.getTimeInMillis()));

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat utcDateFormat = new SimpleDateFormat(PATTERN_UTC);
        return utcDateFormat.format(new Date(cal.getTimeInMillis()));
    }

    /**
     * 将时间转化为utc时间 2016-08-29 16:57 ===>
     */
    public static String parseTimeToUTCTime(long localTime) {
        //1、取得本地时间：
        Calendar cal = Calendar.getInstance();

        cal.setTimeInMillis(localTime);

        //2、取得时间偏移量：
        int zoneOffset = cal.get(Calendar.ZONE_OFFSET);

        //3、取得夏令时差：
        int dstOffset = cal.get(Calendar.DST_OFFSET);

        //4、从本地时间里扣除这些差量，即可以取得UTC时间：
        cal.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));

        //之后调用cal.get(int x)或cal.getTimeInMillis()方法所取得的时间即是UTC标准时间。
        //        System.out.println("UTC:" + new Date(cal.getTimeInMillis()));

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat utcDateFormat = new SimpleDateFormat(PATTERN_UTC);
        return utcDateFormat.format(new Date(cal.getTimeInMillis()));
    }

    public static boolean isCurrentYear(long seconds) {
        Calendar calendar    = Calendar.getInstance();
        int      currentYear = calendar.get(Calendar.YEAR);
        calendar.setTimeInMillis(seconds * 1000);
        int compareDateYear = calendar.get(Calendar.YEAR);
        return compareDateYear == currentYear;
    }

    public static boolean isCurrentDay(long seconds) {
        Calendar calendar     = Calendar.getInstance();
        int      currentYear  = calendar.get(Calendar.YEAR);
        int      currentMonth = calendar.get(Calendar.MONTH) + 1;
        int      currentDay   = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.setTimeInMillis(seconds * 1000);
        int compareDateYear  = calendar.get(Calendar.YEAR);
        int compareDateMonth = calendar.get(Calendar.MONTH) + 1;
        int compareDateDay   = calendar.get(Calendar.DAY_OF_MONTH);
        return compareDateYear == currentYear && compareDateMonth == currentMonth && compareDateDay == currentDay;
    }

    public static boolean isYesterday(long seconds) {
        Calendar calendar     = Calendar.getInstance();
        int      currentYear  = calendar.get(Calendar.YEAR);
        int      currentMonth = calendar.get(Calendar.MONTH) + 1;
        int      currentDay   = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.setTimeInMillis(seconds * 1000);
        int compareDateYear  = calendar.get(Calendar.YEAR);
        int compareDateMonth = calendar.get(Calendar.MONTH) + 1;
        int compareDateDay   = calendar.get(Calendar.DAY_OF_MONTH);
        return compareDateYear == currentYear && compareDateMonth == currentMonth && compareDateDay == currentDay - 1;
    }

    public static String getHourMinute(long milliseconds) {
        Date             date       = new Date(milliseconds);
        SimpleDateFormat dateFormat = new SimpleDateFormat(PATTERN_HH_MM, Locale.getDefault());
        return dateFormat.format(date);
    }

    public static String getAgeCorrect2Day(int countYear, int countMonth, int countDay) {
        Calendar nowCalendar   = Calendar.getInstance();
        Calendar countCalendar = Calendar.getInstance();
        countCalendar.set(countYear, countMonth - 1, countDay, 0, 0, 0);
        if (nowCalendar.compareTo(countCalendar) < 0) {
            return "日期错误";
        }
        int day   = nowCalendar.get(Calendar.DAY_OF_MONTH) - countCalendar.get(Calendar.DAY_OF_MONTH) + 1;
        int month = nowCalendar.get(Calendar.MONTH) - countCalendar.get(Calendar.MONTH);
        int year  = nowCalendar.get(Calendar.YEAR) - countCalendar.get(Calendar.YEAR);
        //day不够向month借
        if (day < 0) {
            month--;
            //上个月
            nowCalendar.add(Calendar.MONTH, -1);
            day += nowCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        }
        //month不够向year借
        if (month < 0) {
            year--;
            month += 12;
        }
        String resultText = "";
        if (year > 0) {
            resultText = year + "岁";
        }
        if (month > 0) {
            resultText = resultText + month + "个月";
        } else if (year > 0) {
            resultText += "零";
        }
        if (day == 0) {
            day++;
        }
        resultText = resultText + day + "天";
        return resultText;
    }

    public static int getLifeDayCount(int year, int month, int day) {
        Calendar nowCalendar    = Calendar.getInstance();
        long     nowMillSeconds = nowCalendar.getTimeInMillis();
        Calendar countCalendar  = Calendar.getInstance();
        countCalendar.set(year, month - 1, day, 0, 0, 0);
        long   countMillSeconds  = countCalendar.getTimeInMillis();
        double offsetMillSeconds = nowMillSeconds - countMillSeconds;
        return (int) Math.ceil(offsetMillSeconds / (1000 * 3600 * 24));
    }

    public static String getZodiac(int month, int date) {
        String[] zodiac = {"摩羯座", "水瓶座", "双鱼座", "白羊座", "金牛座",
                "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座"};
        //黄道十二宫两者间分割日
        int[] partition = {20, 19, 21, 21, 21, 22, 23, 23, 23, 23, 22, 22};
        int   index     = month;
        //所查询日期在分割日之前，索引-1，否则不变
        if (date < partition[month - 1]) {
            index -= 1;
        }
        return zodiac[index];
    }

    public static String getCurrentMonthDayType3() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(PATTERN_MONTH_DAY_3, Locale.getDefault());
        return dateFormat.format(new Date());
    }

}
