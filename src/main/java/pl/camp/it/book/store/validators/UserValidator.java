package pl.camp.it.book.store.validators;

import pl.camp.it.book.store.exceptions.UserValidationException;
import pl.camp.it.book.store.model.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserValidator {
    public static void validateName(String name) {
        String regex = "^[A-Z][a-z]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(name);
        if(!matcher.matches()) {
            throw new UserValidationException();
        }
    }

    public static void validateSurname(String surname) {
        String regex = "^[A-Z][a-z]+([ -][A-Z][a-z]+)?$";
        if(!surname.matches(regex)) {
            throw new UserValidationException();
        }
    }

    public static void validateLogin(String login) {
        String regex = "^.{5,}$";
        if(!login.matches(regex)) {
            throw new UserValidationException();
        }
    }

    public static void validatePassword(String password) {
        String regex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[-_!@#$%^&*()+=\\[\\]{};:,.?]).{5,}$";
        if(!password.matches(regex)) {
            throw new UserValidationException();
        }
    }

    public static void validateEmail(String email) {
        String regex = "^[\\w\\.-]+@[\\w\\.-]+\\.[A-Za-z0-9]{2,3}$";
        if(!email.matches(regex)) {
            throw new UserValidationException();
        }
    }

    public static void validatePasswordsEquality(String pass1, String pass2) {
        if(!pass1.equals(pass2)) {
            throw new UserValidationException();
        }
    }

    public static void validateUser(User user) {
        validateName(user.getName());
        validateSurname(user.getSurname());
        validateEmail(user.getEmail());
        validateLogin(user.getLogin());
        validatePassword(user.getPassword());
    }
}
