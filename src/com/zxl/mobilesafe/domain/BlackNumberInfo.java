package com.zxl.mobilesafe.domain;

/**
 * 黑名单号码的业务bean
 * @author ZXL
 *
 */
public class BlackNumberInfo {
	private String number;
	private String mode;
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	@Override
	public String toString() {
		return "BlackNumberInfo [number=" + number + ", mode=" + mode + "]";
	}
	
}
