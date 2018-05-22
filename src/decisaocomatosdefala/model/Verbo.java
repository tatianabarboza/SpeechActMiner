/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package decisaocomatosdefala.model;

/**
 *
 * @author tatia
 */
public class Verbo {
    
    

    /**
     * @return the tipoVerbo
     */
    public String getTipoVerbo() {
        return tipoVerbo;
    }

    /**
     * @param tipoVerbo the tipoVerbo to set
     */
    public void setTipoVerbo(String tipoVerbo) {
        this.tipoVerbo = tipoVerbo;
    }

    /**
     * @return the verbo
     */
    public String getVerbo() {
        return verbo;
    }

    /**
     * @param verbo the verbo to set
     */
    public void setVerbo(String verbo) {
        this.verbo = verbo;
    }
    
    public Verbo(String verbo, String tipoVerbo){
        this.verbo = verbo;
        this.tipoVerbo = tipoVerbo;
    }
    
    private String verbo;
    private String tipoVerbo;
    
}
