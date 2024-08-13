package com.tenco.bank.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.tenco.bank.dto.DepositDTO;
import com.tenco.bank.dto.SaveDTO;
import com.tenco.bank.dto.TransferDTO;
import com.tenco.bank.dto.withdrawalDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.UnAuthorizedException;
import com.tenco.bank.repository.model.Account;
import com.tenco.bank.repository.model.HistoryAccount;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.service.AccountService;
import com.tenco.bank.utils.Define;

import jakarta.servlet.http.HttpSession;

@Controller // IoC 대상(싱글톤으로 관리)
@RequestMapping("/account")
public class AccountController {

	// 계좌 생성 화면 요청 - DI 처리
	private final AccountService accountService;
	private final HttpSession session;

	// @Autowired
	public AccountController(AccountService accountService, HttpSession session) {
		this.accountService = accountService;
		this.session = session;
	}

	/**
	 * 계좌 생성 페이지 요청 주소 설계 : http://localhost:8080/account/save
	 * 
	 * @return save.jsp
	 */
	@GetMapping("/save")
	public String savePage() {
		return "account/save";
	}

	/**
	 * 계좌 생성 기능 요청 주소설계 : http://localhost:8080/account/save
	 */
	@PostMapping("/save")
	public String saveProc(SaveDTO dto, @SessionAttribute(Define.PRINCIPAL) User principal) {

		// 3. 유효성 검사
		if (dto.getNumber() == null || dto.getNumber().isEmpty()) {
			throw new DataDeliveryException(Define.INVALID_INPUT, HttpStatus.BAD_REQUEST);
		}

		if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_ACCOUNT_NUMBER, HttpStatus.BAD_REQUEST);
		}

		if (dto.getBalance() == null || dto.getBalance() <= 0) {
			throw new DataDeliveryException(Define.ENTER_YOUR_BALANCE, HttpStatus.BAD_REQUEST);
		}

		// 4. 서비스 호출
		accountService.createAccount(dto, principal.getId());

		return "redirect:/account/list";
	}

	/**
	 * 계좌 목록 화면 요청 주소 설계 http://localhost:8080/account/list,....
	 * 
	 * @return
	 */
	@GetMapping({ "/list", "/" })
	private String listPage(Model model, @SessionAttribute(Define.PRINCIPAL) User principal) {

		// 2. 유효성 검사
		// 3. 서비스 호출
		List<Account> accountList = accountService.readAccountListByUserId(principal.getId());
		if (accountList.isEmpty()) {
			model.addAttribute("accountList", null);
		} else {
			model.addAttribute("accountList", accountList);
		}

		return "account/list";
		// JSP 데이트를 넣어 주는 방법
	}

	/**
	 * 출금 페이지 요청
	 * 
	 * @return withdrawal.jsp
	 */
	@GetMapping("/withdrawal")
	public String withdrawalPage() {
		return "account/withdrawal";
	}

	@PostMapping("/withdrawal")
	public String withdrawalProc(withdrawalDTO dto, @SessionAttribute(Define.PRINCIPAL) User principal) {

		// 유효성 검사 (자바 코드를 개발 ) --> 스프링 부트 @Valid 라이브러리가 존재
		if (dto.getAmount() == null) {
			throw new DataDeliveryException(Define.ENTER_YOUR_BALANCE, HttpStatus.BAD_REQUEST);
		}
		if (dto.getAmount().longValue() <= 0) {
			throw new DataDeliveryException(Define.W_BALANCE_VALUE, HttpStatus.BAD_REQUEST);
		}
		if (dto.getWAccountNumber() == null) {
			throw new DataDeliveryException(Define.ENTER_YOUR_ACCOUNT_NUMBER, HttpStatus.BAD_REQUEST);
		}
		if (dto.getWAccountPassword() == null || dto.getWAccountPassword().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_PASSWORD, HttpStatus.BAD_REQUEST);
		}

		accountService.updateAccountWithdraw(dto, principal.getId());
		return "redirect:/account/list";
	}

	// 입금 페이지 요청
	@GetMapping("/deposit")
	public String depositPage() {

		return "account/deposit";
	}

	// 입금 처리 기능 만들기
	@PostMapping("/deposit")
	public String depositProc(DepositDTO dto, @SessionAttribute(Define.PRINCIPAL) User principal) {

		if (dto.getAmount() == null) {
			throw new DataDeliveryException(Define.ENTER_YOUR_BALANCE, HttpStatus.BAD_REQUEST);
		}
		if (dto.getAmount().longValue() <= 0) {
			throw new DataDeliveryException(Define.D_BALANCE_VALUE, HttpStatus.BAD_REQUEST);
		}
		if (dto.getDAccountNumber() == null || dto.getDAccountNumber().trim().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_ACCOUNT_NUMBER, HttpStatus.BAD_REQUEST);
		}
		accountService.updateAccountDeposit(dto, principal.getId());
		return "redirect:/account/list";
	}

	// 이체 페이지 요청
	@GetMapping("/transfer")
	public String transeferPage() {

		return "account/transfer";
	}

	// 이체 기능 처리 요청
	@PostMapping("/transfer")
	public String transFerProc(TransferDTO dto, @SessionAttribute(Define.PRINCIPAL) User principal) {

		if (dto.getAmount() == null) {
			throw new DataDeliveryException(Define.ENTER_YOUR_BALANCE, HttpStatus.BAD_REQUEST);
		}
		if (dto.getAmount().longValue() <= 0) {
			throw new DataDeliveryException(Define.W_BALANCE_VALUE, HttpStatus.BAD_REQUEST);
		}
		if (dto.getWAccountNumber() == null) {
			throw new DataDeliveryException(Define.ENTER_YOUR_ACCOUNT_NUMBER, HttpStatus.BAD_REQUEST);
		}
		if (dto.getPassword() == null) {
			throw new DataDeliveryException(Define.ENTER_YOUR_PASSWORD, HttpStatus.BAD_REQUEST);
		}
		if (dto.getDAccountNumber() == null || dto.getDAccountNumber().trim().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_ACCOUNT_NUMBER, HttpStatus.BAD_REQUEST);
		}

		accountService.updateAccountTransfer(dto, principal.getId());
		return "redirect:/account/list";
	}

	/**
	 * 계좌 상세보기 페이지 주소 설계 : http://localhost:8080/account/detail/1?type=all, deposit,
	 * withdraw
	 * 
	 * @return
	 */
	@GetMapping("/detail/{accountId}")
	public String detail(@PathVariable(name = "accountId") Integer accountId,
			@RequestParam(required = false, name = "type") String type,
			@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "size", defaultValue = "2") int size, Model model) {

		// 유효성 검사
		List<String> validTypes = Arrays.asList("all", "deposit", "withdrawal");

		if (!validTypes.contains(type)) {
			throw new DataDeliveryException("유효하지 않은 접근입니다.", HttpStatus.BAD_REQUEST);
		}

		// 페이지 개수를 계산하기 위해서 총 페이지 수를 계산해주어야 한다.
		int totalRecords = accountService.countHistoryByAccountIdAndType(type, accountId);
		int totalPages = (int) Math.ceil((double) totalRecords / size);

		// 쿼리를 두번 던지지만 한방 쿼릴로 하는것이 서버 자원을 아낄 수 있다.
		Account account = accountService.readAccountById(accountId);
		List<HistoryAccount> historyList = accountService.readHistoryByAccountId(type, accountId, page, size);

		model.addAttribute("account", account);
		model.addAttribute("historyList", historyList);

		model.addAttribute("currentPage", page);
		System.out.println("page : " + page);

		model.addAttribute("totalPages", totalPages);
		System.out.println("totalPages : " + totalPages);

		model.addAttribute("type", type);
		System.out.println("type : " + type);

		model.addAttribute("size", size);
		System.out.println("size : " + size);

		return "account/detail";
	}

}
