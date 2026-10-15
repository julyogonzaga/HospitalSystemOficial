# 🏥 Sistema Hospitalar (Java Swing & MySQL)

Sistema de Gestão Hospitalar modular desenvolvido em **Java (Swing puro)** com banco de dados **MySQL**, estruturado com uma identidade visual moderna, elegante e "arredondada" (`custom paintComponent`), garantindo uma experiência de usuário fluida e profissional.

---

## 🚀 Funcionalidades e Módulos

O sistema é dividido por perfis de acesso, permitindo uma navegação segura e organizada:

1. **Seleção de Perfil (`TelaEscolhaPerfil`)**:
    - Tela de entrada elegante com escolha dinâmica de papéis (Médico, Paciente e Administrador).
2. **Módulo de Médicos (`TelaMedico`)**:
    - Cadastro, edição, exclusão e listagem de médicos.
    - Validação de campos e busca em tempo real (filtra por nome, CPF, CRM ou telefone).
    - Seletor de especialidades dinâmico (`JComboBox` customizado) integrado diretamente ao banco de dados.
3. **Módulo Administrativo (`TelaLoginAdmin` & `TelaPainelAdmin`)**:
    - Autenticação segura de administradores com login e senha salvos no banco.
    - Opção de cadastro de novos administradores.
    - Painel de controle preparado para a chamada e gerenciamento de pacientes na recepção/consultórios.
4. **Módulo de Pacientes (Em desenvolvimento)**:
    - Estrutura integrada para recebimento e gerenciamento de fichas de pacientes.

---

## 🎨 Padrão Visual e Arquitetura de UI

Para fugir do visual padrão e antiquado do Java Swing nativo, o projeto utiliza componentes totalmente personalizados desenhados via código (`Graphics2D` com anti-aliasing):
- **`ComboArredondado`**: ComboBox com cantos arredondados, fundo suave e renderizador de lista customizado.
- **`CampoArredondado` & `SenhaArredondada`**: Inputs de texto com bordas dinâmicas e efeito de foco.
- **`BotaoArredondado`**: Botões estilizados com feedback visual interativo (*hover* e *press*).
- **`PainelCartao`**: Cards flutuantes com efeito de sombra em camadas.

---

## 👥 Autores

* **[Júlio Gabriel]** - *Desenvolvedor - Estrutura Do Médico e ADM*
* **[Marcelo Ferreira]** - *Desenvolvedor- Estrutura do Paciente e ADM*

---

## 🗄️ Estrutura do Banco de Dados (`hospital_db`)

O sistema roda integrado a um banco de dados relacional MySQL contendo as principais tabelas:

```sql
-- Criação do Banco
CREATE DATABASE IF NOT EXISTS hospital_db;
USE hospital_db;

-- Tabela de Especialidades (Com suporte a INSERT IGNORE)
CREATE TABLE IF NOT EXISTS especialidades (
    id_especialidade INT AUTO_INCREMENT PRIMARY KEY,
    nome_especialidade VARCHAR(100) NOT NULL UNIQUE
);

-- Tabela de Médicos
CREATE TABLE IF NOT EXISTS medicos (
    id_medico INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    crm VARCHAR(20) NOT NULL UNIQUE,
    telefone VARCHAR(20) NOT NULL,
    id_especialidade INT,
    FOREIGN KEY (id_especialidade) REFERENCES especialidades(id_especialidade)
);

-- Tabela de Administradores
CREATE TABLE IF NOT EXISTS administradores (
    id_admin INT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);