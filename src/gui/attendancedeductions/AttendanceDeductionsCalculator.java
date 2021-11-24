/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.attendancedeductions;

import datalink.CRUDEmployee;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import model.Attendance;
import model.AttendanceDeduction;
import model.Employee;

/**
 *
 * @author Saleh
 */
public class AttendanceDeductionsCalculator {

    private static List<AttendanceDeduction> attendanceDeductionsList;

    public enum Deduction {

        SINGLE("Single day salary deducted", "\u062E\u0635\u0645 \u0631\u0627\u062A\u0628 \u064A\u0648\u0645"),
        DOUBLE("Two days salary deducted", "\u062E\u0635\u0645 \u0631\u0627\u062A\u0628 \u064A\u0648\u0645\u0627\u0646"),
        DOUBLE_FRIDAY_OMITTED("Two days salary deducted -> Friday omitted", "\u062E\u0635\u0645 \u0631\u0627\u062A\u0628 \u064A\u0648\u0645\u0627\u0646\u061B \u062C\u0645\u0639\u0629 \u0645\u062A\u062E\u0637\u0627\u0629");

        private final String descriptionEN;
        private final String descriptionAR;

        Deduction(String descriptionEN, String descriptionAR) {
            this.descriptionEN = descriptionEN;
            this.descriptionAR = descriptionAR;
        }

        String singleDescriptionEN() {
            return this.descriptionEN;
        }

        String singleDescriptionAR() {
            return this.descriptionAR;
        }
    }

    /**
     * Steps of deductions.
     *
     * First day of month is always single day deduction.
     *
     * subsequent day: - If it occurs within the next 5 days from the previous
     * one; deduct 2 days. - If it occurs after 5 days from the previous one;
     * deduct 1 day.
     *
     * Fridays are always dropped from calculations; in that if a subsequent day
     * occurs as the 7th day from the previous one, and Friday occurs between
     * the previous day and the 7th day then this Friday is dropped such that
     * the subsequent day will be accounted as the 6th day from the previous day
     * instead of being the 7th day.
     *
     * @param listOfAbsentDays
     * @param ym
     * @return List<AttendanceDeduction>
     */
    public static List<AttendanceDeduction> calculateDeductions(List<Attendance> listOfAbsentDays, YearMonth ym) {

        attendanceDeductionsList = new ArrayList<>();

        int size = listOfAbsentDays.size();

        for (int i = 0; i < size; i++) {

            if (size == 1) {
                // If only single day in the list, then apply the deduction and leave the loop.
                setAttendanceDeduction(listOfAbsentDays.get(i), Deduction.SINGLE);
                break;
            }

            if (size > 1 && i == 0) {
                // If more than one day in the list, then record it
                // and continue to check next day if any.
                setAttendanceDeduction(listOfAbsentDays.get(i), Deduction.SINGLE);
            }

            // Check the difference between this day and the next day
            // and apply whatever deduction parameters.
            int day = (int) listOfAbsentDays.get(i).getDate().getDayOfMonth();
            int nextDay = -1;
            int nextDayPosition = i + 1;
            if (nextDayPosition < size) {
                nextDay = (int) listOfAbsentDays.get(i + 1).getDate().getDayOfMonth();
            }
            // If no more days found.
            if (nextDay == -1) {
                break;
            }

            // If Friday occurs between day and nextDay then omit the Friday
            // asume it is 6 days and omit the Friday.
            int diff = nextDay - day;
            int fridayDedected = 0;
            if (diff == 6) {
                // If diff = 6; that means that nextDay is the 7th day from day,
                // hence we check if Friday occurs between day and nextDay
                fridayDedected = dedectFridayBetweenTwoDaysOfMonth(day, nextDay, ym);
            }

            if (((nextDay - fridayDedected) - day) < 6) {
                // If Friday occurred it will be omitted.
                // Then, if diff between nextDay and day is less than 6;
                // it means that nextDay occurs at maximum as the 6th day or less.
                // Hence deduct double days salary.
                if (fridayDedected == 0) {
                    setAttendanceDeduction(listOfAbsentDays.get(nextDayPosition), Deduction.DOUBLE);
                } else {
                    setAttendanceDeduction(listOfAbsentDays.get(nextDayPosition), Deduction.DOUBLE_FRIDAY_OMITTED);
                }
            } else {
                // If diff between nextDay and day is equal or more than 6;
                // it means that nextDay occurs after the 6th day.
                // Hence deduct single days salary.
                setAttendanceDeduction(listOfAbsentDays.get(nextDayPosition), Deduction.SINGLE);
            }
        }

        return attendanceDeductionsList;
    }

    /**
     * Check if Friday exist within 7 days. Works for two days at the same
     * month.
     *
     * @param day
     * @param nextDay
     * @param ym
     * @return
     */
    private static int dedectFridayBetweenTwoDaysOfMonth(int day, int nextDay, YearMonth ym) {

        int fridayDedected = 0;
        int diff = nextDay - day;

        LocalDate friday;
        int startDay = day + 1;
        int endDay = startDay + (diff - 1);
        for (int i = startDay; i < endDay; i++) {
            friday = LocalDate.of(ym.getYear(), ym.getMonth(), i);
            // Friday is 5
            if (friday.get(ChronoField.DAY_OF_WEEK) == 5) {
                fridayDedected = +1;
            }
        }
        return fridayDedected;
    }

    private static void setAttendanceDeduction(Attendance absentRecord, Deduction deduction) {
        AttendanceDeduction attendanceDeduction = new AttendanceDeduction();
        attendanceDeduction.setAttendanceId(absentRecord.getId());
        attendanceDeduction.setDescriptionAR(deduction.singleDescriptionAR());
        attendanceDeduction.setDescriptionEN(deduction.singleDescriptionEN());
        attendanceDeduction.setDate(absentRecord.getDate());
        double daySalary = getDaySalary(absentRecord.getEmployeeId());
        double salaryDeuction = getDeduction(deduction, daySalary);
        attendanceDeduction.setDeduction(rounding(salaryDeuction));
        attendanceDeductionsList.add(attendanceDeduction);
    }

    private static double getDaySalary(int id) {
        Employee employee = CRUDEmployee.getById(id);
        double salary = employee.getSalary();
        double daySalary = salary / 30;
        return daySalary;
    }

    private static double getDeduction(Deduction deduction, double daySalary) {
        double salaryDeduction = 0;
        switch (deduction) {
            case SINGLE:
                salaryDeduction = daySalary;
                break;
            case DOUBLE:
            case DOUBLE_FRIDAY_OMITTED:
                salaryDeduction = daySalary * 2;
                break;
            default:
                salaryDeduction = 0;
                break;
        }
        return salaryDeduction;
    }

    private static double rounding(double salaryDeuction) {
        BigDecimal dec = new BigDecimal(salaryDeuction);
        BigDecimal roundedDecimal = dec.setScale(3, RoundingMode.HALF_UP);
        return roundedDecimal.doubleValue();
    }

}
