package view.backing;

import beanws.EmployeeDto;
import beanws.FileInfoDto;

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

import model.AppModuleImpl;

import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputFile;
import oracle.adf.view.rich.component.rich.nav.RichButton;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.ApplicationModule;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;
import oracle.jbo.client.Configuration;

import org.apache.commons.lang3.StringUtils;
import org.apache.myfaces.trinidad.model.UploadedFile;

public class CsvFileUploadMB {


    private String errorMessage = "";
    private List<EmployeeDto> employeesList = new ArrayList();

    private boolean disableStartUploadButton = true;
    private boolean displayTable = false;
    private boolean disableCommitButton = true;
    private RichTable table;
    private RichButton bSave;
    private FileInfoDto fileInfoDto = new FileInfoDto();
    private RichInputFile richUploadFile;

    private InputStream uploadFileInputStream;
    private List<String> wrongDataList = new ArrayList();
    private final static int NUMBER_OF_COLUMN = 11;


    public void uploadFileListener(ValueChangeEvent valueChangeEvent) throws Exception {
        BufferedReader br = null;
        setEmployeesList(new ArrayList());
        setWrongDataList(new ArrayList());


        // 1 : check file format
        UploadedFile uploadedFile = (UploadedFile) valueChangeEvent.getNewValue();
        if (!uploadedFile.getFilename().endsWith("csv")) {
            showMessage(uploadedFile, FacesMessage.SEVERITY_ERROR, "Check File Format",
                        uploadedFile.getFilename() + " is not CSV file !");
            setDisplayTable(false);
            setEmployeesList(null);
            throw new Exception(uploadedFile.getFilename() + " is not CSV file !");
        }

        try {

            fileInfoDto = buildFileInfo(uploadedFile);
            List<EmployeeDto> dtoList = buildData(br, uploadedFile);
            setEmployeesList(dtoList);
            // Display success message
            // showMessage(uploadedFile, FacesMessage.SEVERITY_INFO, "SUCCESS", "File uploaded with success, you file has :"+employeesList.size() +" records");
            setDisplayTable(true);
            setDisableCommitButton(false);
            //setDisableStartUploadButton(false);
            
            //test
            for(String s : getWrongDataList()){
                System.out.println("wrong data : "+s);
                saveWrongDataInDB(getWrongDataList());
                }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            showMessage(FacesMessage.SEVERITY_ERROR, "ERROR", e.getMessage());
        } catch (IOException e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "ERROR", e.getMessage());
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {

                    br.close();
                } catch (IOException e) {
                    showMessage(FacesMessage.SEVERITY_ERROR, "ERROR", e.getMessage());
                    e.printStackTrace();
                }
            }
        }


    }

    private FileInfoDto buildFileInfo(UploadedFile uploadedFile) {
        FileInfoDto fileInfoDto = new FileInfoDto();
        fileInfoDto.setFileName(uploadedFile.getFilename());
        fileInfoDto.setFileType("CSV");
        fileInfoDto.setFileSize(String.valueOf(uploadedFile.getLength()));
        return fileInfoDto;
    }

    /**
     * Build data from CSV file
     * @param br
     * @param uploadedFile
     * @return
     * @throws IOException
     * @throws Exception
     * @throws ParseException
     */
    private List<EmployeeDto> buildData(BufferedReader br, UploadedFile uploadedFile) throws IOException, Exception,
                                                                                             ParseException {
        List<EmployeeDto> tempList = new ArrayList();
        String line = "";
        String cvsSplitBy = ",";
        int iteration = 0;

        // Upload file
        uploadFileInputStream = uploadedFile.getInputStream();
        // Validate structure file
        br = new BufferedReader(new InputStreamReader(uploadFileInputStream));
        while ((line = br.readLine()) != null) {
            String[] empl = line.split(cvsSplitBy);
            int fieldsNumber = empl.length;
            if (iteration == 0 && checkCsvFileHeaderStructure(uploadedFile, empl)) {
                iteration++;
                continue;
            } //empl!= null && empl.length!=0 && empl[0] != null && !StringUtils.isBlank(empl[0])
            // Array should be not empty
            if(empl!= null && empl.length!=0){
                    if (empl.length == NUMBER_OF_COLUMN) {
                        EmployeeDto newEmployee = createNewEmployee(empl);
                        tempList.add(newEmployee);
                        System.out.println("Employee [ID= " + newEmployee.getEmploeeId() + " , name=" +
                                           newEmployee.getFirstName() + "]");

                    } else {
                        // Add wrong data into errorDataList
                        String wrongString = convertArrayToString(empl);
                        if(wrongString!=null && wrongString.length()!=0){
                                wrongDataList.add(wrongString);
                            }
                       
                    }
                }
           

        }
        return tempList;
    }
