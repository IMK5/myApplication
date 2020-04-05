package view.backing;

import beanws.EmployeeDto;

import javax.faces.application.FacesMessage;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.RichSubform;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.Key;
import oracle.jbo.uicli.binding.JUCtrlHierBinding;
import oracle.jbo.uicli.binding.JUCtrlHierNodeBinding;

import org.apache.myfaces.trinidad.event.ReturnEvent;
import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.model.CollectionModel;


public class ManageErrorDataMBean {

    private ADFLogger logger = ADFLogger.createADFLogger(this.getClass());
    private EmployeeDto empDto = new EmployeeDto();
    private RichInputText emploeeIdInput;
    private Services services = new Services();
    private RichTable errorDataTable;
    private RichPopup popup;
    private RichSubform subFormtable;
    


    public void saveEmployee(ActionEvent actionEvent) {
        System.out.println("Call saveEmploee ...");
        try {
            if(getEmpDto().getEmploeeId()==null){
                throw new Exception("EmployeeId is NULL ! Please choose an ID");
                }
            services.updateDataStructure(getEmpDto());
           // services.saveEmployee(getEmpDto());
           // services.deleteErrorDataById(getEmpDto().getEmploeeId());
            // showMessage(FacesMessage.SEVERITY_INFO, "Success", "Data updated successfully");
            showPopup();
            setEmpDto(new EmployeeDto());
          //  refresh();
          DCIteratorBinding iter = (DCIteratorBinding) BindingContext.getCurrent().getCurrentBindingsEntry().get("DataErrorsView1Iterator"); // from pageDef.
          iter.getViewObject().executeQuery();

            AdfFacesContext.getCurrentInstance().addPartialTarget(getErrorDataTable());
            AdfFacesContext.getCurrentInstance().addPartialTarget(getSubFormtable());

        } catch (Exception e) {
            e.getStackTrace();
            System.out.println("ERROR SAVING DATA..." + e.getMessage());
            showMessage(FacesMessage.SEVERITY_ERROR, "Error while Execution", e.getMessage());
        }
    }

    /**
     * When user click on table's row
     * @param selectionEvent
     */
    public void selectionRowListener(SelectionEvent selectionEvent) {
        System.out.println("Call selectionRowListener  ...");
        onTableRowSelection(selectionEvent);
        DCBindingContainer binding = (DCBindingContainer) BindingContext.getCurrent().getCurrentBindingsEntry();
        DCIteratorBinding itorBinding = binding.findIteratorBinding("DataErrorsView1Iterator");
       // List<Object>  list =  binding.getOperationBindings();
        String empId = itorBinding.getCurrentRow()
                                  .getAttribute("Id")
                                  .toString();
        System.out.println("Employee Id selected is : " + empId);
        getEmploeeIdInput().setValue(empId);
        AdfFacesContext.getCurrentInstance().addPartialTarget(getEmploeeIdInput());


    }

    public void onTableRowSelection(SelectionEvent selectionEvent) {
        RichTable _table = (RichTable) selectionEvent.getSource();
        CollectionModel model = (CollectionModel) _table.getValue();
        JUCtrlHierBinding _binding = (JUCtrlHierBinding) model.getWrappedData();
        DCIteratorBinding iteratorBinding = _binding.getDCIteratorBinding();
        Object selectedRowData = _table.getSelectedRowData();
        JUCtrlHierNodeBinding node = (JUCtrlHierNodeBinding) selectedRowData;
        Key rwkey = node.getRowKey();
        iteratorBinding.setCurrentRowWithKey(rwkey.toStringFormat(true));


    }

    public void refreshAfterCommit(ReturnEvent returnEvent) {
        System.out.println("Call refreshAfterCommit ...");
        FacesContext fc = FacesContext.getCurrentInstance();


        String refreshpage = fc.getViewRoot().getViewId();
        ViewHandler ViewH = fc.getApplication().getViewHandler();
        UIViewRoot UIV = ViewH.createView(fc, refreshpage);
        UIV.setViewId(refreshpage);
        fc.setViewRoot(UIV);


    }

    public void refresh() {
        System.out.println("Call refresh ...");
        FacesContext fc = FacesContext.getCurrentInstance();
        String refreshpage = fc.getViewRoot().getViewId();
        ViewHandler ViewH = fc.getApplication().getViewHandler();
        UIViewRoot UIV = ViewH.createView(fc, refreshpage);
        UIV.setViewId(refreshpage);
        fc.setViewRoot(UIV);
    }

    public String showMessage(FacesMessage.Severity severity, String message, String errorMessage) {
        FacesContext facesCntx = FacesContext.getCurrentInstance();
        FacesMessage msg = new FacesMessage(severity, message, errorMessage);
        facesCntx.addMessage(null, msg);
        return null;
    }


    public void showPopup() {
        RichPopup.PopupHints hints = new RichPopup.PopupHints();
        this.getPopup().setAutoDismissalTimeout(3);
        this.getPopup().show(hints);
    }


    public void resetFormInput(ActionEvent actionEvent) {
        System.out.println("CAll resetFormInput    ...");
        setEmpDto(new EmployeeDto());
    }

     

    public void setEmpDto(EmployeeDto empDto) {
        this.empDto = empDto;
    }

    public EmployeeDto getEmpDto() {
        return empDto;
    }

    public void setServices(Services services) {
        this.services = services;
    }

    public Services getServices() {
        return services;
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

    public void setPopup(RichPopup popup) {
        this.popup = popup;
    }

    public RichPopup getPopup() {
        return popup;
    }


    public void setSubFormtable(RichSubform subFormtable) {
        this.subFormtable = subFormtable;
    }

    public RichSubform getSubFormtable() {
        return subFormtable;
    }
/*
    public void callRefreshTable(PollEvent pollEvent) {
       System.out.println("call callRefreshTable method ..");
       DCBindingContainer bindings =(DCBindingContainer) getBindings();
       DCIteratorBinding dcIter = bindings.findIteratorBinding("DataErrorsView1Iterator");
       dcIter.executeQuery();
        AdfFacesContext.getCurrentInstance().addPartialTarget(getErrorDataTable());
        AdfFacesContext.getCurrentInstance().addPartialTarget(getSubFormtable());
    }

    public  BindingContainer getBindings(){
          return (BindingContainer) BindingContext.getCurrent().getCurrentBindingsEntry();                                                  }
     }
*/

     
}
