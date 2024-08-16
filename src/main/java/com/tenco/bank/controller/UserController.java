package com.tenco.bank.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.tenco.bank.dto.OAuthToken;
import com.tenco.bank.dto.SignInDTO;
import com.tenco.bank.dto.SignUpDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.service.UserService;
import com.tenco.bank.utils.Define;

import jakarta.servlet.http.HttpSession;

@Controller // Ioc의 대상 (싱글톤 패턴으로 관리 됨)
@RequestMapping("/user") // 대문 처리
public class UserController {

	private final UserService userService;
	private final HttpSession session;

	// DI처리
	@Autowired // 노란색 경고는 사용할 필요 없음 - 가독성 위해서 선언해도 됨
	public UserController(UserService service, HttpSession session) {
		this.userService = service;
		this.session = session;
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
			throw new DataDeliveryException(Define.ENTER_YOUR_USERNAME, HttpStatus.BAD_REQUEST);
		}

		if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_PASSWORD, HttpStatus.BAD_REQUEST);
		}

		if (dto.getFullname() == null || dto.getFullname().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_FULLNAME, HttpStatus.BAD_REQUEST);
		}

		// 서비스 객체 전달
		userService.createUser(dto);
		// TODO - 추후 수정
		return "redirect:/user/sign-in";
	}

	/*
	 * 로그인 화면 요청 주소설계 : http://localhost:8080/user/sign-in
	 */
	@GetMapping("/sign-in")
	public String signInPage() {
		// 인증검사 x
		// 유효성 검사 x
		return "user/signIn";
	}

	/**
	 * 로그인 요청 처리
	 * @param dto
	 * @return
	 */
	@PostMapping("/sign-in")
	public String signProc(SignInDTO dto) {

		// 1. 인증 검사 x
		// 2. 유효성 검사
		if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_USERNAME, HttpStatus.BAD_REQUEST);
		}

		if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_PASSWORD, HttpStatus.BAD_REQUEST);
		}

		// 서비스 호출
		User principal = userService.readUser(dto);

		// 세션 메모리에 등록 처리
		session.setAttribute(Define.PRINCIPAL, principal);

		// 새로운 페이지로 이동처리
		// TODO - 계좌 목록 페이지 이동처리 예정
		return "redirect:/index";
	}
	
	/**
	 * 로그아웃 처리
	 * @return
	 */
	@GetMapping("/logout")
	public String logout() {
		session.invalidate();  // 로그아웃 됨
		return "redirect:/user/sign-in";
	}
	
	@GetMapping("/kakao")
	@ResponseBody //@RestController = @Controller + @ ResponseBody
	public String getMethodName(@RequestParam("code") String code) {
		System.out.println(code);
		
		// POST - 카카오 토큰 요청 받기
		// Header, body 구성
		RestTemplate rt1 = new RestTemplate();
		// 헤더 구성
		HttpHeaders header1 = new HttpHeaders();
		header1.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
		// 바디 구성
		MultiValueMap<String, String> params1 = new LinkedMultiValueMap<String, String>();
		params1.add("grant_type", "authorization_code");
		params1.add("client_id", "ae7c97b7d1b43bc5ffa24d2956b1febf");
		params1.add("redirect_uri", "http://localhost:8080/user/kakao");
		params1.add("code", code);
		
		// 헤더 + 바디 결합
		HttpEntity<MultiValueMap<String, String>> reqkakaMessage
			= new HttpEntity<>(params1, header1);
		
		// 통신 요청
		ResponseEntity<OAuthToken> response1 = rt1.exchange("https://kauth.kakao.com/oauth/token", 
				HttpMethod.POST, reqkakaMessage, OAuthToken.class);
		System.out.println("response : " + response1.getBody().toString());
		
		// 카카오 리소스 서버 사용자 정보 가져오기
		RestTemplate rt2 = new RestTemplate();
		// 헤더
		HttpHeaders headers2 = new HttpHeaders();
		// 반드시 Bearer 다음에 공백 한 칸 추가
		headers2.add("Authorization", "Bearer " + response1.getBody().getAccessToken());
		headers2.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
		// 본문 x 
		
		// Http Entity 만들기
		HttpEntity<MultiValueMap<String, String>> reqKakoInfoMessage = new HttpEntity<>(headers2);
		
		// 통신 요청
		ResponseEntity<String> response2 = rt2.exchange("https://kapi.kakao.com/v2/user/me", HttpMethod.POST,
				reqKakoInfoMessage, String.class);
		
		System.out.println("response2 : " + response2);
		
		return response2.getBody();
	}
	
	
	
	
	

}
