package com.code.enums;


public enum CustomerColumns {
	name("name"), 
	code("code"),
	address("address"), 
	phoneOne("phoneOne"),
	phoneTwo("phoneTwo"),
	creditLimit("creditLimit"),
	currentCredit("currentCredit"),
	id("id");
	
	private String value;
    
    private CustomerColumns(String value){
        this.value = value;
    }
  
    public String getValue(){return value;}
}
