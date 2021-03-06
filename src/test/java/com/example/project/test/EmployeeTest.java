package com.example.project.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.project.dto.*;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class EmployeeTest {

    private RequestSpecification requestSpecification;
    private ResponseSpecification responseSpecification;
    private final ObjectMapper objectMapper;

    public EmployeeTest() {
        objectMapper = new ObjectMapper();
    }

    @BeforeClass
    public void createRequestAndResponseSpecification() {
        requestSpecification = new RequestSpecBuilder()
                .setBaseUri("http://dummy.restapiexample.com/api/v1")
                .build();
        responseSpecification = new ResponseSpecBuilder()
                .expectStatusCode(200)
                .build();
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    @DataProvider(name = "existingEmployees")
    public Object[][] existingEmployees() {
        return new Object[][]{
                {EmployeeData.builder()
                        .id(1)
                        .employeeName("Tiger Nixon")
                        .employeeSalary(320800)
                        .employeeAge(61)
                        .profileImage(StringUtils.EMPTY)
                        .build()},
                {EmployeeData.builder()
                        .id(2)
                        .employeeName("Garrett Winters")
                        .employeeSalary(170750)
                        .employeeAge(63)
                        .profileImage(StringUtils.EMPTY)
                        .build()},
                {EmployeeData.builder()
                        .id(3)
                        .employeeName("Ashton Cox")
                        .employeeSalary(86000)
                        .employeeAge(66)
                        .profileImage(StringUtils.EMPTY)
                        .build()},
        };
    }

    @Test(dataProvider = "existingEmployees")
    public void checkGetEmployeesByIds(EmployeeData employeeData) {
        val employee = getEmployee(employeeData.getId());

        assertThat(employee.getData()).isEqualTo(employeeData);
    }

    @Test
    public void checkForEmployeeWithNotExistingId() {
        val notExistingId = 25;
        val employee = getEmployee(notExistingId);

        val softly = new SoftAssertions();
        softly.assertThat(employee.getData()).isNull();
        softly.assertThat(employee.getStatus()).isEqualTo("failure");
        softly.assertThat(employee.getMessage()).isEqualTo("Failure! There is no such employee.");
        softly.assertAll();
    }

    @Test
    public void testCreateNewEmployee() throws JsonProcessingException {
        val newEmployeeData = NewEmployeeData.builder()
                .name("Adam")
                .salary(5000)
                .age(40)
                .build();

        val employeeToBeCreated = postEmployee(newEmployeeData);

        val newEmployeeId = employeeToBeCreated.getData().getId();

        val createdEmployee = getEmployee(newEmployeeId);

        assertThat(createdEmployee.getData()).as("check if new employee has not null data").isNotNull();

        val softly = new SoftAssertions();
        softly.assertThat(createdEmployee.getMessage()).isEqualTo("Successfully! Record has been added.");
        softly.assertThat(areEmployeesEqual(createdEmployee,employeeToBeCreated)).isTrue();
        softly.assertAll();
    }

    private boolean areEmployeesEqual(Employee employee1, NewEmployee employee2){
        return employee1.getData().getEmployeeName().equals(employee2.getData().getName())
                && employee1.getData().getEmployeeAge() == employee2.getData().getAge()
                && employee1.getData().getEmployeeSalary() == employee2.getData().getSalary()
                && employee1.getData().getId() == employee2.getData().getId();
    }

    private Employee getEmployee(int id) {
        return given()
                    .spec(requestSpecification)
                    .pathParams("id", id)
                .expect()
                    .spec(responseSpecification)
                .when()
                    .get("/employee/{id}")
                .as(Employee.class);
    }

    private NewEmployee postEmployee(NewEmployeeData newEmployeeData) throws JsonProcessingException {
        return given()
                    .spec(requestSpecification)
                    .contentType("application/json")
                    .body(objectMapper.writeValueAsString(newEmployeeData))
                .expect()
                    .spec(responseSpecification)
                .when()
                    .post("/create")
                .as(NewEmployee.class);
    }
}
