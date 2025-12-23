package com.ecommerce.project.customExceptionHandler;

import javax.xml.transform.sax.SAXResult;

public class ResourseNorFoundException extends RuntimeException {

    String resource;
    String fieldName;
    String field;
    Long fieldId;

    public ResourseNorFoundException() {
    }

    public ResourseNorFoundException(String resource, String fieldName, String field) {
        super(String.format("%s not found with %s: %s",resource,fieldName,field));
        this.resource = resource;
        this.fieldName = fieldName;
        this.field = field;
    }

    public ResourseNorFoundException(String resource, String field, Long fieldId) {
        super(String.format("%s not found with %s: %s",resource,field,fieldId));
        this.resource = resource;
        this.field = field;
        this.fieldId = fieldId;
    }
}
