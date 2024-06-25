package com.example.demo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RentalService {
    private Map<String, Tool> tools;

    public RentalService() {
        tools = new HashMap<>();
        tools.put("CHNS", new Tool("CHNS", "Chainsaw", "Stihl", 1.49, true, false, true));
        tools.put("LADW", new Tool("LADW", "Ladder", "Werner", 1.99, true, true, false));
        tools.put("JAKD", new Tool("JAKD", "Jackhammer", "DeWalt", 2.99, true, false, false));
        tools.put("JAKR", new Tool("JAKR", "Jackhammer", "Ridgid", 2.99, true, false, false));
    }

    public RentalAgreement checkout(String toolCode, int rentalDays, int discountPercent, String checkoutDateStr) throws Exception {
        if (rentalDays < 1) {
            throw new IllegalArgumentException("Rental day count must be 1 or greater.");
        }
        if (discountPercent < 0 || discountPercent > 100) {
            throw new IllegalArgumentException("Discount percent must be between 0 and 100.");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
        Date checkoutDate = sdf.parse(checkoutDateStr);

        Tool tool = tools.get(toolCode);
        if (tool == null) {
            throw new IllegalArgumentException("Invalid tool code.");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(checkoutDate);
        calendar.add(Calendar.DAY_OF_YEAR, rentalDays);
        Date dueDate = calendar.getTime();

        int chargeDays = calculateChargeDays(tool, checkoutDate, dueDate);
        double preDiscountCharge = chargeDays * tool.getDailyCharge();
        double discountAmount = preDiscountCharge * discountPercent / 100;
        double finalCharge = preDiscountCharge - discountAmount;

        return new RentalAgreement(tool, rentalDays, checkoutDate, discountPercent, dueDate, chargeDays, preDiscountCharge, discountAmount, finalCharge);
    }

    private int calculateChargeDays(Tool tool, Date startDate, Date endDate) {
        int chargeDays = 0;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        while (calendar.getTime().before(endDate)) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            boolean isWeekend = (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) || (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
            boolean isHoliday = isHoliday(calendar);

            if (tool.isWeekdayCharge() && !isWeekend && !isHoliday) {
                chargeDays++;
            }
            if (tool.isWeekendCharge() && isWeekend && !isHoliday) {
                chargeDays++;
            }
            if (tool.isHolidayCharge() && isHoliday) {
                chargeDays++;
            }
        }
        return chargeDays;
    }

    private boolean isHoliday(Calendar calendar) {
        // Check Independence Day
        if (calendar.get(Calendar.MONTH) == Calendar.JULY && (calendar.get(Calendar.DAY_OF_MONTH) == 4 || (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY && calendar.get(Calendar.DAY_OF_MONTH) == 3) || (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && calendar.get(Calendar.DAY_OF_MONTH) == 5))) {
            return true;
        }

        // Check Labor Day (first Monday in September)
        if (calendar.get(Calendar.MONTH) == Calendar.SEPTEMBER && calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && calendar.get(Calendar.DAY_OF_MONTH) <= 7) {
            return true;
        }

        return false;
    }
}