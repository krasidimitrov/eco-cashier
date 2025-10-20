package com.ee.ecocashier.service;

import com.ee.ecocashier.model.CashOperationRequest;

public interface CashOperationService {

    void handleOperation(CashOperationRequest cashOperationRequest);
}
