package estrutura;
import dao.PacienteDAO;
import tela.PainelPaciente;
import util.Validador;
import bd.ConexaoBD;
import bd.ConexaoBanco;
import dao.EspecialidadeDAO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Pacientes {
    private int id;
    private String nome;
    private LocalDate nascimento;
    private String email;
    private String cpf;
    private String tipoExame;
    private LocalDateTime horaChegada;
    private boolean atendido;

    // Construtor completo com os 8 parâmetros (necessário para o DAO)
    public Pacientes(int id, String nome, LocalDate nascimento, String email, String cpf, String tipoExame, LocalDateTime horaChegada, boolean atendido) {
        this.id = id;
        this.nome = nome;
        this.nascimento = nascimento;
        this.email = email;
        this.cpf = cpf;
        this.tipoExame = tipoExame;
        this.horaChegada = horaChegada;
        this.atendido = atendido;
    }

    // Construtor para novos cadastros
    public Pacientes(String nome, LocalDate nascimento, String email, String cpf, String tipoExame, LocalDateTime horaChegada) {
        this.nome = nome;
        this.nascimento = nascimento;
        this.email = email;
        this.cpf = cpf;
        this.tipoExame = tipoExame;
        this.horaChegada = horaChegada;
        this.atendido = false;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public LocalDate getNascimento() { return nascimento; }
    public void setNascimento(LocalDate nascimento) { this.nascimento = nascimento; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getTipoExame() { return tipoExame; }
    public void setTipoExame(String tipoExame) { this.tipoExame = tipoExame; }

    public LocalDateTime getHoraChegada() { return horaChegada; }
    public void setHoraChegada(LocalDateTime horaChegada) { this.horaChegada = horaChegada; }

    public boolean isAtendido() { return atendido; }
    public void setAtendido(boolean atendido) { this.atendido = atendido; }
}