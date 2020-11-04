package com.bl.employeepayrolldb;

import java.sql.Array;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeePayrollDBService {

	private PreparedStatement employeePayrollDataStatement;
	private static EmployeePayrollDBService employeePayrollDBService;

	private EmployeePayrollDBService() {

	}

	public static EmployeePayrollDBService getInstance() {
		if (employeePayrollDBService == null) {
			employeePayrollDBService = new EmployeePayrollDBService();
		}
		return employeePayrollDBService;
	}

	public List<EmployeePayrollData> readData() {
		String sql = "SELECT * FROM employee_payroll_table";
		return this.getEmployeePayrollDataUsingDB(sql);
	}

	private Connection getConnection() throws SQLException {
		String jdbcURL = "jdbc:mysql://localhost:3306/payroll_service?useSSL=false";
		String userName = "root";
		String password = "root";
		Connection con;
		System.out.println("Connecting to database:" + jdbcURL);
		con = DriverManager.getConnection(jdbcURL, userName, password);
		System.out.println("Connection is successful!!!" + con);
		return con;
	}

	public int updateEmployeeData(String name, double salary) {
		return this.updateEmployeeDataUsingStatement(name, salary);
	}

	private int updateEmployeeDataUsingStatement(String name, double salary) {
		String sql = String.format("update employee_payroll_table set salary = %.2f where name = '%s';", salary, name);
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			return statement.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public List<EmployeePayrollData> getEmployeePayrollData(String name) {
		List<EmployeePayrollData> employeePayrollList = null;
		if (this.employeePayrollDataStatement == null) {
			this.prepareStatementForEmployeeData();
		}
		try {
			employeePayrollDataStatement.setString(1, name);
			ResultSet resultSet = employeePayrollDataStatement.executeQuery();
			employeePayrollList = this.getEmployeePayrollData(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	private List<EmployeePayrollData> getEmployeePayrollData(ResultSet resultSet) {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		try {
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				String name = resultSet.getString("name");
				double salary = resultSet.getDouble("salary");
				LocalDate startDate = resultSet.getDate("start").toLocalDate();
				int companyId = resultSet.getInt("company_id");
				String deptName= resultSet.getString("dept_name");
				employeePayrollList.add(new EmployeePayrollData(id, name, salary, startDate, companyId, deptName));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	private void prepareStatementForEmployeeData() {
		try {
			Connection connection = this.getConnection();
			String sql = "SELECT * FROM employee_payroll_table WHERE name = ?";
			employeePayrollDataStatement = connection.prepareStatement(sql);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<EmployeePayrollData> getEmployeePayrollForDateRange(LocalDate startDate, LocalDate endDate) {
		String sql = String.format("SELECT * FROM employee_payroll_table WHERE start BETWEEN '%s' AND '%s';",
				Date.valueOf(startDate), Date.valueOf(endDate));
		return this.getEmployeePayrollDataUsingDB(sql);

	}

	private List<EmployeePayrollData> getEmployeePayrollDataUsingDB(String sql) {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			employeePayrollList = this.getEmployeePayrollData(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	public Map<String, Double> getAverageSalaryByGender() {
		String sql = "SELECT gender, AVG(salary) as avg_salary FROM employee_payroll_table GROUP BY gender;";
		Map<String, Double> genderToAverageSalaryMap = new HashMap<>();
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				String gender = resultSet.getString("gender");
				double salary = resultSet.getDouble("avg_salary");
				genderToAverageSalaryMap.put(gender, salary);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return genderToAverageSalaryMap;
	}

	public EmployeePayrollData addEmployeeToPayrollUC7(String name, double salary, LocalDate startDate, String gender) {
		int employeeId = -1;
		EmployeePayrollData employeePayrolldata = null;
		String sql = String.format(
				"INSERT INTO employee_payroll_table (name, gender, salary, start) VALUES ('%s', '%s', '%s', '%s')",
				name, gender, salary, Date.valueOf(startDate));
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			int rowAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
			if (rowAffected == 1) {
				ResultSet resultSet = statement.getGeneratedKeys();
				if (resultSet.next())
					employeeId = resultSet.getInt(1);
			}
			employeePayrolldata = new EmployeePayrollData(employeeId, name, salary, startDate);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrolldata;
	}

	public EmployeePayrollData addEmployeeToPayroll(String name, double salary, LocalDate startDate, String gender, int companyId, String[] deptArray) {
		int employeeId = -1;
		EmployeePayrollData employeePayrolldata = null;
		Connection connection = null;
		try {
			connection = this.getConnection();
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try (Statement statement = connection.createStatement()) {
			String sql = String.format(
					"INSERT INTO employee_payroll_table (name, gender, salary, start, company_id, dept_name) VALUES ('%s', '%s', '%s', '%s', '%s', '%s')",
					name, gender, salary, Date.valueOf(startDate), companyId, deptArray[0]);
			int rowAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
			if (rowAffected == 1) {
				ResultSet resultSet = statement.getGeneratedKeys();
				if (resultSet.next())
					employeeId = resultSet.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
				return employeePayrolldata;
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}

		try (Statement statement = connection.createStatement()) {
			double deductions = salary * 0.2;
			double taxablePay = salary - deductions;
			double tax = taxablePay * 0.1;
			double netPay = salary - tax;
			String sql = String.format(
					"INSERT INTO payroll_details (employee_id, basic_pay, deductions, taxable_pay, tax, net_pay) VALUES (%s, %s, %s, %s, %s, %s)",
					employeeId, salary, deductions, taxablePay, tax, netPay);
			int rowAffected = statement.executeUpdate(sql);
			if (rowAffected == 1)
				employeePayrolldata = new EmployeePayrollData(employeeId, name, salary, startDate, companyId, deptArray[0]);
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		try {
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return employeePayrolldata;
	}
}
