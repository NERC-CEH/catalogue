package uk.ac.ceh.ukeof.model;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * @author Christopher Johnson
 */
@Data
@Accessors(chain = true)
@XmlRootElement
public class UsernamePassword {
    @NotNull @NotEmpty(message = "Username cannot be blank")
    private String username;
    @NotNull @NotEmpty(message = "Password cannot be blank")
    private String password;
}
