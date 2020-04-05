package view.backing;

import beanws.EmployeeDto;
import beanws.FileInfoDto;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;

import model.AppModuleImpl;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.share.logging.ADFLogger;

import oracle.jbo.ApplicationModule;
import oracle.jbo.Row;
import oracle.jbo.ViewObject;
import oracle.jbo.client.Configuration;

import org.apache.commons.lang3.StringUtils;
import org.apache.myfaces.trinidad.model.UploadedFile;

public class Services {

    private ADFLogger logger = ADFLogger.createADFLogger(this.getClass());
    /**
     * Saves Employees list in DB
     * @param dtoList
     */
    public void saveEmployeesList(List<EmployeeDto> dtoList) throws Exception {
        logger.info("call saveEmployees_Draft List ..");
        ApplicationModule am = getConfig();
        try {
            AppModuleImpl service = (AppModuleImpl) am;
            ViewObject vo = service.getEmployeesView1();

            for (EmployeeDto dto : dtoList) {
                createEmployeeRow(vo, dto);
            }

            vo.executeQuery();
            service.getTransaction().commit();


        } finally {
            Configuration.releaseRootApplicationModule(am, true);
        }


    }
    
    /**
     * Saves Employees_Draft list in DB
     * @param dtoList
     */
    public void saveEmployees_Draft_List(List<EmployeeDto> dtoList) throws Exception {
        logger.info("call saveEmployeesList ..");
        ApplicationModule am = getConfig();
        try {
            AppModuleImpl service = (AppModuleImpl) am;
            ViewObject vo = service.getEmployeesDraftView1();;

            for (EmployeeDto dto : dtoList) {
                createEmployeeRow(vo, dto);
            }

            vo.executeQuery();
            service.getTransaction().commit();


        } finally {
            Configuration.releaseRootApplicationModule(am, true);
        }


    }
    /**
     *
     * @param dtoList List<EmployeeDto> EmployeesDraft
     * @param wrongDataList List of Error_Data
     * @throws Exception
     */
    public void saveEmployees_Draft_AND_Error_Data(List<EmployeeDto> dtoList, List<String> wrongDataList) throws Exception {
        logger.info("call saveEmployees_Draft_AND_Error_Data ..");
        ApplicationModule am = getConfig();
        try {
            AppModuleImpl service = (AppModuleImpl) am;
            ViewObject vo = service.getEmployeesDraftView1();
            ViewObject ve = service.getDataErrorsView1();
            // Save EmployeesDraft in DB 
            if(dtoList!= null && !dtoList.isEmpty()){
                    for (EmployeeDto dto : dtoList) {
                        createEmployeeRow(vo, dto);
                    }
                }
            // Save Erro data in DB 
            if(wrongDataList!= null && !wrongDataList.isEmpty()){
                    for (String wrongData : wrongDataList) {
                        System.out.println("error data  : " + wrongData);
                        Row row = ve.createRow();
                        row.setAttribute("Data", wrongData);
                        ve.insertRow(row);
                    }
                }
            // Commit transaction
            vo.executeQuery();
            ve.executeQuery();
            service.getTransaction().commit();


        } finally {
            Configuration.releaseRootApplicationModule(am, true);
        }


    }

    /**
     * Saves Employee   in DB
     * @param EmployeeDto
     */
    public void saveEmployee(EmployeeDto dto) {
        logger.info("Call Services.saveEmploee ...");
        ApplicationModule am = getConfig();
        try {
            AppModuleImpl service = (AppModuleImpl) am;
            ViewObject vo = service.getEmployeesView1();
            createEmployeeRow(vo, dto);
            vo.executeQuery();
            service.getTransaction().commit();


        } finally {
            Configuration.releaseRootApplicationModule(am, true);
        }

    }

