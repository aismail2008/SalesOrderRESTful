package com.code.enums;


public enum SalesOrderColumns {
	totalPrice("totalPrice"),
	orderNumber("orderNumber"), 
	customerId("customerId"),
	customerCode("customerCode"), 
	id("id");
	
	private String value;
    
    private SalesOrderColumns(String value){
        this.value = value;
    }
  
    public String getValue(){return value;}
}
