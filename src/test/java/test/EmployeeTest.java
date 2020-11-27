package test;

import com.google.gson.Gson;
import dto.*;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class EmployeeTest {

    private RequestSpecification requestSpecification;

    @BeforeClass
    public void createRequestSpecification() {
        requestSpecification = new RequestSpecBuilder()
                .setBaseUri("http://dummy.restapiexample.com/api/v1")
                .build();
    }

    @DataProvider(name = "existingEmployees")
    public Object[][] existingEmployees() {
        EmployeeData employee1Data = EmployeeData.builder()
                .id(1)
                .employeeName("Tiger Nixon")
                .employeeSalary(320800)
                .employeeAge(61)
                .profileImage("")
                .build();
        EmployeeData employee2Data = EmployeeData.builder()
                .id(2)
                .employeeName("Garrett Winters")
                .employeeSalary(170750)
                .employeeAge(63)
                .profileImage("")
                .build();
        EmployeeData employee3Data = EmployeeData.builder()
                .id(3)
                .employeeName("Ashton Cox")
                .employeeSalary(86000)
                .employeeAge(66)
                .profileImage("")
                .build();
        return new Object[][]{
                {employee1Data},
                {employee2Data},
                {employee3Data},
        };
    }

    @Test(dataProvider = "existingEmployees")
    public void checkGetEmployeesByIds(EmployeeData employeeData) {
        Employee employee = getEmployee(employeeData.getId());

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(employee.getData().getEmployeeName()).isEqualTo(employeeData.getEmployeeName());
        softly.assertThat(employee.getData().getEmployeeSalary()).isEqualTo(employeeData.getEmployeeSalary());
        softly.assertThat(employee.getData().getEmployeeAge()).isEqualTo(employeeData.getEmployeeAge());
        softly.assertThat(employee.getData().getProfileImage()).isEqualTo(employeeData.getProfileImage());
        softly.assertAll();
    }

    @Test
    public void checkForEmployeeWithNotExistingId() {
        int notExistingId = 25;
        Employee employee = getEmployee(notExistingId);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(employee.getData()).isNull();
        softly.assertThat(employee.getStatus()).isEqualTo("failure");
        softly.assertThat(employee.getMessage()).isEqualTo("Failure! There is no such employee.");
        softly.assertAll();
    }

    @Test
    public void testCreateNewEmployee() {
        NewEmployeeData newEmployeeData = NewEmployeeData.builder()
                .name("Adam")
                .salary(5000)
                .age(40)
                .build();

        NewEmployee employeeToBeCreated = postEmployee(newEmployeeData);

        int newEmployeeId = employeeToBeCreated.getData().getId();

        Employee createdEmployee = getEmployee(newEmployeeId);

        assertThat(createdEmployee.getData()).as("check if new employee has not null data").isNotNull();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(employeeToBeCreated.getMessage()).isEqualTo("Successfully! Record has been added.");
        softly.assertThat(createdEmployee.getData().getId()).isEqualTo(employeeToBeCreated.getData().getId());
        softly.assertThat(createdEmployee.getData().getEmployeeName()).isEqualTo(employeeToBeCreated.getData().getName());
        softly.assertThat(createdEmployee.getData().getEmployeeAge()).isEqualTo(employeeToBeCreated.getData().getAge());
        softly.assertThat(createdEmployee.getData().getEmployeeSalary()).isEqualTo(employeeToBeCreated.getData().getSalary());
        softly.assertAll();
    }

    private Employee getEmployee(int id) {
        Response employeeByIdResponse =
                given()
                        .spec(requestSpecification)
                        .pathParams("id", id)
                .when()
                        .get("/employee/{id}");

        assertThat(employeeByIdResponse.statusCode()).as("check response status code").isEqualTo(200);

        return employeeByIdResponse.as(Employee.class);
    }

    private NewEmployee postEmployee(NewEmployeeData newEmployeeData) {
        Response newEmployeeResponse =
                given()
                        .spec(requestSpecification)
                        .contentType("application/json")
                        .body(new Gson().toJson(newEmployeeData))
                .when()
                        .post("/create");

        assertThat(newEmployeeResponse.statusCode()).as("check response status code").isEqualTo(200);

        return newEmployeeResponse.as(NewEmployee.class);
    }
}
