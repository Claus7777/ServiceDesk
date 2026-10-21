package dev.servicedesk.account;

public class AccountAlreadyExistsException extends RuntimeException {
    public AccountAlreadyExistsException() { super("An account with this email already exists."); }
}
