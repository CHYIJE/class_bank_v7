package com.tenco.bank.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tenco.bank.dto.DepositDTO;
import com.tenco.bank.dto.SaveDTO;
import com.tenco.bank.dto.TransferDTO;
import com.tenco.bank.dto.withdrawalDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.repository.interfaces.AccountRepository;
import com.tenco.bank.repository.interfaces.HistoryRepository;
import com.tenco.bank.repository.model.Account;
import com.tenco.bank.repository.model.History;
import com.tenco.bank.repository.model.HistoryAccount;
import com.tenco.bank.utils.Define;

@Service
public class AccountService {
	private final AccountRepository accountRepository;
	private final HistoryRepository historyRepository;

	@Autowired // 생략 가능 - DI 처리
	public AccountService(AccountRepository accountRepository, HistoryRepository historyRepository) {
		this.accountRepository = accountRepository;
		this.historyRepository = historyRepository;

	}

	/**
	 * 계좌 생성 기능
	 * 
	 * @param dto
	 * @param id
	 */
	// 트랜잭션 처리
	@Transactional
	public void createAccount(SaveDTO dto, Integer principalId) {
		int result = 0;
		try {
			result = accountRepository.insert(dto.toAccount(principalId));
		} catch (DataAccessException e) {
			throw new DataDeliveryException(Define.INVALID_INPUT, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new RedirectException(Define.UNKNOWN, HttpStatus.SERVICE_UNAVAILABLE);
		}
		if (result == 0) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// select 구문에 트랜잭션 처리 안걸꺼임 일단은 왜냐하면 개같이 까이기 때문
	public List<Account> readAccountListByUserId(Integer userId) {
		List<Account> accountListEntity = null;

		try {
			accountListEntity = accountRepository.findByUserId(userId);
		} catch (DataAccessException e) {
			throw new DataDeliveryException(Define.INVALID_INPUT, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new RedirectException(Define.UNKNOWN, HttpStatus.SERVICE_UNAVAILABLE);
		}

		return accountListEntity;

	}

	// 한번에 모든 기능을 생각하기 힘듬
	// 1. 계좌 존재 여부를 확인 -- select
	// 2. 본인 계좌 여부를 확인 -- 객체 상태값에서 비교
	// 3. 계좌 비번 확인 -- 객체 상태값에서 일치 여부 확인
	// 4. 잔액 여부 확인 -- 객체 상태값에서 확인
	// 5. 출금 처리 -- update
	// 6. 거래 내역 등록 -- insert(history)
	// 7. 트랜잭션 처리

	@Transactional // 7.트랜잭션 처리~
	public void updateAccountWithdraw(withdrawalDTO dto, Integer principalId) {
		// 1.
		Account accountEntity = accountRepository.findByNumber(dto.getWAccountNumber());
		if (accountEntity == null) {
			throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
		}
		// 2.
		accountEntity.checkOwner(principalId);

		// 3.
		accountEntity.checkPassword(dto.getWAccountPassword());

		// 4.
		accountEntity.checkBalance(dto.getAmount());

		// 5 출금 기능
		// accountEntity 객체의 잔액으 ㄹ변경하고 업데이트 처리해야 한다.
		accountEntity.withdraw(dto.getAmount());
		// update 처리
		accountRepository.updateById(accountEntity);

		// 6. 거래 내역 등록
		/*
		 * <insert id="insert"> insert into history_tb(amount, w_balance, d_balance,
		 * w_account_id, d_account_id) values ( #{amount}, #{wBalance}, #{dBalance},
		 * #{wAccountId}, #{dAccountId} ) </insert>
		 */
		History history = new History();
		history.setAmount(dto.getAmount());
		history.setWBalance(accountEntity.getBalance());
		history.setDBalance(null);
		history.setWAccountId(principalId);
		history.setDAccountId(null);

		int rowResultCount = historyRepository.insert(history);
		if (rowResultCount != 1) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 입금 기능 만들기
	// 1. 계좌 존재 여부를 확인
	// 2. 본인 계좌 여부를 확인 -- 객체 상태값에서 비교
	// 3. 입금 처리 -- update
	// 4. 거래 내역 등록 --
	@Transactional
	public void updateAccountDeposit(DepositDTO dto, Integer principalId) {
		// 1.
		Account accountEntity = accountRepository.findByNumber(dto.getDAccountNumber());
		if (accountEntity == null) {
			throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
		}
		// 2.
		accountEntity.checkOwner(principalId);
		// 3.
		accountEntity.deposit(dto.getAmount());
		accountRepository.updateById(accountEntity);
		// 4.
		History history = History.builder().amount(dto.getAmount()).dAccountId(accountEntity.getId())
				.dBalance(accountEntity.getBalance()).wAccountId(null).wBalance(null).build();
		int rowResultCount = historyRepository.insert(history);
		if (rowResultCount != 1) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 이체 기능 만들기
	// 1. 출금 계좌 존재 여부 확인 -- select
	// 2. 입금 계좌 존재 여부 확인 -- select (객체 리턴 받은 상태)
	// 3. 출금 계좌 본인 소유 확인 -- 객체 상태값과 세션 id 비교
	// 4. 출금 계좌 비밀번호 확인 -- 객체 상태값과 dto 비밀번호 비교
	// 5. 출금 계좌 잔액 여부 확인 -- 객체 상태값 확인, dto와 비교
	// 6. 입금 계좌 객체 상태값 변경 처리 (거래 금액 증가)
	// 7. 입금 계좌 -- update 처리
	// 8. 출금 계좌 객체 상태값 변경 처리 (잔액 - 거래금액)
	// 9. 출금 계좌 -- update 처리
	// 10. 거래 내역 등록 처리
	// 11. 트랜잭션 처리
	@Transactional
	public void updateAccountTransfer(TransferDTO dto, Integer principalId) {
		// 1. 출금 계좌 존재 여부 확인 -- select
		Account withAccountEntity = accountRepository.findByNumber(dto.getWAccountNumber());
		if (withAccountEntity == null) {
			throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
		}
		// 2. 입금 계좌 존재 여부 확인 -- select (객체 리턴 받은 상태)
		Account depoAccountEntity = accountRepository.findByNumber(dto.getDAccountNumber());
		if (depoAccountEntity == null) {
			throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
		}
		// 3. 출금 계좌 본인 소유 확인 -- 객체 상태값과 세션 id 비교
		withAccountEntity.checkOwner(principalId);

		// 4. 출금 계좌 비밀번호 확인 -- 객체 상태값과 dto 비밀번호 비교
		withAccountEntity.checkPassword(dto.getPassword());

		// 5. 출금 계좌 잔액 여부 확인 -- 객체 상태값 확인, dto와 비교
		withAccountEntity.checkBalance(dto.getAmount());

		// 6. 입금 계좌 객체 상태값 변경 처리 (거래 금액 증가)
		depoAccountEntity.deposit(dto.getAmount());

		// 7. 입금 계좌 -- update 처리
		accountRepository.updateById(depoAccountEntity);

		// 8. 출금 계좌 객체 상태값 변경 처리 (잔액 - 거래금액)
		withAccountEntity.withdraw(dto.getAmount());

		// 9. 출금 계좌 -- update 처리
		accountRepository.updateById(withAccountEntity);

		// 10. 거래 내역 등록 처리
		History history = new History();
		history.setAmount(dto.getAmount());
		history.setWBalance(withAccountEntity.getBalance());
		history.setDBalance(depoAccountEntity.getBalance());
		history.setWAccountId(principalId);
		history.setDAccountId(depoAccountEntity.getId());

		// 결과히스토리 확인
		int rowResultCount = historyRepository.insert(history);
		System.out.println("histoiry 확인" + history);
		if (rowResultCount != 1) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 단일 계좌 조회 기능
	 * 
	 * @param accountId (pk)
	 * @return
	 */
	public Account readAccountById(Integer accountId) {
		Account accountEntity = accountRepository.findByAccountId(accountId);
		if (accountEntity == null) {
			throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return accountEntity;
	}

	/**
	 * 단일 계좌 거래내역 조회
	 * 
	 * @param type      = [all, deposit, withdrawal]
	 * @param accountId (pk)
	 * @return 전체, 입금, 출금 거래내역(3가지 타입 반환)
	 */
	// @Transactional \셀랙문은 트랜잭션 안거는데 이건 업데이트, 딜리트가 많아서 하는게 좋음\
	public List<HistoryAccount> readHistoryByAccountId(String type, Integer accountId, int page, int size) {
		List<HistoryAccount> list = new ArrayList<>();
		int limit = size;
		int offset = (page - 1) * size;
		list = historyRepository.findByAccountIdAndTypeOfHistory(type, accountId, limit, offset);
		return list;
	}

	/**
	 * 페이징
	 * 
	 * @param type
	 * @param accountId
	 * @return
	 */
	public int countHistoryByAccountIdAndType(String type, Integer accountId) {
		return historyRepository.countByAccountIdAndType(type, accountId);
	}

	public List<Account> readAccountListByUserIdForPage(Integer id, int limit, int offset) {
		List<Account> list = accountRepository.findByAccountIdForPage(id, limit, offset);
		
		return list;
	}
	
	
	
	

}