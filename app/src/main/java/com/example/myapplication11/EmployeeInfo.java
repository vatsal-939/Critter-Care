package com.example.myapplication11;

public class EmployeeInfo {
    private String employeeName;
    private String employeeContactNumber;
    private String employeeDesignation;

    // Default constructor required for calls to DataSnapshot.getValue(EmployeeInfo.class)
    public EmployeeInfo() {
    }

    public EmployeeInfo(String employeeName, String employeeContactNumber, String employeeDesignation) {
        this.employeeName = employeeName;
        this.employeeContactNumber = employeeContactNumber;
        this.employeeDesignation = employeeDesignation;
    }

    // Getters and setters

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeContactNumber() {
        return employeeContactNumber;
    }

    public void setEmployeeContactNumber(String employeeContactNumber) {
        this.employeeContactNumber = employeeContactNumber;
    }

    public String getEmployeeDesignation() {
        return employeeDesignation;
    }

    public void setEmployeeDesignation(String employeeDesignation) {
        this.employeeDesignation = employeeDesignation;
    }
}