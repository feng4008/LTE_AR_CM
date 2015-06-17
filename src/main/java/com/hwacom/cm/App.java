package com.hwacom.cm;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hwacom.cm.task.TaskManager;


/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {
    	ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
    	
    	TaskManager taskManager = (TaskManager) context.getBean("taskManager");
    	
    	taskManager.arrangeCollectConfigTask();
    }
}
