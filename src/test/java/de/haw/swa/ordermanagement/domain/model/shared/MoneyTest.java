package de.haw.swa.ordermanagement.domain.model.shared;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {
    
    @Test
    void shouldCreateMoneyWithEuro() {
        Money money = Money.euro(BigDecimal.valueOf(10.50));
        
        assertEquals(BigDecimal.valueOf(10.50).setScale(2), money.getAmount());
        assertEquals(Currency.getInstance("EUR"), money.getCurrency());
    }
    
    @Test
    void shouldCreateMoneyWithDoubleValue() {
        Money money = Money.euro(10.99);
        
        assertEquals(BigDecimal.valueOf(10.99), money.getAmount());
        assertEquals(Currency.getInstance("EUR"), money.getCurrency());
    }
    
    @Test
    void shouldThrowExceptionForNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> {
            Money.euro(BigDecimal.valueOf(-10.00));
        });
    }
    
    @Test
    void shouldThrowExceptionForNullAmount() {
        assertThrows(NullPointerException.class, () -> {
            new Money(null, Currency.getInstance("EUR"));
        });
    }
    
    @Test
    void shouldThrowExceptionForNullCurrency() {
        assertThrows(NullPointerException.class, () -> {
            new Money(BigDecimal.valueOf(10.00), null);
        });
    }
    
    @Test
    void shouldAddMoneyWithSameCurrency() {
        Money money1 = Money.euro(10.00);
        Money money2 = Money.euro(5.50);
        
        Money result = money1.add(money2);
        
        assertEquals(Money.euro(15.50), result);
    }
    
    @Test
    void shouldThrowExceptionWhenAddingDifferentCurrencies() {
        Money euroMoney = Money.euro(10.00);
        Money usdMoney = new Money(BigDecimal.valueOf(10.00), Currency.getInstance("USD"));
        
        assertThrows(IllegalArgumentException.class, () -> {
            euroMoney.add(usdMoney);
        });
    }
    
    @Test
    void shouldMultiplyByQuantity() {
        Money money = Money.euro(10.00);
        
        Money result = money.multiply(3);
        
        assertEquals(Money.euro(30.00), result);
    }
    
    @Test
    void shouldRoundToTwoDecimalPlaces() {
        Money money = new Money(BigDecimal.valueOf(10.999), Currency.getInstance("EUR"));
        
        assertEquals(BigDecimal.valueOf(11.00).setScale(2), money.getAmount());
    }
    
    @Test
    void shouldBeEqualWhenSameAmountAndCurrency() {
        Money money1 = Money.euro(10.00);
        Money money2 = Money.euro(10.00);
        
        assertEquals(money1, money2);
        assertEquals(money1.hashCode(), money2.hashCode());
    }
    
    @Test
    void shouldNotBeEqualWhenDifferentAmount() {
        Money money1 = Money.euro(10.00);
        Money money2 = Money.euro(15.00);
        
        assertNotEquals(money1, money2);
    }
    
    @Test
    void shouldNotBeEqualWhenDifferentCurrency() {
        Money euroMoney = Money.euro(10.00);
        Money usdMoney = new Money(BigDecimal.valueOf(10.00), Currency.getInstance("USD"));
        
        assertNotEquals(euroMoney, usdMoney);
    }
    
    @Test
    void shouldReturnCorrectStringRepresentation() {
        Money money = Money.euro(10.50);
        
        String result = money.toString();
        
        assertEquals("10.50 EUR", result);
    }
}