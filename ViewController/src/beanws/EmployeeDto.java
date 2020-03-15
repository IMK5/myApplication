package beanws;

import java.util.Date;


/**
 * Used to when uploading CSV file
 */
public class EmployeeDto  {
     

    public EmployeeDto() {
        super();
    }
    private Integer emploeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Date hireDate;
    private String jobId;
    private Long salary;
    private Integer commissionPct;
    private Integer managerId;
    private Integer departmentId;
    
    
    public void setEmploeeId(Integer emploeeId) {
        this.emploeeId = emploeeId;
    }

    public Integer getEmploeeId() {
        return emploeeId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

     

    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }

    public Date getHireDate() {
        return hireDate;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setSalary(Long salary) {
        this.salary = salary;
    }

    public Long getSalary() {
        return salary;
    }

    public void setCommissionPct(Integer commissionPct) {
        this.commissionPct = commissionPct;
    }

    public Integer getCommissionPct() {
        return commissionPct;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
