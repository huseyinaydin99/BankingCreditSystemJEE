package tr.com.huseyinaydin.web.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.Serializable;

public abstract class AbstractManagedBean implements Serializable {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected void addSuccessMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }

    protected void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    /**
     * Spring context'ten bean çözer — CDI yokken JSF bean'lerine Spring bean enjekte etmek için.
     */
    protected <T> T getBean(Class<T> type) {
        FacesContext fc = FacesContext.getCurrentInstance();
        ServletContext sc = (ServletContext) fc.getExternalContext().getContext();
        return WebApplicationContextUtils.getRequiredWebApplicationContext(sc).getBean(type);
    }
}
