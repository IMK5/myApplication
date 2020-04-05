package view.backing;

import beanws.EmployeeDto;
import beanws.FileInfoDto;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.text.ParseException;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import model.EmployeesDraftImpl;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputFile;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.layout.RichPanelBox;
import oracle.adf.view.rich.component.rich.layout.RichPanelFormLayout;
import oracle.adf.view.rich.component.rich.nav.RichButton;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.binding.BindingContainer;
import oracle.binding.OperationBinding;

import oracle.jbo.Row;

import org.apache.myfaces.trinidad.model.UploadedFile;

public class CsvFileUploadMB {


    private String errorMessage = "";
    private List<EmployeeDto> employeesList = new ArrayList();
    private RichPopup popup;
    private boolean disableStartUploadButton = true;
    private boolean displayTable = false;
    private boolean disableCommitButton = true;
    private boolean displayStructureDataLink = false;
    private RichTable table;
    private RichButton bSave;
    private FileInfoDto fileInfoDto = new FileInfoDto();
    private RichTable errorDataTable;
    private RichTable draftTable;
    private RichPanelFormLayout panelForm;
    private RichInputFile richUploadFile;
    private RichInputText emploeeIdInput;


    private InputStream uploadFileInputStream;
    private List<String> wrongDataList = new ArrayList();
    private final static int NUMBER_OF_COLUMN = 11;
    private EmployeeDto empDto = new EmployeeDto();

    private Services services = new Services();
    private RichPanelBox panelTable;
    


    /**
     * Called when click on upload button
     * @param valueChangeEvent
     * @throws Exception
     */
    public void uploadFileListener(ValueChangeEvent valueChangeEvent) throws Exception {
        BufferedReader br = null;
        setEmployeesList(new ArrayList());
        setWrongDataList(new ArrayList());


        // 1 : check file format
        UploadedFile uploadedFile = (UploadedFile) valueChangeEvent.getNewValue();
        if (!services.isCsvFile(uploadedFile)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Check File Format",
                        uploadedFile.getFilename() + " is not CSV file !");
            setDisplayTable(false);
            setEmployeesList(null);
            throw new Exception(uploadedFile.getFilename() + " is not CSV file !");
        }

