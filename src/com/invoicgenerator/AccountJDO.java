package com.invoicgenerator;

import javax.jdo.annotations.*;

@PersistenceCapable
public class AccountJDO {
	
	@PrimaryKey
	@Persistent
private String accountNumberPojo;

	@Persistent
private String addressLine1;
	
	@Persistent
private String addressLine2;
		
	@Persistent
private String addressLine3;
		
	@Persistent
private String addressLine4;
	
	@Persistent
private String addressLine5;
	
	public String getAddressLine5() {
		return addressLine5;
	}

	public void setAddressLine5(String addressLine5) {
		this.addressLine5 = addressLine5;
	}

	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	public String getAddressLine3() {
		return addressLine3;
	}

	public void setAddressLine3(String addressLine3) {
		this.addressLine3 = addressLine3;
	}

	public String getAddressLine4() {
		return addressLine4;
	}

	public void setAddressLine4(String addressLine4) {
		this.addressLine4 = addressLine4;
	}

	
	public String getAccountNumberPojo() {
		return accountNumberPojo;
	}

	public void setAccountNumberPojo(String accountNumberPojo) {
		this.accountNumberPojo = accountNumberPojo;
	}

}
