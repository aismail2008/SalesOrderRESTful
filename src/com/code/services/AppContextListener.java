package com.code.services;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.code.dal.DataAccess;


public class AppContextListener implements ServletContextListener {
    
    public void contextInitialized(ServletContextEvent event) {    
        DataAccess.init();
        System.out.println("Loading !!!");
    }
  
    public void contextDestroyed(ServletContextEvent event) {}
}