        try {

            fileInfoDto = services.buildFileInfo(uploadedFile);
            List<EmployeeDto> dtoList = buildData(br, uploadedFile);
            setEmployeesList(dtoList);
         
            getFileInfoDto().setErrorRecordsNumber(getWrongDataList().size());
            getFileInfoDto().setRightDataNumber(dtoList.size());

            // Save data in DB
            services.saveEmployees_Draft_AND_Error_Data(getEmployeesList(), getWrongDataList());

            //  Refresh table;
            services.refreshEmpDraftTable("EmployeesDraftView1Iterator");
            if(getWrongDataList().size()>0){
                    setDisplayStructureDataLink(true);
                }

            setDisplayTable(true);
            AdfFacesContext.getCurrentInstance().addPartialTarget(getPanelForm());
            
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
    /**
     * Save EmployeesDraft list in DB
     * @return String
     */
    public String bSave_action() {
         
        System.out.println("Call  bSave_action ...");
        BindingContainer bindings = getBindings();
        OperationBinding operationBinding = bindings.getOperationBinding("Commit");
        Object result = operationBinding.execute();
        if (!operationBinding.getErrors().isEmpty()) {
            return null;
        }
        setDisplayTable(false);
        showPopup();
        return null;
    }
    
    /**
     * Validate data and submit them to DB
     * @return
     */
    public String submit_action() {
        System.out.println("Calla  submit_action ...");
        // Get data from table 
        DCBindingContainer dcBindings = 
                (DCBindingContainer)BindingContext.getCurrent().getCurrentBindingsEntry();
        DCIteratorBinding iterBind= (DCIteratorBinding)dcBindings.get("EmployeesDraftView1Iterator"); 
        Row[] rows = iterBind.getAllRowsInRange();  
        Row r= rows[1];
        r.getStructureDef();
       
        for (Row row : rows) {  
         String email= (String)row.getAttribute("Email");
            System.out.println("email ..."+email);
            
        }   
        // Call validation data 
        
        // Submit date to DB
        
        showPopup();
        return null;
    }


    /**
     * Build data from CSV file
     * @param br BufferedReader
     * @param uploadedFile UploadedFile
     * @return  List<EmployeeDto>
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
            if (iteration == 0 && checkCsvFileHeaderStructure(uploadedFile, empl)) {
                iteration++;
                continue;
            }
            // Check structure file
            if (empl != null && empl.length != 0) {
                if (empl.length == NUMBER_OF_COLUMN) {
                    EmployeeDto newEmployee = services.createNewEmployee(empl);
                    tempList.add(newEmployee);
                    System.out.println("Employee [ID= " + newEmployee.getEmploeeId() + " , name=" +
                                       newEmployee.getFirstName() + "]");

                } else {
                    // Add wrong data into errorDataList
                    String wrongString = services.convertArrayToString(empl);
                    if (wrongString != null && wrongString.length() != 0) {
                        wrongDataList.add(wrongString);
                    }

                }
            }


        }
        return tempList;
    }
    
 /*
    public String saveDraftEmployees() {
        System.out.println("Call  saveDraftEmployees...");
        try {
            // List<EmployeeDto> dtoList = (List<EmployeeDto>) getTable().getValue();
            services.saveEmployees_Draft_List(getEmployeesList());
            setDisplayTable(false);
            if (getFileInfoDto().getErrorRecordsNumber() > 0) {
                System.out.println("go to updateStructureFile ...");
                showMessage(FacesMessage.SEVERITY_INFO, "Update data structure !",
                            "You have ( " + getFileInfoDto().getErrorRecordsNumber() + " rows) to be updated");
                return "updateDataStructure";
            } else {
                showMessage(FacesMessage.SEVERITY_INFO, "INFO", "File saved successfully");
            }

        } catch (Exception e) {
            showMessage(FacesMessage.SEVERITY_FATAL, "ERROR", e.getMessage());
            System.out.println("ERROR ..." + e.getMessage());

        }

        return null;
    }*/

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
        // Check all fields...
        if (employee[0].equalsIgnoreCase("EmploeeId") && employee[1].equalsIgnoreCase("FirstName") &&
            employee[2].equalsIgnoreCase("LastName") && employee[3].equalsIgnoreCase("Email"))
            return true;
        else {
            errorMessage = "Please check file structure !";
            showMessage(FacesMessage.SEVERITY_ERROR, "File structure", errorMessage);
            setEmployeesList(null);
            handleTable();
            throw new Exception(errorMessage);

        }

    }

/*
    private void showMessage(UploadedFile uploadedFile, FacesMessage.Severity severity, String message1,
                             String message2) {
        FacesContext.getCurrentInstance()
            .addMessage(richUploadFile.getClientId(FacesContext.getCurrentInstance()),
                        new FacesMessage(severity, message1, message2));
        richUploadFile.resetValue();
        richUploadFile.setValid(false);
    }
*/
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

/**
     * Get inputValue by component Id from UI
     * @param id
     * @return
     */
    public String getInputValueById(String id) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        UIViewRoot root = facesContext.getViewRoot();
        RichInputText inputText = (RichInputText) root.findComponent(id);
        String val = inputText.getValue().toString();
        return val;
    }
    
    
    public void showPopup() {
        RichPopup.PopupHints hints = new RichPopup.PopupHints();
        this.getPopup().setAutoDismissalTimeout(3);
        this.getPopup().show(hints);
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

    public void setServices(Services services) {
        this.services = services;
    }

    public Services getServices() {
        return services;
    }

    public void setEmpDto(EmployeeDto empDto) {
        this.empDto = empDto;
    }

    public EmployeeDto getEmpDto() {
        return empDto;
    }

    public void setEmploeeIdInput(RichInputText emploeeIdInput) {
        this.emploeeIdInput = emploeeIdInput;
    }

    public RichInputText getEmploeeIdInput() {
        return emploeeIdInput;
    }


    public void setErrorDataTable(RichTable errorDataTable) {
        this.errorDataTable = errorDataTable;
    }

    public RichTable getErrorDataTable() {
        return errorDataTable;
    }

    public void setPanelForm(RichPanelFormLayout panelForm) {
        this.panelForm = panelForm;
    }

    public RichPanelFormLayout getPanelForm() {
        return panelForm;
    }

 
    public void setPanelTable(RichPanelBox panelTable) {
        this.panelTable = panelTable;
    }

    public RichPanelBox getPanelTable() {
        return panelTable;
    }

    public BindingContainer getBindings() {
        return BindingContext.getCurrent().getCurrentBindingsEntry();
    }


    public void setDisplayStructureDataLink(boolean displayStructureDataLink) {
        this.displayStructureDataLink = displayStructureDataLink;
    }

    public boolean isDisplayStructureDataLink() {
        return displayStructureDataLink;
    }

    public void setDraftTable(RichTable draftTable) {
        this.draftTable = draftTable;
    }

    public RichTable getDraftTable() {
        return draftTable;
    }


    public void setPopup(RichPopup popup) {
        this.popup = popup;
    }

    public RichPopup getPopup() {
        return popup;
    }
}
