package view.backing;

import beanws.EmployeeDto;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import oracle.adf.view.rich.component.rich.RichDocument;
import oracle.adf.view.rich.component.rich.RichForm;
import oracle.adf.view.rich.component.rich.fragment.RichPageTemplate;
import oracle.adf.view.rich.component.rich.input.RichInputFile;
import oracle.adf.view.rich.component.rich.layout.RichPanelFormLayout;
import oracle.adf.view.rich.component.rich.output.RichMessages;

import org.apache.commons.lang3.StringUtils;
import org.apache.myfaces.trinidad.model.UploadedFile;

public class CsvFileUploadMB {
    private RichPageTemplate pt1;
    private RichForm f1;
    private RichDocument d1;
    private RichMessages richMessages;
    private RichPanelFormLayout pfl1;
    private String errorMessage = "";
    private List<EmployeeDto> employeesList = null;

    private RichInputFile richUploadFile;

    private InputStream uploadFileInputStream;

    //private List bulkEmployeeList;


    public void setPt1(RichPageTemplate pt1) {
        this.pt1 = pt1;
    }

    public RichPageTemplate getPt1() {
        return pt1;
    }

    public void setF1(RichForm f1) {
        this.f1 = f1;
    }

    public RichForm getF1() {
        return f1;
    }

    public void setD1(RichDocument d1) {
        this.d1 = d1;
    }

    public RichDocument getD1() {
        return d1;
    }


    public void setM1(RichMessages m1) {
        this.richMessages = m1;
    }

    public RichMessages getM1() {
        return richMessages;
    }


    public void uploadFileListener(ValueChangeEvent valueChangeEvent) throws Exception {
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        int iteration = 0;
        List<EmployeeDto> employeesList = new ArrayList();
        // 1 : check file format
        UploadedFile uploadedFile = (UploadedFile) valueChangeEvent.getNewValue();
        if (!uploadedFile.getFilename().endsWith("csv")) {
            showMessage(uploadedFile, FacesMessage.SEVERITY_ERROR, "Check File Format",
                        uploadedFile.getFilename() + " is not CSV file !");
            throw new Exception (uploadedFile.getFilename() + " is not CSV file !");
        }

        try {
            // Upload file
            uploadFileInputStream = uploadedFile.getInputStream();
            // Validate structure file
            br = new BufferedReader(new InputStreamReader(uploadFileInputStream));

            while ((line = br.readLine()) != null) {
                String[] empl = line.split(cvsSplitBy);
                if (iteration == 0 && checkCsvFileStructure(uploadedFile, empl)) {
                    iteration++;
                    continue;
                }
                if(empl[0]!= null && !StringUtils.isBlank(empl[0]) ){
                        System.out.println("Employee: " + empl[0] + " , " + empl[1] + " , " + empl[2] + " , " + empl[3]);
                        EmployeeDto newEmployee = createNewEmployee(empl);
                        employeesList.add(newEmployee);

                        System.out.println("Employee [ID= " + newEmployee.getEmploeeId() + " , name=" +
                                           newEmployee.getFirstName() + "]");

                    }
               
            }
            // Display success message
            showMessage(uploadedFile, FacesMessage.SEVERITY_INFO, "SUCCESS", "File uploaded with success, you file has :"+employeesList.size() +" records");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    /**
     * Check the head of csv file
     * @param employee
     * @return
     * @throws Exception
     */
    private boolean checkCsvFileStructure(UploadedFile uploadedFile, String[] employee) throws Exception {

        if (employee == null) {
            errorMessage = "Your file is empty @!";
            return false;
        }

        if (employee[0].equalsIgnoreCase("EmploeeId") && employee[1].equalsIgnoreCase("FirstName") &&
            employee[2].equalsIgnoreCase("LastName") && employee[3].equalsIgnoreCase("Email"))
            return true;
        else {
            errorMessage = "Please check the file structure !";
            showMessage(uploadedFile, FacesMessage.SEVERITY_ERROR, "File structure", errorMessage);
            throw new Exception(errorMessage);

        }

    }

    private EmployeeDto createNewEmployee(String[] empl) throws ParseException {
        EmployeeDto employee = new EmployeeDto();
        if (empl[0] != null && !StringUtils.isBlank(empl[0])) {
            employee.setEmploeeId(Integer.parseInt(empl[0]));
        }

        employee.setFirstName(empl[1]);
        employee.setLastName(empl[2]);
        employee.setEmail(empl[3]);
        employee.setTelephoneNumber(empl[4]);
        if (empl[5] != null && !StringUtils.isBlank(empl[5])) {
            Date date = StringToDate(empl[5]);
            employee.setHireDate(date);
        }

        employee.setJobId(empl[6]);
        if (empl[7] != null && !StringUtils.isBlank(empl[7])) {
            employee.setSalary(Long.parseLong(empl[7]));
        }

        if (empl[8] != null && !StringUtils.isBlank(empl[8])) {
            employee.setCommissionPct(Integer.parseInt(empl[8]));
        }
        if (empl[9] != null && !StringUtils.isBlank(empl[9])) {
            employee.setManagerId(Integer.parseInt(empl[9]));
        }
        if (empl[10] != null && !StringUtils.isBlank(empl[10])) {
            employee.setDepartmentId(Integer.parseInt(empl[10]));
        }


        return employee;
    }

    private Date StringToDate(String dob) throws ParseException {
        //Instantiating the SimpleDateFormat class
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yy");
        //Parsing the given String to Date object
        Date date = formatter.parse(dob);
        return date;
    }

    public void setPfl1(RichPanelFormLayout pfl1) {
        this.pfl1 = pfl1;
    }

    public RichPanelFormLayout getPfl1() {
        return pfl1;
    }


    public void setUploadFileInputStream(InputStream uploadFileInputStream) {
        this.uploadFileInputStream = uploadFileInputStream;
    }

    public InputStream getUploadFileInputStream() {
        return uploadFileInputStream;
    }

    public void setRichUploadFile(RichInputFile richUploadFile) {
        this.richUploadFile = richUploadFile;
    }

    public RichInputFile getRichUploadFile() {
        return richUploadFile;
    }


    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setEmployeesList(List<EmployeeDto> employeesList) {
        this.employeesList = employeesList;
    }

    public List<EmployeeDto> getEmployeesList() {
        return employeesList;
    }

    private void showMessage(UploadedFile uploadedFile, FacesMessage.Severity severity, String message1,
                             String message2) {
        FacesContext.getCurrentInstance()
            .addMessage(richUploadFile.getClientId(FacesContext.getCurrentInstance()),
                        new FacesMessage(severity, message1, message2));
        richUploadFile.resetValue();
        richUploadFile.setValid(false);
    }
}
