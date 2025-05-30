# L2JEder Interlude - Servidor Privado Lineage II

Bem-vindo ao repositório do projeto ** Interlude **. Aqui você encontra todos os arquivos e instruções necessárias para rodar seu servidor de Lineage II com base na crônica Interlude.

---

## 🔧 Requisitos

- Java 14
- MariaDB (banco de dados)
- Cliente Interlude com patch customizado

---

## 📦 Downloads Necessários

| Componente   | Download |
|--------------|----------|
| 🔧 Patch Files (System, Animations, SysTextures) | [Download Patch](https://mega.nz/file/lq8UnKhT#mO4BLffqoSEqgjF6nA34wvm67eHTkJU8Zwq9s_N9TJI) |
| ☕ Java 14 | [Download Java 14](https://mega.nz/file/xzdl0BrA#unjVTTUYbTK7-Q8wkX6Ps4b43Q5K6yOzuFGKdce3vHc) |
| 🛢️ MariaDB | [Download MariaDB](https://mega.nz/file/E6FG1LyI#uMzj3r2X1NpL9z7cINfd41Q0IRXNLgUnkcUNCd_BDtQ) |

---

## 🧪 Instalação

### 1. Instalar o Java 14

- Execute o instalador do Java baixado.
- Confirme se o `java -version` retorna algo como `14.0.x`.

### 2. Instalar o MariaDB

- Execute o instalador.
- Crie um usuário com:
  - **Usuário**: `root`
  - **Senha**: `root`
- Finalize a instalação com o serviço ativo.

---

## 🧰 Configuração do Banco de Dados

- Navegue até a pasta `tools/`
- Execute o script correspondente ao seu sistema operacional:

### No Windows:

```bat
install_db.bat

### No Linux:
chmod +x database_installer.sh
./install_db.sh
