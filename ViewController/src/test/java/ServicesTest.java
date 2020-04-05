package test.java;

import beanws.EmployeeDto;

import java.text.ParseException;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import view.backing.Services;


public class ServicesTest {

    private Services service = new Services();

    @Before
    public void setUp() throws Exception {
        //Method annotated with `@Before` will execute before each test method in this class is executed.

        //If you find that several tests need similar objects created before they can run this method could be used to do set up those objects (aka test-fixtures).
    }

    @Test
    public void createNewEmployeeTest() throws ParseException, Exception {
        String[] data = {
            "288874", "Bob", "King", "BOB@SKI.COM", "515.123.4567", "17-Jun-03", "AD_PRES", "24000", "", "", "90"
        };
        EmployeeDto dto = service.createNewEmployee(data);
        Boolean result = Util.haveSamePropertyValues(EmployeeDto.class, dto, buildEmployeeDto());
        assertEquals(result, Boolean.TRUE);


    }

    @Test
    public void convertArrayToStringTest() {
        String[] array = { "", "Luis", "", "LPOPP" };
        String result = service.convertArrayToString(array);
        assertEquals(result, "Luis,LPOPP");

    }

    @Test
    public void convertArrayToStringWhenGivingEmptyArrayTest() {
        String[] array = { "", "", "", "" };
        String result = service.convertArrayToString(array);
        assertEquals(result, "");

    }

    @Test
    public void convertStringToDateTest() throws ParseException {
        String sDate = "17-JUN-03";
        Date date = service.convertStringToDate(sDate);
        assertEquals(date.toString(), "Tue Jun 17 00:00:00 AST 2003");
    }

    @Test(expected = Exception.class)
    public void convertStringToDateThrowsExceptionTest() throws ParseException {
        String sDate = "17-05-03";
        Date date = service.convertStringToDate(sDate);
    }

    private EmployeeDto buildEmployeeDto() throws ParseException {
        EmployeeDto dto = new EmployeeDto();
        dto.setEmploeeId(288874);
        dto.setFirstName("Bob");
        dto.setLastName("King");
        dto.setEmail("BOB@SKI.COM");
        dto.setPhoneNumber("515.123.4567");
        Date date = service.convertStringToDate("17-Jun-03");
        dto.setHireDate(date);
        dto.setJobId("AD_PRES");
        dto.setSalary(24000L);
        dto.setCommissionPct(null);
        dto.setManagerId(null);
        dto.setDepartmentId(90);

        return dto;

    }
}
