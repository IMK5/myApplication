package beanws;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import oracle.adf.model.BindingContext;

import oracle.binding.BindingContainer;
import oracle.binding.OperationBinding;

public class DisplayMBean {
    public DisplayMBean() {
    }

    public BindingContainer getBindings() {
        return BindingContext.getCurrent().getCurrentBindingsEntry();
    }

    public String commit_action() {
        BindingContainer bindings = getBindings();
        OperationBinding operationBinding = bindings.getOperationBinding("Commit");
        Object result = operationBinding.execute();
        
        if (!operationBinding.getErrors().isEmpty()) {
            return null;
        }
        else{
                FacesContext ctx = FacesContext.getCurrentInstance();
                FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_INFO," Updated with success!.", "");
                ctx.addMessage(null,fm);
                return "success";
            }
        
    }
}
