package model.applicationModule;

import oracle.jbo.ApplicationModule;
import oracle.jbo.client.Configuration;

public class AppModuleAMFixture {
    private static AppModuleAMFixture fixture1 = new AppModuleAMFixture();
    private ApplicationModule _am;

    private AppModuleAMFixture() {
        _am = Configuration.createRootApplicationModule("model.AppModule", "AppModuleLocal");
    }

    public void setUp() {
    }

    public void tearDown() {
    }

    public static AppModuleAMFixture getInstance() {
        return fixture1;
    }

    public void release() throws Exception {
        Configuration.releaseRootApplicationModule(_am, true);
    }

    public ApplicationModule getApplicationModule() {
        return _am;
    }
}
