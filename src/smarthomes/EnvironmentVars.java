/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smarthomes;

/**
 *
 * @author root
 */
public class EnvironmentVars {
    private static EnvironmentVars instance ;
    
    private String lightValue ="";
    private EnvironmentVars(){}
    public static EnvironmentVars getInstance(){
    if(instance==null){
    instance = new EnvironmentVars();
    }
    return instance;
    }

    /**
     * @return the lightValue
     */
    public String getLightValue() {
        return lightValue;
    }

    /**
     * @param lightValue the lightValue to set
     */
    public void setLightValue(String lightValue) {
        this.lightValue = lightValue;
    }
}
