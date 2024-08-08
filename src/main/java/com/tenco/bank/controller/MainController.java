package com.tenco.bank.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.handler.exception.UnAuthorizedException;
import com.tenco.bank.utils.Define;

@Controller // Ioc 대상(싱그톤 패턴 관리가 된다.) --> 제어의 역전
public class MainController {

	// REST API 기반으로 주소설계 가능

	// 주소설계
	// http:localhost:8080/main-page

	@GetMapping({ "/main-page", "/index" })
	public String mainPage() {
		System.out.println("mainPage() 호출 확인");
		// [JSP 파일 찾기 (yml 설정)] - 뷰 리졸버
		// prefix: /WEB-INF/view
		// /main
		// suffix: .jsp

		return "main";
	}

	// todo - 삭제 예정
	// 주소 설계
	// http://localhost:8080/error-test1/true
	// http://localhost:8080/error-test1/isError

	@GetMapping("error-test1/{isError}")
	public String errorPage(@PathVariable("isError") boolean isError) {
		System.out.println("-------------");
		System.out.println("isError: " + isError);
		if (isError) {
			throw new RedirectException(Define.INVALID_INPUT, HttpStatus.NOT_FOUND);
		}
		return "main";
	}

	// http://localhost:8080/error-test2
	@GetMapping("error-test2")
	public String errorData() {
		if (true) {
			throw new DataDeliveryException(Define.INVALID_INPUT, HttpStatus.BAD_REQUEST);
		}
		return "main";
	}

	// http://localhost:8080/error-test3
	@GetMapping("error-test3")
	public String errorData3() {
		if (true) {
			throw new UnAuthorizedException(Define.NOT_AN_AUTHENTICATED_USER, HttpStatus.BAD_REQUEST);
		}
		return "main";
	}

}
