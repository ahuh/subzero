package org.subzero.core.bean;

/**
 * Process Report
 * @author Julien
 *
 */
public class ProcessReport {
	private int nbFileToProcess;
	private int nbFileSuccess;
	private int nbFileNoSub;
	private int nbFileNoPostProcess;
	
	public int getNbFileToProcess() {
		return nbFileToProcess;
	}
	public void setNbFileToProcess(int nbFileToProcess) {
		this.nbFileToProcess = nbFileToProcess;
	}
	public int getNbFileSuccess() {
		return nbFileSuccess;
	}
	public void setNbFileSuccess(int nbFileSuccess) {
		this.nbFileSuccess = nbFileSuccess;
	}
	public int getNbFileNoSub() {
		return nbFileNoSub;
	}
	public void setNbFileNoSub(int nbFileNoSub) {
		this.nbFileNoSub = nbFileNoSub;
	}
	public int getNbFileNoPostProcess() {
		return nbFileNoPostProcess;
	}
	public void setNbFileNoPostProcess(int nbFileNoPostProcess) {
		this.nbFileNoPostProcess = nbFileNoPostProcess;
	}
	public ProcessReport(int nbFileToProcess, int nbFileSuccess,
			int nbFileNoSub, int nbFileNoPostProcess) {
		super();
		this.nbFileToProcess = nbFileToProcess;
		this.nbFileSuccess = nbFileSuccess;
		this.nbFileNoSub = nbFileNoSub;
		this.nbFileNoPostProcess = nbFileNoPostProcess;
	}
	
}
