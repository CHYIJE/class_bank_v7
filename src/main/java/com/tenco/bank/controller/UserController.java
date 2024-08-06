package com.tenco.bank.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tenco.bank.dto.SignUpDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.service.UserService;

@Controller // Ioc의 대상 (싱글톤 패턴으로 관리 됨)
@RequestMapping("/user") // 대문 처리
public class UserController {

	private UserService userService;

	// DI처리
	@Autowired // 노란색 경고는 사용할 필요 없음 - 가독성 위해서 선언해도 됨
	public UserController(UserService service) {
		this.userService = service;
	}

	/**
	 * 회원 가입 페이지 요청 주소설계 -> http://localhost:8080/user/sign-up
	 * 
	 * @return signUp.jsp
	 */

	@GetMapping("/sign-up")
	public String signUpPage() {
		// prefix: WEB-INF/view
		// suffix: .jsp
		// return: user/signUp
		return "user/signUp";
	}

	/**
	 * 회원 가입 로직 처리 요청 주소 설계 : http://localhost:8080/user/sign-up
	 * 
	 * @param dto
	 * @return
	 */

	@PostMapping("/sign-up")
	public String signUpProc(SignUpDTO dto) {
		// controller에서 일반적인 코드 작업
		// 1. 인증검사(여기서는 인증검사 불필요)
		// 2. 유효성 검사
		if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
			throw new DataDeliveryException("username 을 입력하세요", HttpStatus.BAD_REQUEST);
		}

		if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
			throw new DataDeliveryException("password 을 입력하세요", HttpStatus.BAD_REQUEST);
		}

		if (dto.getFullname() == null || dto.getFullname().isEmpty()) {
			throw new DataDeliveryException("fullname 을 입력하세요", HttpStatus.BAD_REQUEST);
		}

		// 서비스 객체 전달
		userService.createUser(dto);
		// TODO - 추후 수정
		return "redirect:/index";
	}

}
