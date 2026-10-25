package estrutura;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Pacientes {

    private int id;
    private String nome;
    private LocalDate nascimento;
    private String email;
    private String cpf;

    // Controle interno da fila de atendimento (não faz parte do formulário de cadastro)
    private LocalDateTime horaChegada;
    private boolean atendido;

    public Pacientes() {
    }

    public Pacientes(String nome, LocalDate nascimento, String email, String cpf) {
        this.nome = nome;
        this.nascimento = nascimento;
        this.email = email;
        this.cpf = cpf;
        // Milissegundos: mesma precisão da coluna DATETIME(3), evitando arredondamentos no banco
        this.horaChegada = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        this.atendido = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getNascimento() {
        return nascimento;
    }

    public void setNascimento(LocalDate nascimento) {
        this.nascimento = nascimento;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public LocalDateTime getHoraChegada() {
        return horaChegada;
    }

    public void setHoraChegada(LocalDateTime horaChegada) {
        this.horaChegada = horaChegada;
    }

    public boolean isAtendido() {
        return atendido;
    }

    public void setAtendido(boolean atendido) {
        this.atendido = atendido;
    }
}