    public void updateDataStructure(EmployeeDto dto) throws Exception{
            System.out.println("Call Services.updateDataStructure ...");
            ApplicationModule am = getConfig();
            try {
                AppModuleImpl service = (AppModuleImpl) am;
                ViewObject employeeVO = service.getEmployeesDraftView1();
                ViewObject dataErrorVO = service.getDataErrorsView1();
                
                //Step1 : save Employee_Draft in DB
                createEmployeeRow(employeeVO, dto);
                employeeVO.executeQuery();
              
                // Step2: delete ERROR_DATA row from DB
                dataErrorVO.setWhereClause("Id=" + dto.getEmploeeId());
                Row delRow = dataErrorVO.first();
                delRow.remove();
                dataErrorVO.executeQuery();
                System.out.println("Delete from Error_Data Id : "+dto.getEmploeeId());
                // Step3: commit transaction
                service.getTransaction().commit();
               
            } finally {
                Configuration.releaseRootApplicationModule(am, true);
            }
        
        }

    public void deleteErrorDataById(int id) {
        logger.info("call deleteErrorDataById .. id= : " + id);
        ApplicationModule am = getConfig();
        try {

            AppModuleImpl service = (AppModuleImpl) am;
            ViewObject vo = service.getDataErrorsView1();
            // Get row(s) to delete.
            vo.setWhereClause("Id=" + id);
            vo.executeQuery();

            Row delRow = vo.first();
            delRow.remove();
            am.getTransaction().commit();
        } finally {
            Configuration.releaseRootApplicationModule(am, true);
        }


    }

    public ApplicationModule getConfig() {
        String amDef = "model.AppModule";
        String config = "AppModuleLocal";
        ApplicationModule am = Configuration.createRootApplicationModule(amDef, config);
        return am;
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
     * Save wrong data  in Data Base (ERROR_DATA table)
     * @param wrongDataList
     */
    public void saveWrongDataInDB(List<String> wrongDataList) {
        logger.info("Call   saveWrongDataInDB ...");
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


        } catch (Exception e) {
            // showMessage(FacesMessage.SEVERITY_FATAL, "ERROR_DATA ERROR", e.getMessage());
            System.out.println("ERROR ..." + e.getMessage());
        } finally {
            Configuration.releaseRootApplicationModule(am, true);
        }


    }

    /**
     * Create new EmployeeDto
     * @param empl
     * @return EmployeeDto
     * @throws ParseException
     */
    public EmployeeDto createNewEmployee(String[] empl) throws ParseException {
        EmployeeDto employee = new EmployeeDto();
        if (empl[0] != null && !StringUtils.isBlank(empl[0])) {
            employee.setEmploeeId(Integer.parseInt(empl[0]));
        }

        employee.setFirstName(empl[1]);
        employee.setLastName(empl[2]);
        employee.setEmail(empl[3]);
        employee.setPhoneNumber(empl[4]);
        if (empl[5] != null && !StringUtils.isBlank(empl[5])) {
            Date date = convertStringToDate(empl[5]);
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

    public Date convertStringToDate(String dob) throws ParseException {
        //Instantiating the SimpleDateFormat class
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yy");
        //Parsing the given String to Date object
        Date date = formatter.parse(dob);
        return date;
    }

    /**
     * Convert Array to String
     * @param array represents one line from CSV file
     * @return
     */
    public String convertArrayToString(String[] array) {
        String result = "";
        if (array != null && array.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (String s : array) {
                // To avoid empty String
                if (!StringUtils.isBlank(s)) {
                    sb.append(s).append(",");
                }

            }
            if (!StringUtils.isBlank(sb)) {
                result = sb.deleteCharAt(sb.length() - 1).toString();
            }
        }

        return result;
    }
/**
     * Build summary of uploaded file
     * @param uploadedFile
     * @return FileInfoDto
     */
    public FileInfoDto buildFileInfo(UploadedFile uploadedFile) {
        FileInfoDto fileInfoDto = new FileInfoDto();
        fileInfoDto.setFileName(uploadedFile.getFilename());
        fileInfoDto.setFileType("CSV");
        fileInfoDto.setFileSize(String.valueOf(uploadedFile.getLength()));
        return fileInfoDto;
    }

    public boolean isCsvFile(UploadedFile uploadedFile) {
        return uploadedFile.getFilename().endsWith("csv");
    }
    
    /**
     * refresh table
     * @param viewIterartor
     */
    public void refreshEmpDraftTable(String viewIterartor) {
        DCIteratorBinding iter = (DCIteratorBinding) BindingContext.getCurrent()
                                                                   .getCurrentBindingsEntry()
                                                                   .get(viewIterartor);  
        iter.getViewObject().executeQuery();
    }


}
