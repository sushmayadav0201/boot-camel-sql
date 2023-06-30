package com.assign01.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.sql.SqlComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.assign01.model.Employee;

/**
 * Created service layer for the project.
 */
@Service
public class EmployeeServiceImpl extends RouteBuilder {

	@Autowired
	DataSource dataSource;

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Bean
	public SqlComponent sql(DataSource dataSource) {
		SqlComponent sql = new SqlComponent();
		sql.setDataSource(dataSource);
		return sql;
	}

	@Override
	public void configure() throws Exception {

		from("direct:select").to("sql:select * from employee").process(new Processor() {
			public void process(Exchange xchg) throws Exception {
				ArrayList<Map<String, String>> dataList = (ArrayList<Map<String, String>>) xchg.getIn().getBody();
				List<Employee> employees = new ArrayList<Employee>();
				System.out.println(dataList);
				for (Map<String, String> data : dataList) {
					Employee employee = new Employee();
					employee.setEmpId(data.get("empId"));
					employee.setEmpName(data.get("empName"));
					employees.add(employee);
				}
				xchg.getIn().setBody(employees);
			}
		});

		from("direct:insert").log("Processing message: ${body}").setHeader("message", body()).process(new Processor() {
			public void process(Exchange xchg) throws Exception {
				Employee employee = xchg.getIn().getBody(Employee.class);
				Map<String, Object> answer = new HashMap<String, Object>();
				answer.put("EmpId", employee.getEmpId());
				answer.put("EmpName", employee.getEmpName());
				xchg.getIn().setBody(answer);
			}
		}).to("sql:INSERT INTO employee(EmpId, EmpName) VALUES (:#EmpId, :#EmpName)");

	}
}
