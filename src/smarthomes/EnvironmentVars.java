/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smarthomes;

import java.awt.Component;

/**
 *
 * @author root
 */
public class EnvironmentVars extends Component {

    private static EnvironmentVars instance;
    private String lightValue = "";
    private boolean personPresent = false;
    private byte[] detectedFace = null;
    private int[] personPosition = new int[3];
    private double histIntensity = 0;

    private EnvironmentVars() {
    }

    public static EnvironmentVars getInstance() {
        if (instance == null) {
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

    /**
     * @return the personPresent
     */
    public boolean isPersonPresent() {
        return personPresent;
    }

    /**
     * @param personPresent the personPresent to set
     */
    public void setPersonPresent(boolean personPresent) {
        this.personPresent = personPresent;
    }

    /**
     * @return the detectedFace
     */
    public byte[] getDetectedFace() {
        return detectedFace;
    }

    /**
     * @param detectedFace the detectedFace to set
     */
    public void setDetectedFace(byte[] detectedFace) {
        this.detectedFace = detectedFace;
    }

    /**
     * @return the personPosition
     */
    public int[] getPersonPosition() {
        return personPosition;
    }

    /**
     * @param personPosition the personPosition to set
     */
    public void setPersonPosition(int[] personPosition) {
        this.personPosition = personPosition;
    }

    /**
     * @return the histIntensity
     */
    public double getHistIntensity() {
        return histIntensity;
    }

    /**
     * @param histIntensity the histIntensity to set
     */
    public void setHistIntensity(double histIntensity) {
        this.histIntensity = histIntensity;
    }
}
