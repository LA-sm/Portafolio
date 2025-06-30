
package billsapppojos;

import java.io.Serializable;

public class Usuario implements Serializable {
    
    private String alias;
    private String email;
    private String password;
    private String telefono;
    private Integer userId;

    public Usuario() {
    }

    public Usuario(Integer userId) {
        this.userId = userId;
    }

    public Usuario(String alias, String email, String password, String telefono, Integer userId) {
        this.alias = alias;
        this.email = email;
        this.password = password;
        this.telefono = telefono;
        this.userId = userId;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Usuario{" + "alias=" + alias + ", email=" + email + ", password=" + password + ", telefono=" + telefono + ", userId=" + userId + "}";
    }
    
    
    
}
