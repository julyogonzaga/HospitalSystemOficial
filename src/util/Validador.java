package util;
import estrutura.Pacientes;
import tela.PainelPaciente;
import dao.PacienteDAO;

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

    // Aceita qualquer CPF que tenha exatamente 11 dígitos numéricos
    public static boolean cpfValido(String cpf) {
        String digitos = somenteDigitos(cpf);
        return digitos.length() == 11;
    }

    // 12345678909 -> 123.456.789-09
    public static String formatarCpf(String cpf) {
        String digitos = somenteDigitos(cpf);
        if (digitos.length() != 11) {
            return cpf;
        }
        return digitos.substring(0, 3) + "." + digitos.substring(3, 6) + "."
                + digitos.substring(6, 9) + "-" + digitos.substring(9);
    }
}