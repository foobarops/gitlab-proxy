package com.example.gitlabproxy.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<String> handleIllegalStateException(IllegalStateException ex) {
		// Log the exception message without the stack trace
		log.error(ex.getMessage());

		// Respond with a 5xx error
		return new ResponseEntity<>("Internal Server Error: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
