package uk.co.metadesignsolutions.javachallenge.external.testlogger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestPrinter {

    Logger logger = LoggerFactory.getLogger(TestPrinter.class);

    private String groupName;


    public TestPrinter() {
    }

    public TestPrinter(String groupName) {
        this.groupName = groupName;
    }

    public void print(Runnable runnable,Integer point, Position position ,String name){

        TestResult testResult = new TestResult();
        testResult.setPosition(position);
        testResult.setPoint(point);

        try {
            runnable.run();
            testResult.setStatus(Status.PASSED);
        }catch (AssertionError | Exception error){
            testResult.setError(error.getMessage());
            testResult.setStatus(Status.FAILED);
        }

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement stackTraceElement = stackTrace[2];

        String groupName = getGroupName(stackTraceElement);
        testResult.setGroupName(groupName);
        testResult.setName(name);

        ObjectMapper objectMapper = new ObjectMapper();
        String output = null;
        try {
            output = objectMapper.writeValueAsString(testResult);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        System.out.println("[TEST-STAT]  uk.co.mds.TestPrinter *-* " + output);
    }


    public  void print(Runnable runnable, Integer point , Position position ) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement stackTraceElement = stackTrace[2];
        String methodName = getMethodName(stackTraceElement);
        print(runnable,point,position,methodName);

    }

    private  String getMethodName(StackTraceElement stackTraceElement) {
         return camelCaseToSentence(stackTraceElement.getMethodName());
    }

    private  String getGroupName(StackTraceElement stackTraceElement) {
        if (groupName!=null && !groupName.equals("")){
            return groupName;
        }

        String className = stackTraceElement.getClassName();
        int lastDotIndex = className.lastIndexOf(".");
        String groupName=  className.substring(lastDotIndex + 1);
        return camelCaseToSentence(groupName);
    }


    private static String camelCaseToSentence(String input) {
        StringBuilder result = new StringBuilder();
        result.append(Character.toUpperCase(input.charAt(0)));
        for (int i = 1; i < input.length(); i++) {
            char currentChar = input.charAt(i);
            if (Character.isUpperCase(currentChar)) {
                result.append(" ");
            }
            result.append(currentChar);
        }
        return result.toString().trim();
    }



}
