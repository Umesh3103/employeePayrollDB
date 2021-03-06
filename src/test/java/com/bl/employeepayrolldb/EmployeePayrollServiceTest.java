package com.bl.employeepayrolldb;

import java.sql.Array;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.bl.employeepayrolldb.EmployeePayrollService.IOService;

public class EmployeePayrollServiceTest {

	@Test
	public void Given3Employee_WhenWrittenToFile_ShouldMatchEmployeeEntries() {
		EmployeePayrollData[] arrayOfEmps = { new EmployeePayrollData(1, "Jeff Bezos", 100000.0),
				new EmployeePayrollData(2, "Bill Gates", 200000.0),
				new EmployeePayrollData(3, "Mark Zuckerberg", 300000.0) };
		EmployeePayrollService employeePayrollService;
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmps));
		employeePayrollService.writeEmployeePayrollData(IOService.FILE_IO);
		employeePayrollService.printData(IOService.FILE_IO);
		long entries = employeePayrollService.countEntries(IOService.FILE_IO);
		Assert.assertEquals(3, entries);
	}

	@Test
	public void givenEmployeePayrollInDB_WhenRetrieved_ShouldMatchEmployeeCount() throws employeePayrollException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<EmployeePayrollData> employeePayrollData = employeePayrollService
				.readEmployeePayrollDataDB(IOService.DB_IO);
		Assert.assertEquals(5, employeePayrollData.size());
	}

	@Test
	public void givenNewSalaryForEmployee_WhenUpdated_ShouldSyncWithDB() throws employeePayrollException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<EmployeePayrollData> employeePayrollData = employeePayrollService
				.readEmployeePayrollDataDB(IOService.DB_IO);
		employeePayrollService.updateEmployeeSalary("Terisa", 3000000.00);
		boolean result = employeePayrollService.checkEmployeePayrollInSyncWithDB("Terisa");
		Assert.assertTrue(result);
	}

	@Test
	public void givenDateRange_WhenRetrieved_ShouldMatchEmployeeCount() throws employeePayrollException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readEmployeePayrollDataDB(IOService.DB_IO);
		LocalDate startDate = LocalDate.of(2018, 01, 01);
		LocalDate endDate = LocalDate.now();
		List<EmployeePayrollData> employeePayrollData = employeePayrollService
				.readEmployeePayrollForDateRange(IOService.DB_IO, startDate, endDate);
		Assert.assertEquals(5, employeePayrollData.size());
	}

	@Test
	public void givenPayrollData_WhenAverageSalaryRetrievedByGender_ShouldReturnProperValue() throws employeePayrollException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readEmployeePayrollDataDB(IOService.DB_IO);
		Map<String, Double> averageSalaryByGender = employeePayrollService.readAverageSalaryByGender(IOService.DB_IO);
		Assert.assertTrue(
				averageSalaryByGender.get("M").equals(3000000.00) && averageSalaryByGender.get("F").equals(3000000.00));
	}

	@Test
	public void givenNewEmployee_WhenAdded_ShouldSyncWithDB() throws employeePayrollException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readEmployeePayrollDataDB(IOService.DB_IO);
		String[] deptArray = new String[4];
		deptArray[0] = "sales";
		deptArray[1] = "hr";
		deptArray[2] = "marketing";
		deptArray[3] = "hr,sales";
		employeePayrollService.addEmployeeToPayroll("Mark", 5000000.00, LocalDate.now(), "M", 2, deptArray);
		boolean result = employeePayrollService.checkEmployeePayrollInSyncWithDB("Mark");
		Assert.assertTrue(result);
	}
	
	@Test
	public void givenNewEmployee_WhenAdded_ShouldSyncWithNewDB() throws employeePayrollException {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readEmployeePayrollDataDB(IOService.DB_IO);
		String[] deptArray = new String[4];
		deptArray[0] = "sales";
		deptArray[1] = "hr";
		deptArray[2] = "marketing";
		deptArray[3] = "hr,sales";
		employeePayrollService.addEmployeeToPayroll("Radha", 3000000.00, LocalDate.now(), "F", 1, deptArray);
		boolean result = employeePayrollService.checkEmployeePayrollInSyncWithDB("Radha");
		Assert.assertTrue(result);
	}
	
	@Test
	public void givenEmployee_WhenRemoved_ShouldGetRemoved() throws employeePayrollException{
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readEmployeePayrollDataDB(IOService.DB_IO);
		int result = employeePayrollService.removeEmployeeFromPayroll("Mark",0);
		System.out.println(result);
		Assert.assertEquals(4,result);
	}
}