/**
     * Convert Array to String 
     * @param array represents one line from CSV file
     * @return
     */
    private String convertArrayToString(String[] array) {
        String result = "";
        if (array != null && array.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (String s : array) {
                // To avoid empty String
                if(!StringUtils.isBlank(s)){
                        sb.append(s).append(",");
                    }
                
            }
            if(!StringUtils.isBlank(sb)){
                    result = sb.deleteCharAt(sb.length() - 1).toString();
                }
        }
        
        return result;
    }
       private ApplicationModule getConfig (){
        String amDef = "model.AppModule";
        String config = "AppModuleLocal";
        ApplicationModule am = Configuration.createRootApplicationModule(amDef, config);
        return am;
        }
    /**
     * Save data in DB
     * @param actionEvent
     */
    public void commit(javax.faces.event.ActionEvent actionEvent) {
        System.out.println("Call save method ...");
        ApplicationModule am = getConfig();
        try {
            AppModuleImpl service = (AppModuleImpl) am;
            ViewObject vo = service.getEmployeesView1();
            List<EmployeeDto> dtoList = (List<EmployeeDto>) getTable().getValue();
            for (EmployeeDto dto : dtoList) {
                System.out.println("dto Email : " + dto.getEmail());
                createEmployeeRow(vo, dto);
            }

            vo.executeQuery();
            service.getTransaction().commit();

            setDisplayTable(false);
            showMessage(FacesMessage.SEVERITY_INFO, "INFO", "File saved successfully");

        } catch (Exception e) {
            showMessage(FacesMessage.SEVERITY_FATAL, "ERROR", e.getMessage());
            System.out.println("ERROR ..." + e.getMessage());
        } finally {
            Configuration.releaseRootApplicationModule(am, true);
        }

    }
    /**
     * Save wrong data  in Data Base (ERROR_DATA table)
     * @param wrongDataList
     */
    private void saveWrongDataInDB(List<String> wrongDataList){
        System.out.println("Call   saveWrongDataInDB ...");
        ApplicationModule am = getConfig();
        try {
            AppModuleImpl service = (AppModuleImpl) am;
            ViewObject vo = service.getDataErrorsView1();
            for (String wrongData : wrongDataList) {
                System.out.println("error data  : " + wrongData);
                Row row = vo.createRow();
                row.setAttribute("Data", wrongData);
                vo.insertRow(row);
            }

            vo.executeQuery();
            service.getTransaction().commit();

            setDisplayTable(false);
            showMessage(FacesMessage.SEVERITY_INFO, "ERROR_DATA INFO", "File saved successfully");

        } catch (Exception e) {
            showMessage(FacesMessage.SEVERITY_FATAL, "ERROR_DATA ERROR", e.getMessage());
            System.out.println("ERROR ..." + e.getMessage());
        } finally {
            Configuration.releaseRootApplicationModule(am, true);
        }
        
                                            
    }

    private void createEmployeeRow(ViewObject vo, EmployeeDto dto) {
        Row row = vo.createRow();
        row.setAttribute("EmployeeId", dto.getEmploeeId());
        row.setAttribute("FirstName", dto.getFirstName());
        row.setAttribute("LastName", dto.getLastName());
        row.setAttribute("Email", dto.getEmail());
        row.setAttribute("PhoneNumber", dto.getPhoneNumber());
        row.setAttribute("HireDate", dto.getHireDate());
        row.setAttribute("JobId", dto.getJobId());
        row.setAttribute("Salary", dto.getSalary());
        row.setAttribute("CommissionPct", dto.getCommissionPct());
        row.setAttribute("ManagerId", dto.getManagerId());
        row.setAttribute("DepartmentId", dto.getDepartmentId());
        vo.insertRow(row);

    }

    /**
     * Check the header of csv file
     * @param employee
     * @return
     * @throws Exception
     */
    private boolean checkCsvFileHeaderStructure(UploadedFile uploadedFile, String[] employee) throws Exception {

        if (employee == null) {
            errorMessage = "Your file is empty @!";
            handleTable();
            return false;
        }

        if (employee[0].equalsIgnoreCase("EmploeeId") && employee[1].equalsIgnoreCase("FirstName") &&
            employee[2].equalsIgnoreCase("LastName") && employee[3].equalsIgnoreCase("Email"))
            return true;
        else {
            errorMessage = "Please check the file structure !";
            showMessage(uploadedFile, FacesMessage.SEVERITY_ERROR, "File structure", errorMessage);
            setEmployeesList(null);
            handleTable();
            throw new Exception(errorMessage);

        }

    }

    /**
     * Create new EmployeeDto
     * @param empl
     * @return EmployeeDto
     * @throws ParseException
     */
    private EmployeeDto createNewEmployee(String[] empl) throws ParseException {
        EmployeeDto employee = new EmployeeDto();
        if (empl[0] != null && !StringUtils.isBlank(empl[0])) {
            employee.setEmploeeId(Integer.parseInt(empl[0]));
        }

        employee.setFirstName(empl[1]);
        employee.setLastName(empl[2]);
        employee.setEmail(empl[3]);
        employee.setPhoneNumber(empl[4]);
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


    private void showMessage(UploadedFile uploadedFile, FacesMessage.Severity severity, String message1,
                             String message2) {
        FacesContext.getCurrentInstance()
            .addMessage(richUploadFile.getClientId(FacesContext.getCurrentInstance()),
                        new FacesMessage(severity, message1, message2));
        richUploadFile.resetValue();
        richUploadFile.setValid(false);
    }

    private void showMessage(FacesMessage.Severity severity, String message1, String message2) {
        FacesContext.getCurrentInstance()
            .addMessage(richUploadFile.getClientId(FacesContext.getCurrentInstance()),
                        new FacesMessage(severity, message1, message2));
        richUploadFile.resetValue();
        richUploadFile.setValid(false);
    }

    private void handleTable() {
        setEmployeesList(new ArrayList());
        setDisplayTable(false);
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

    public void setTable(RichTable table) {
        this.table = table;
        AdfFacesContext facesContext = AdfFacesContext.getCurrentInstance();
        facesContext.addPartialTarget(table);
    }

    public RichTable getTable() {
        return table;
    }

    public void setDisableStartUploadButton(boolean disableStartUploadButton) {
        this.disableStartUploadButton = disableStartUploadButton;
    }

    public boolean isDisableStartUploadButton() {
        return disableStartUploadButton;
    }

    public void setDisplayTable(boolean displayTable) {
        this.displayTable = displayTable;
    }

    public boolean isDisplayTable() {
        return displayTable;
    }

    public void setEmployeesList(List<EmployeeDto> employeesList) {
        this.employeesList = employeesList;
    }

    public List<EmployeeDto> getEmployeesList() {
        return employeesList;
    }

    public void setDisableCommitButton(boolean disableCommitButton) {
        this.disableCommitButton = disableCommitButton;
    }

    public boolean isDisableCommitButton() {
        return disableCommitButton;
    }

    public void setBSave(RichButton bSave) {
        this.bSave = bSave;
    }

    public RichButton getBSave() {
        return bSave;
    }


    public void setFileInfoDto(FileInfoDto fileInfoDto) {
        this.fileInfoDto = fileInfoDto;
    }

    public FileInfoDto getFileInfoDto() {
        return fileInfoDto;
    }

    public void setWrongDataList(List<String> wrongDataList) {
        this.wrongDataList = wrongDataList;
    }

    public List<String> getWrongDataList() {
        return wrongDataList;
    }
}
