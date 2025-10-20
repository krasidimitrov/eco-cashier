package com.ee.ecocashier.service;

import com.ee.ecocashier.model.CashOperationRequest;
import org.springframework.stereotype.Service;

@Service
public class CashOperationServiceImpl implements CashOperationService {

    @Override
    public void handleOperation(CashOperationRequest request) {
        switch (request.operationType()) {
            case DEPOSIT -> deposit(request);
            case WITHDRAW -> withdraw(request);
        }
    }

    private void deposit(CashOperationRequest request) {
    }

    private void withdraw(CashOperationRequest request) {
    }


}
