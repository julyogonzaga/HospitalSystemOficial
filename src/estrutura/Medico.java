public class Medico {
    private int idMedico;
    private String nome;
    private String cpf;
    private String crm;
    private String telefone;
    private int idEspecialidade;

    // Construtor completo (utilizado na busca do banco de dados)
    public Medico(int idMedico, String nome, String cpf, String crm, String telefone, int idEspecialidade) {
        this.idMedico = idMedico;
        this.nome = nome;
        this.cpf = cpf;
        this.crm = crm;
        this.telefone = telefone;
        this.idEspecialidade = idEspecialidade;
    }

    // Construtor sem ID (utilizado no cadastro de novos médicos)
    public Medico(String nome, String cpf, String crm, String telefone, int idEspecialidade) {
        this.nome = nome;
        this.cpf = cpf;
        this.crm = crm;
        this.telefone = telefone;
        this.idEspecialidade = idEspecialidade;
    }

    // Getters
    public int getIdMedico() { return idMedico; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getCrm() { return crm; }
    public String getTelefone() { return telefone; }
    public int getIdEspecialidade() { return idEspecialidade; }
}