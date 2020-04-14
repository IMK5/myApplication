package view.backing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;

public class ValidationRules {
    
    
    public boolean emailValidator(String email_address) {
        boolean isValidate = true;
        String email_pattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        if (!StringUtils.isEmpty(email_address)) {
            Pattern patn = Pattern.compile(email_pattern);
            Matcher matcher = patn.matcher(email_address);

            String error_Message = "You have entered an invalid email address. Please try again.";

            if (!matcher.matches()) {
                isValidate = false;
                //throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, Error_Message, null));
                shoeInlineMessage(error_Message);

            }
        }

        return isValidate;
    }
    
    
    
    
    public void shoeInlineMessage(String message){
            FacesMessage msg = new FacesMessage(message);
            msg.setSeverity(FacesMessage.SEVERITY_FATAL);
            FacesContext fctx = FacesContext.getCurrentInstance();
            fctx.addMessage(null, msg);
        }
}
