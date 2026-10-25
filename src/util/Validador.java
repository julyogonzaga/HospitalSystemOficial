package util;

import java.util.regex.Pattern;

public final class Validador {

    private static final Pattern EMAIL =
            Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)*\\.[a-zA-Z]{2,}$");

    private Validador() {
    }

    public static String somenteDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    public static boolean emailValido(String email) {
        return email != null && email.length() <= 100 && EMAIL.matcher(email).matches();
    }

    // Valida o CPF pelos dois dígitos verificadores (não basta ter 11 números)
    public static boolean cpfValido(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}")) {
            return false;
        }
        if (cpf.chars().distinct().count() == 1) {
            return false; // rejeita 111.111.111-11, 000.000.000-00 etc.
        }
        return calcularDigito(cpf, 9) == cpf.charAt(9) - '0'
                && calcularDigito(cpf, 10) == cpf.charAt(10) - '0';
    }

    private static int calcularDigito(String cpf, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (cpf.charAt(i) - '0') * (quantidade + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    // 12345678909 -> 123.456.789-09
    public static String formatarCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "."
                + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }
}