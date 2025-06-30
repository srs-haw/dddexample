package de.haw.swa.ordermanagement.application.service;

import de.haw.swa.ordermanagement.domain.model.shared.Money;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    
    private final Environment environment;
    
    public PaymentService(Environment environment) {
        this.environment = environment;
    }
    
    public boolean processPayment(Money amount) {
        // In test environment, always succeed to avoid flaky tests
        if (isTestEnvironment()) {
            return true;
        }
        
        // Simulate external payment processing
        // In a real application, this would integrate with payment providers like Stripe, PayPal, etc.
        try {
            Thread.sleep(100); // Simulate network call
            
            // Simulate 95% success rate
            return Math.random() > 0.05;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    private boolean isTestEnvironment() {
        String[] activeProfiles = environment.getActiveProfiles();
        for (String profile : activeProfiles) {
            if ("test".equals(profile)) {
                return true;
            }
        }
        return false;
    }
}