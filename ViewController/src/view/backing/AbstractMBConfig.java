package view.backing;

import model.AppModuleImpl;

import oracle.jbo.ApplicationModule;
import oracle.jbo.client.Configuration;

public abstract class AbstractMBConfig {
    
    
    protected AppModuleImpl getConfig() {
        String amDef = "model.AppModule";
        String config = "AppModuleLocal";
        ApplicationModule am = Configuration.createRootApplicationModule(amDef, config);
        return (AppModuleImpl) am;
    }
    
    protected void rollback(AppModuleImpl app){
            
        app.getTransaction().rollback();
        
        
        }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    }
