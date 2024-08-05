package com.tenco.bank.handler.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class UnAuthorizedException extends RuntimeException{
    
    private HttpStatus status;
    
    // throw new UnAuthorized Exception( , )
    public UnAuthorizedException(String message,HttpStatus status) {
        super(message);
        this.status=status;
    }
}