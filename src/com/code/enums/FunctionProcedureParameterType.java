
package com.code.enums;

public enum FunctionProcedureParameterType {
    
    IN("in"),
    OUT("out"),
    IN_OUT("inout")
   ;
    
    private String code;
      
    private FunctionProcedureParameterType(String code){
        this.code = code;
    }
  
    public String getCode(){return code;}
}